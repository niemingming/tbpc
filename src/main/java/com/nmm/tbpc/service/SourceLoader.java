package com.nmm.tbpc.service;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nmm.tbpc.po.*;
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
public class SourceLoader extends BaseLoader{
    private Logger logger = LoggerFactory.getLogger(SourceLoader.class);



    /**
     * 加载项目信息
     * @param document
     * @return
     */
    public Project loadProject(Document document) throws Exception {
        Project project = super.loadProject(document);
        File file = new File(project.getCachePath());
        //加载属性信息
        loadProjectProperty(document,project);
        //获取Hub配置信息
        Map<String,ScriptObjectMirror> hubConfig = loadScriptConfig(document);
        //load顶部左侧图片
        loadLeftTopPics(file,document);
        //load顶部右侧图片
        loadRightTopPics(file,document,project,hubConfig.get("sku"));
        //加载底部详情图片
        loadBottomDetailPics(file,document,hubConfig.get("g_config"));
        //运费信息  itemId在g_config中，areaId如何获取？
        loadFreightCast(project,hubConfig.get("g_config"));

        return project;
    }

    @Override
    protected String selectProjectName(Document document) {
        Element title = document.select("#J_Title h3").first();
        if (title == null){
            logger.info("无法获取项目名称！");
            return null;
        }
        return title.text();
    }

    /**
     * 加载商品属性信息
     * @param document
     * @param project
     */
    private void loadProjectProperty(Document document, Project project) {
        Elements props = document.selectFirst("#attributes ul").select("li");
        for (int i = 0; i < props.size(); i++) {
            String value = props.get(i).attr("title");
            String name = props.get(i).html().split(":")[0];
            project.addProperty(name,value);
        }
    }

    /**
     *
     * @param project
     * @param g_config
     */
    private void loadFreightCast(Project project, ScriptObjectMirror g_config) throws IOException {
        String itemId = g_config.get("itemId") + "";
        String baseUrl = "http://detailskip.taobao.com/json/deliveryFee.htm?itemId=" + itemId + "&_ksTS=" +
                System.currentTimeMillis()+ "_1313&callback=jsonp211&areaId=";
        HttpClient client = HttpClients.createDefault();
        Gson gson = new Gson();
        for (String areaId : SystemProperty.AREA_IDS) {
            HttpGet get = new HttpGet(baseUrl + areaId);
            String res = EntityUtils.toString(client.execute(get).getEntity());
            JsonObject freightjson = gson.fromJson(res.substring(res.indexOf("(") + 1,res.indexOf(")")),JsonObject.class);
            JsonObject data = freightjson.getAsJsonObject("data");
            String areaName = data.get("areaName").getAsString();
            String freight = data.getAsJsonObject("serviceInfo").getAsJsonArray("list").get(0)
                    .getAsJsonObject().get("info").getAsString();

            FreightCast freightCast = new FreightCast();
            freightCast.setAreaId(areaId);
            freightCast.setAreaName(areaName);
            if (freight.contains("免运费")){
                freightCast.setCast(0);
            }else {
                freightCast.setCast(Double.valueOf(freight.substring(freight.indexOf("</span>")+ "</span>".length())));
            }
            project.addFreightCast(freightCast);

        }
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
            loadPicture(file,filename,picurl);
        }
    }

    /**
     * 加载顶部右侧图片
     *
     * @param file
     * @param document
     * @param project
     * @param skup  配置文件用于获取数据.
     *             skuMap:价格映射表
     *             propertyMemoMap:颜色分类标签
     */
    private void loadRightTopPics(File file, Document document, Project project, ScriptObjectMirror skup) throws Exception {
        Elements lis = document.selectFirst("#J_isku .J_Prop_Color ul").select("li");
        ScriptObjectMirror sku = (ScriptObjectMirror) skup.get("valItemInfo");
        String flag = "background:url(";
        //配置信息
        ScriptObjectMirror property = sku.get("propertyMemoMap") == null ? null :(ScriptObjectMirror) sku.get("propertyMemoMap");
        for (int i = 0; i < lis.size(); i++ ){
            //js对应数据的关键字
            String key = lis.get(i).attr("data-value");
            ColorType colorType = new ColorType();
            colorType.setDataValue(key);//key值
            if (property != null&&property.get(key) != null){
                colorType.setDesc(property.get(key) + "");//描述名称
            }else {//尝试获取span数据
                Element span = lis.get(i).selectFirst("span");
                if (span != null) {
                    colorType.setDesc(span.html());
                }
            }
            project.addColor(colorType);

            //获取连接
            String picurl = lis.get(i).selectFirst("a").attr("style");
            if (picurl != null && picurl.contains(flag)){
                int index = picurl.indexOf(flag) + flag.length();
                picurl = picurl.substring(index,picurl.indexOf(")",index));
                picurl = SystemProperty.PIC_SCHEMA + picurl.substring(0,picurl.lastIndexOf("_"));
                String filename = SystemProperty.RIGHT_TOP_IMAGES + SystemProperty.FILE_SEPORTER  + colorType.getDesc() + SystemProperty.FILE_SEPORTER + i + picurl.substring(picurl.lastIndexOf("."));
                colorType.setPic(filename);

                loadPicture(file,filename,picurl);

            }else{
                colorType.setPic(colorType.getDesc());
            }
        }
        //尝试获取规格信息
        Elements dls = document.select("#J_isku .J_Prop");
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
        for (int i = 0; i < imgs.size(); i++){
            String picurl = imgs.get(i).attr("data-src");
            if (picurl != null){
                picurl = SystemProperty.PIC_SCHEMA + picurl.substring(0,picurl.lastIndexOf("_"));
                String filename = SystemProperty.LEFT_TOP_IMAGES + i+ picurl.substring(picurl.lastIndexOf("."));
                try{
                    loadPicture(file,filename,picurl);
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
