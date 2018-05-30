package com.nmm.tbpc.service;

import com.nmm.tbpc.po.ColorPrice;
import com.nmm.tbpc.po.ColorType;
import com.nmm.tbpc.po.Project;
import com.nmm.tbpc.variable.SystemProperty;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.ws.ServiceMode;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 加载1688货源网数据
 */
@Service
public class SourceLoader1688 extends BaseLoader {

    public SourceLoader1688(){
        super(false);
    }

    public Project loadProject(String url) throws Exception {
        return super.loadProject(url + "&sk=consign");
    }

    @Override
    public Project loadProject(Document document) throws Exception {
        //获取目录文档结构
        Project project = super.loadProject(document);
        File file = new File(project.getCachePath());
        //加载js配置文件var iDetailData
        Map<String,ScriptObjectMirror> config = loadScriptConfig(document);
        //加载左上方图片
        loadLeftTopPics(file,document);
        //加载右上方图片
        loadRightTopPics(file,document,config.get("sku"),project);
        return project;
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
                content = content.substring(content.indexOf("var iDetailData"),content.lastIndexOf("//"));
                ScriptObjectMirror sku = (ScriptObjectMirror) javascript.eval(content + ";iDetailData");
                hubconfig.put("sku", (ScriptObjectMirror) sku.get("sku"));
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
        Elements imgs = document.selectFirst("#dt-tab ul").select("li");
        for (int i = 0; i < imgs.size(); i++){
            String picurl = imgs.get(i).attr("original");
            if (picurl != null){
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
        ScriptObjectMirror colors = (ScriptObjectMirror) ((ScriptObjectMirror) sku.get("skuProps")).get(0);
        colors = (ScriptObjectMirror) colors.get("value");
        ScriptObjectMirror cms = (ScriptObjectMirror) ((ScriptObjectMirror) sku.get("skuProps")).get(1);
        cms = (ScriptObjectMirror) cms.get("value");
        //获取配置信息；
        ScriptObjectMirror skuMap = (ScriptObjectMirror) sku.get("skuMap");
        String linkStr = "&gt;";
        for (int i = 0; i < colors.size(); i++){
            ScriptObjectMirror color = (ScriptObjectMirror) colors.get(i);
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
                ScriptObjectMirror cm = (ScriptObjectMirror) cms.get(j);
                ColorPrice colorPrice = new ColorPrice();
                colorPrice.setStyle(cm.get("name")+"");
                String key = colorType.getDesc() + linkStr + colorPrice.getStyle();
                ScriptObjectMirror price = (ScriptObjectMirror) skuMap.get(key);
                colorPrice.setPrice(Double.valueOf(price.get("price")+""));
                colorType.getColorPrices().add(colorPrice);
            }
        }
    }
}
