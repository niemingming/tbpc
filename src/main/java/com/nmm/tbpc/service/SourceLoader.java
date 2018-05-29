package com.nmm.tbpc.service;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.nmm.tbpc.po.ColorPrice;
import com.nmm.tbpc.po.ColorType;
import com.nmm.tbpc.po.Project;
import com.nmm.tbpc.variable.SystemProperty;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author nmm 2018/5/28
 * @description
 */
@Service
public class SourceLoader {

    private String resPath = SystemProperty.RES_PATH;
    private Logger logger = LoggerFactory.getLogger(SourceLoader.class);

    /**
     * 加载目标资源，返回document对象
     * @param url
     * @return
     */
    public Document loadDocument(String url) throws IOException {
        //创建js执行脚本
        WebClient webClient = new WebClient();
        //设置超时时间
        webClient.getOptions().setTimeout(10000);
        //设置执行js
        webClient.getOptions().setJavaScriptEnabled(true);
        //设置不渲染css
        webClient.getOptions().setCssEnabled(false);
        //设置js报错不抛出
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        //加载数据
        HtmlPage page = webClient.getPage(url);
        DomElement element = page.getElementById("J_DivItemDesc");
        element.focus();
        //等待js执行
        webClient.waitForBackgroundJavaScript(10000);

        String str = page.asXml();

        //创建解析器
        return Jsoup.parse(str);
    }

    /**
     * 加载项目信息
     * @param url
     * @return
     */
    public Project loadProject(String url) throws Exception {
        return loadProject(loadDocument(url));
    }

    /**
     * 加载项目信息
     * @param document
     * @return
     */
    public Project loadProject(Document document) throws Exception {
        Project project = new Project();
        //获取名称
        Element title = document.select("#J_Title h3").first();
        if (title == null){
            logger.info("无法获取项目名称！");
            return null;
        }
        String projectName = title.text();
        project.setProjectName(projectName);
        String cachePath = resPath + "/" + projectName;
        File file = new File(cachePath);
        //创建缓存目录
        if (!file.exists()&& !file.isDirectory()){
            file.mkdirs();
        }
        //获取Hub配置信息
        Map<String,ScriptObjectMirror> hubConfig = loadScriptConfig(document);
        //load顶部左侧图片
        loadLeftTopPics(file,document);
        //load顶部右侧图片
        loadRightTopPics(file,document,project,hubConfig.get("sku"));
        //加载底部详情图片
        loadBottomDetailPics(file,document,hubConfig.get("g_config"));
        //运费信息  itemId在g_config中，areaId如何获取？
//        https://detailskip.taobao.com/json/deliveryFee.htm?areaId=370811&itemId=570004077409&_ksTS=1527586383959_1313&callback=jsonp211

        return project;
    }

    /**
     * 加载js配置文件
     * @param document
     * @return
     */
    private Map<String,ScriptObjectMirror> loadScriptConfig(Document document) throws ScriptException {
        Map<String,ScriptObjectMirror> hubconfig = new HashMap<>();
        Elements scripts = document.select("script");
        //创建js引擎
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine javascript = manager.getEngineByName("javascript");
        for (int i = 0; i < scripts.size(); i++){
            String content = scripts.get(i).html();
            if (content != null && content.contains("Hub = {}")){//sku配置信息，主要是颜色分类'
                content = content.substring(content.indexOf("Hub = {}"),content.lastIndexOf("//"));
                ScriptObjectMirror sku = (ScriptObjectMirror) javascript.eval(content + ";Hub.config.get('sku')");
                hubconfig.put("sku",sku);
            }
            if (content != null && content.contains("var g_config")) {//获取详情图片配置信息
                content = content.substring(content.indexOf("var g_config"),content.lastIndexOf("//"));
                ScriptObjectMirror gConfig = (ScriptObjectMirror) javascript.eval("var location = {protocol:'http:'};"+content + ";g_config;");
                hubconfig.put("g_config",gConfig);
            }
        }
        return hubconfig;
    }

    /**
     * 加载底部详情图片
     * @param file
     * @param document
     * @param g_config
     */
    private void loadBottomDetailPics(File file, Document document, ScriptObjectMirror g_config) throws IOException {
        //详情页面懒加载，这是图片地址,地址从hub中取
        String detailInfoUrl = SystemProperty.PIC_SCHEMA + g_config.get("descUrl") + "";
        HttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(detailInfoUrl);
        HttpResponse response = client.execute(get);
        String res = EntityUtils.toString(response.getEntity());
        if (res != null&& res.contains("'")){
            res = res.substring(res.indexOf("'") + 1,res.lastIndexOf("'"));
        }
        get.abort();
        Document images = Jsoup.parse(res);
        Elements imgs = images.select("img");
        for (int i = 0; i < imgs.size(); i++ ){
            String picurl = imgs.get(i).attr("src").replace("https:",SystemProperty.PIC_SCHEMA);
            String filename = SystemProperty.DETAIL_IMAGES + i + picurl.substring(picurl.lastIndexOf("."));
            FileOutputStream fos = new FileOutputStream(new File(file,filename));
            get = new HttpGet(picurl);
            client.execute(get).getEntity().writeTo(fos);
            fos.flush();
            fos.close();
            get.abort();
        }
    }

    /**
     * 加载顶部右侧图片
     *
     * @param file
     * @param document
     * @param project
     * @param sku  配置文件用于获取数据.
     *             skuMap:价格映射表
     *             propertyMemoMap:颜色分类标签
     */
    private void loadRightTopPics(File file, Document document, Project project, ScriptObjectMirror sku) throws Exception {
        Elements lis = document.selectFirst("#J_isku J_Prop_Color ul").select("li");
        HttpClient client = HttpClients.createDefault();
        String flag = "background:url(";
        //配置信息
        ScriptObjectMirror property = sku.get("propertyMemoMap") == null ? null :(ScriptObjectMirror) sku.get("propertyMemoMap");
        for (int i = 0; i < lis.size(); i++ ){
            //js对应数据的关键字
            String key = lis.get(i).attr("data-value");
            ColorType colorType = new ColorType();
            colorType.setDataValue(key);//key值
            if (property != null){
                colorType.setDesc(property.get(key) + "");//描述名称
            }
            project.addColor(colorType);

            //获取连接
            String picurl = lis.get(i).selectFirst("a").attr("style");
            if (picurl != null && picurl.contains(flag)){
                int index = picurl.indexOf(flag) + flag.length();
                picurl = picurl.substring(index,picurl.indexOf(")",index));
                picurl = SystemProperty.PIC_SCHEMA + picurl.substring(0,picurl.lastIndexOf("_"));
                HttpGet get = new HttpGet(picurl);
                String filename = SystemProperty.RIGHT_TOP_IMAGES + SystemProperty.FILE_SEPORTER  + colorType.getDesc() + SystemProperty.FILE_SEPORTER + i + picurl.substring(picurl.lastIndexOf("."));
                colorType.setPic(filename);
                FileOutputStream fos = new FileOutputStream(new File(file,filename));
                try{
                    client.execute(get).getEntity().writeTo(fos);
                    fos.flush();
                    fos.close();
                    get.abort();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        //尝试获取规格信息
        Elements dls = document.select("#J_isku J_Prop");
        //价格信息
        property = sku.get("skuMap") == null ? null : (ScriptObjectMirror) sku.get("skuMap");
        if (dls.size() == 2){//表示有多个属性，还有尺码属性
            Elements prolis = dls.get(1).select("ul li");
            //遍历颜色分类，然后遍历尺码获取价格信息
            for (int i = 0; i < project.getColorTypes().size(); i++){
                ColorType colorType = project.getColorTypes().get(i);
                for (int j = 0; j < prolis.size(); j++){
                    String key = prolis.get(j).attr("data-value");
                    key = ";" + colorType.getDataValue() + ";" + key + ";";
                    //获取价格信息
                    ScriptObjectMirror price = (ScriptObjectMirror) property.get(key);
                    //获取尺码
                    String style = prolis.get(j).selectFirst("span").html();
                    //尺码信息
                    ColorPrice colorPrice = new ColorPrice();
                    colorPrice.setStyle(style);
                    colorPrice.setPrice(Double.valueOf(price.get("price")+""));
                    //添加样式价格
                    colorType.getColorPrices().add(colorPrice);
                }
            }
        }else {//没有对应尺码，就用默认值，查询价格方式一致
            for (int i = 0; i < project.getColorTypes().size(); i++){
                ColorType colorType = project.getColorTypes().get(i);
                String key = ";" + colorType.getDataValue() + ";";
                //获取价格信息
                ScriptObjectMirror price = (ScriptObjectMirror) property.get(key);
                ColorPrice colorPrice = new ColorPrice();
                colorPrice.setPrice(Double.valueOf(price.get("price")+""));
                //添加样式价格
                colorType.getColorPrices().add(colorPrice);
            }
        }
    }

    /**
     * 下载左侧顶部图片
     * @param file
     * @param document
     */
    private void loadLeftTopPics(File file, Document document) throws Exception {
        Elements imgs = document.selectFirst(".tb-item-info-l #J_UlThumb").select("img");
        HttpClient client = HttpClients.createDefault();
        for (int i = 0; i < imgs.size(); i++){
            String picurl = imgs.get(i).attr("data-src");
            if (picurl != null){
                picurl = SystemProperty.PIC_SCHEMA + picurl.substring(0,picurl.lastIndexOf("_"));
                HttpGet get = new HttpGet(picurl);
                String filename = SystemProperty.LEFT_TOP_IMAGES + i+ picurl.substring(picurl.lastIndexOf("."));
                FileOutputStream fos = new FileOutputStream(new File(file,filename));
                try{
                    client.execute(get).getEntity().writeTo(fos);
                    fos.flush();
                    fos.close();
                    get.abort();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }


    }

    public String getResPath() {
        return resPath;
    }
    public void setResPath(String resPath) {
        this.resPath = resPath;
    }
}
