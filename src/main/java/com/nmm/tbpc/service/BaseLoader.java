package com.nmm.tbpc.service;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.nmm.tbpc.po.Project;
import com.nmm.tbpc.variable.SystemProperty;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * 加载资源基础类，用于处理淘宝、1688的共性函数
 */
public class BaseLoader {

    protected String resPath = SystemProperty.RES_PATH;
    protected Logger logger = LoggerFactory.getLogger(BaseLoader.class);
    protected boolean shouldExecScript = true;

    public BaseLoader(){}
    public BaseLoader(boolean shouldExecScript){
        this.shouldExecScript = shouldExecScript;
    }
    /**
     * 加载目标资源，返回document对象
     * @param url
     * @return
     */
    public Document loadDocument(String url) throws IOException {
        if (!shouldExecScript){
            return Jsoup.connect(url).get();
        }
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

        //等待js执行
        webClient.waitForBackgroundJavaScript(10000);

        String str = page.asXml();
        System.out.println(str);
        //创建解析器
        return Jsoup.parse(str);
    }
    /**
     * 加载项目信息
     * @param url
     * @return
     */
    public Project loadProject(String url) throws Exception {
        Project project = loadProject(loadDocument(url));
        project.setSourcePath(url);
        return project;
    }
    public Project loadProject(Document document) throws Exception {
        Project project = new Project();
        String projectName = selectProjectName(document);
        project.setProjectName(projectName);
        String cachePath = resPath + "/" + projectName;
        File file = new File(cachePath);
        //创建缓存目录
        if (!file.exists()&& !file.isDirectory()){
            file.mkdirs();
        }
        project.setCachePath(cachePath);
        return project;
    }

    protected String selectProjectName(Document document) {
        return null;
    }

    protected void loadPicture(File file, String filename, String picurl) throws IOException {
        HttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(picurl);
        FileOutputStream fos = new FileOutputStream(new File(file,filename));
        get = new HttpGet(picurl);
        client.execute(get).getEntity().writeTo(fos);
        fos.flush();
        fos.close();
        get.abort();
    }
}
