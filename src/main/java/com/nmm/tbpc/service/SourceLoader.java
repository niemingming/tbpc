package com.nmm.tbpc.service;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.nmm.tbpc.po.ColorType;
import com.nmm.tbpc.po.Project;
import com.nmm.tbpc.variable.SystemProperty;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
        //load顶部左侧图片
        loadLeftTopPics(file,document);
        //load顶部右侧图片
        loadRightTopPics(file,document,project);
        //加载底部详情图片
        loadBottomDetailPics(file,document);
        return project;
    }

    /**
     * 加载底部详情图片
     * @param file
     * @param document
     */
    private void loadBottomDetailPics(File file, Document document) {
        //详情页面懒加载，这是图片地址,地址从hub中取
//        Hub.config.set('desc', {
//                dummy       : false,
//                apiImgInfo  : '//tds.alicdn.com/json/item_imgs.htm?t=TB1LpCmtSBYBeNjy0FeXXbnmFXa&sid=677255500&id=570316136094&s=f38b2410a68ac20855b7fee6bbd5f64d&v=2&m=1',
//                similarItems: {
//            api           : '//tds.alicdn.com/recommended_same_type_items.htm?v=1',
//                    rstShopId     : '482284953',
//                    rstItemId     : '570316136094',
//                    rstdk         : 0,
//                    rstShopcatlist: ''
//        }
//    });
//        https://tds.alicdn.com/json/item_imgs.htm?cb=jsonp_image_info&t=TB1LpCmtSBYBeNjy0FeXXbnmFXa&sid=677255500&id=570316136094&s=f38b2410a68ac20855b7fee6bbd5f64d&v=2&m=1
    }

    /**
     * 加载顶部右侧图片
     * @param file
     * @param document
     * @param project
     */
    private void loadRightTopPics(File file, Document document, Project project) throws Exception {
        Elements lis = document.select("#J_isku ul li");
        HttpClient client = HttpClients.createDefault();
        String flag = "background:url(";
        for (int i = 0; i < lis.size(); i++ ){
            //js对应数据的关键字
            String key = lis.get(i).attr("data-value");
            ColorType colorType = new ColorType();
            project.addColor(colorType);
            //TODO 颜色分类详情，和快递费用详情
            //获取连接
            String picurl = lis.get(i).selectFirst("a").attr("style");
            if (picurl != null && picurl.contains(flag)){
                int index = picurl.indexOf(flag) + flag.length();
                picurl = picurl.substring(index,picurl.indexOf(")",index));
                picurl = SystemProperty.PIC_SCHEMA + picurl.substring(0,picurl.lastIndexOf("_"));
                HttpGet get = new HttpGet(picurl);
                String filename = SystemProperty.RIGHT_TOP_IMAGES + i + picurl.substring(picurl.lastIndexOf("."));
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
