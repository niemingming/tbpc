package com.nmm.tbpc.service;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.nmm.tbpc.po.Project;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * @author nmm 2018/5/28
 * @description
 */
@Service
public class SourceLoader {

    private String resPath;

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
        String str = page.asXml();
        //创建解析器
        return Jsoup.parse(str);
    }

    /**
     * 加载项目信息
     * @param url
     * @return
     */
    public Project loadProject(String url) throws IOException {
        return loadProject(loadDocument(url));
    }

    /**
     * 加载项目信息
     * @param document
     * @return
     */
    public Project loadProject(Document document){
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
        return project;
    }

    /**
     * 下载左侧顶部图片
     * @param file
     * @param document
     */
    private void loadLeftTopPics(File file, Document document) {
        Elements imgs = document.selectFirst(".tb-item-info-l #J_UlThumb").select("img");

    }

    public String getResPath() {
        return resPath;
    }
    public void setResPath(String resPath) {
        this.resPath = resPath;
    }
}
