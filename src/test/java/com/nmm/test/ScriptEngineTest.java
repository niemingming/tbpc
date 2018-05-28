package com.nmm.test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.nmm.tbpc.service.SourceLoader;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.IOException;

/**
 * @author nmm 2018/5/28
 * @description
 */
public class ScriptEngineTest {

    @Test
    public void testScriptEngine() throws Exception {
        String url = "https://item.taobao.com/item.htm?spm=a1z10.3-c.w4002-18396302249.62.6594374au3khN4&id=570316136094";
        //获取页面并加载
        WebClient webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(true);//允许执行js
        webClient.getOptions().setCssEnabled(false);//不允许执行css
        webClient.getOptions().setThrowExceptionOnScriptError(false);//js异常不退出
        webClient.getOptions().setTimeout(10000);//超时时间
        //连接获取
        HtmlPage page = webClient.getPage(url);
        DomElement element = page.getElementById("J_DivItemDesc");
        element.getLastElementChild().focus();
        String xml = page.asXml();
        //通过Jsoup解析页面
        Elements scripts = Jsoup.parse(xml).select("script");
        //获取java js引擎
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine javascript = manager.getEngineByName("javascript");
        for (int i = 0; i < scripts.size(); i++) {
            String content = scripts.get(i).html();
            if (content.contains("Hub = {}")){
                String hubstr = content.substring(content.indexOf("Hub = {}"),content.lastIndexOf("//"));
                String execstr = ";Hub.config.get('sku');";
                ScriptObjectMirror sku = (ScriptObjectMirror) javascript.eval(hubstr + execstr);
                //获取基本信息
                Object itemInfo = sku.get("valItemInfo");
                if (itemInfo != null){
                    //价格信息
                    Object skuMap = ((ScriptObjectMirror)itemInfo).get("skuMap");
                    //提示信息
                    Object propertyMemoMap = ((ScriptObjectMirror)itemInfo).get("propertyMemoMap");

                }

            }
        }
    }
    @Test
    public void testloader() throws Exception {
        String url = "https://item.taobao.com/item.htm?spm=a1z10.3-c.w4002-18396302249.62.6594374au3khN4&id=570316136094";
        SourceLoader loader = new SourceLoader();
        loader.loadProject(url);
    }
}
