package com.nmm.tbpc.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nmm.tbpc.po.ColorPrice;
import com.nmm.tbpc.po.ColorType;
import com.nmm.tbpc.po.FreightCast;
import com.nmm.tbpc.po.Project;
import com.nmm.tbpc.variable.SystemProperty;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.ws.ServiceMode;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 加载1688货源网数据
 */
@Service
public class SourceLoader1688 extends BaseLoader {

    public SourceLoader1688(){
        super(true);
    }

    public Project loadProject(String url) throws Exception {
        return super.loadProject(url );
    }

    @Override
    public Document loadDocument(String url) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(url);
        byte[] buff = new byte[fileInputStream.available()];
        fileInputStream.read(buff);
        fileInputStream.close();

        return Jsoup.parse(new String(buff,"GBK"));
    }

    @Override
    public Project loadProject(Document document) throws Exception {
        //获取目录文档结构
        Project project = super.loadProject(document);
        File file = new File(project.getCachePath());
        loadProjectProperty(document,project);
        //加载js配置文件var iDetailData
        Map<String,ScriptObjectMirror> config = loadScriptConfig(document);
        //加载左上方图片
        loadLeftTopPics(file,document);
        //加载右上方图片
        loadRightTopPics(file,document,config.get("sku"),project);
        //加载底部新详情图片
        loadBottomDetailPics(file,document,project);
        //加载运费信息
//        loadFreightCast(document,project,config.get("config"));
        return project;
    }



    /**
     * 加载属性信息
     * @param document
     * @param project
     */
    private void loadProjectProperty(Document document, Project project) {
        Elements props = document.selectFirst("#mod-detail-attributes tbody").select("td");
        for (int i = 0; i < props.size(); i+=2) {
            if(i+1 >= props.size() ){
                break;
            }
            String name = props.get(i).text();
            String value = props.get(i+1).html();
            project.addProperty(name,value);
        }
    }


    @Override
    protected String selectProjectName(Document document) {

        Element title = document.select("#mod-detail-title h1").first();
        if (title == null){
            logger.info("无法获取项目名称！");
            return null;
        }
        return title.text();
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
            if (content != null && content.contains("var iDetailData")){//sku配置信息，主要是颜色分类'
                content = content.substring(content.indexOf("var iDetailData"));//,content.lastIndexOf("//")
                ScriptObjectMirror sku = (ScriptObjectMirror) javascript.eval(content + ";iDetailData");
                hubconfig.put("sku", (ScriptObjectMirror) sku.get("sku"));
            }
            //获取配置信息
            if (content != null && content.contains("var iDetailConfig")) {//基本配置信息
                content = content.substring(content.indexOf("var iDetailConfig"),content.indexOf("var iDetailData"));
                ScriptObjectMirror config = (ScriptObjectMirror) javascript.eval(content + ";iDetailConfig");
                hubconfig.put("config", config);
            }
        }
        return hubconfig;
    }


    /**
     * 加载左上方图片
     * @param file
     * @param document
     */
    private void loadLeftTopPics(File file, Document document) {
        Elements imgs = document.selectFirst("#dt-tab ul").select("img");
        for (int i = 0; i < imgs.size(); i++){
            String picurl = imgs.get(i).attr("src");
            if (picurl != null){
                picurl = picurl.replace(".60x60","");
                String filename = SystemProperty.LEFT_TOP_IMAGES + i+ picurl.substring(picurl.lastIndexOf("."));
                try{
                    loadPicture(file,filename,picurl);
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * 加载右上角图片
     * @param file
     * @param document
     * @param sku
     * @param project
     */
    private void loadRightTopPics(File file, Document document, ScriptObjectMirror sku, Project project) {
        //从js中获取配置信息。skuProps包括prop颜色和prop尺码，skuMap通过&gt;关联颜色获取价格和库存
        ScriptObjectMirror colors = (ScriptObjectMirror) ((ScriptObjectMirror) sku.get("skuProps")).get("0");
        colors = (ScriptObjectMirror) colors.get("value");
        ScriptObjectMirror cms = (ScriptObjectMirror) ((ScriptObjectMirror) sku.get("skuProps")).get("1");
        cms = (ScriptObjectMirror) cms.get("value");
        //获取配置信息；
        ScriptObjectMirror skuMap = (ScriptObjectMirror) sku.get("skuMap");
        String linkStr = "&gt;";
        for (int i = 0; i < colors.size(); i++){
            ScriptObjectMirror color = (ScriptObjectMirror) colors.get(i+"");
            ColorType colorType = new ColorType();
            project.addColor(colorType);
            colorType.setDesc(color.get("name")+"");
            String picurl = color.get("imageUrl")+"";
            if (picurl != null){
                String filename = SystemProperty.RIGHT_TOP_IMAGES + SystemProperty.FILE_SEPORTER  + colorType.getDesc() + SystemProperty.FILE_SEPORTER + i + picurl.substring(picurl.lastIndexOf("."));
                try{
                    loadPicture(file,filename,picurl);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            colorType.setPic(picurl);
            //遍历尺码
            for (int j = 0; j < cms.size(); j++ ){
                ScriptObjectMirror cm = (ScriptObjectMirror) cms.get(j+"");
                ColorPrice colorPrice = new ColorPrice();
                colorPrice.setStyle(cm.get("name")+"");
                String key = colorType.getDesc() + linkStr + colorPrice.getStyle();
                ScriptObjectMirror price = (ScriptObjectMirror) skuMap.get(key);
                colorPrice.setPrice(Double.valueOf(price.get("price")+""));
                colorType.getColorPrices().add(colorPrice);
            }
        }
    }

    /**
     * 加载底部详情图片
     * @param file
     * @param document
     * @param project
     */
    private void loadBottomDetailPics(File file, Document document, Project project) throws IOException {
        Element detail = document.selectFirst("#desc-lazyload-container");
        String infoUrl = detail.attr("data-tfs-url");
        //从这里加载数据
        HttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(infoUrl);
        String scripts = EntityUtils.toString(client.execute(get).getEntity());
        scripts  = scripts.substring(scripts.indexOf(":\"") + 2,scripts.lastIndexOf("\"}")).replace("\\\"","");

        //获取文字描述
//        Elements descs = detail.select("span");
//        for (int i = 0; i < descs.size(); i++) {
//            String content = descs.get(i).html();
//            if (content != null){
//                content = content.replace("&nbsp;","");
//                if (content.length() > 0){
//                    project.setDescription(project.getDescription() + content + "\r\n");
//                }
//            }
//        }
        //获取图片信息
        Elements imgs = Jsoup.parse(scripts).select("img");
        for (int i = 0; i < imgs.size(); i++){
            String picurl = imgs.get(i).attr("src");
            String filename = SystemProperty.DETAIL_IMAGES + i + picurl.substring(picurl.lastIndexOf("."));
            loadPicture(file,filename,picurl);
        }
    }

    /**
     * 加载运费信息，因为1688的省份编码没有，我们采用关键地域的
     * 甘肃、黑龙江、海南、云南
     * @param document
     * @param project
     * @param config
     */
    private void loadFreightCast(Document document, Project project, ScriptObjectMirror config) throws IOException {

        Element costEle = document.selectFirst(".unit-detail-freight-cost");
        String jconfig = costEle.attr("data-unit-config");
        JsonObject costcfg = new Gson().fromJson(jconfig,JsonObject.class);


        String baseUrl = costcfg.get("calculationUrl").getAsString()+"?";
        baseUrl += "callback=jQuery_" + System.currentTimeMillis() + "&";
        baseUrl += "offerId=" + config.get("offerId") + "&";
        baseUrl += "templateId=" + costcfg.get("freightTemplateId").getAsString()+"&amount=" + costcfg.get("beginAmount")+"&";
        baseUrl += "weight=" + costcfg.get("unitWeight").getAsString() + "&price=" + costcfg.get("refPrice").getAsString()+"&";
        baseUrl += "memberId=guest&volume=0&countryCode=1001&flow=" + costcfg.get("flow").getAsString() + "&";
        baseUrl += "excludeAreaCode4FreePostage=" + costcfg.get("excludeAreaCode4FreePostage").getAsString() + "&";
        baseUrl += "provinceCode={provinceCode}&cityCode={cityCode}" ;
        String[] pc = {"1181:甘肃;1232:庆阳","3559:云南;3629:丽江","1816:黑龙江;1857:大庆","1474:海南;1477:三亚"};

        HttpClient client = HttpClients.createDefault();
        Gson gson = new Gson();
        for (String areaId :pc) {
            String provinceName = areaId.split(";")[0].split(":")[1];
            String provinceId = areaId.split(";")[0].split(":")[0];
            String cityId = areaId.split(";")[1].split(":")[0];
            String cityName = areaId.split(";")[1].split(":")[1];
            HttpGet get = new HttpGet(baseUrl.replace("{provinceCode}",provinceId).replace("{cityCode}",cityId));

            String res = EntityUtils.toString(client.execute(get).getEntity());
            JsonObject freightjson = gson.fromJson(res.substring(res.indexOf("(") + 1,res.indexOf(")")),JsonObject.class);
            JsonObject data = freightjson.getAsJsonObject("data");
            double freight = data.getAsJsonArray("costs").get(0)
                    .getAsJsonObject().get("cost").getAsDouble();

            FreightCast freightCast = new FreightCast();
            freightCast.setAreaId(areaId);
            freightCast.setAreaName(provinceName + ":" + cityName);
            freightCast.setCast(freight);
            project.addFreightCast(freightCast);

        }
    }
}
