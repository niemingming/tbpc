package com.nmm.test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.nmm.tbpc.po.Project;
import com.nmm.tbpc.service.SourceLoader;
import com.nmm.tbpc.service.SourceLoader1688;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.junit.Test;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.List;

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
        String url = "https://item.taobao.com/item.htm?spm=a21bz.7725273.1998564503.1.20d63db8fVmZS2&id=565402344300&umpChannel=qianggou&u_channel=qianggou";
        SourceLoader loader = new SourceLoader();
        Project project = loader.loadProject(url);
        System.out.println(project);
    }
    @Test
    public void testloader1688() throws Exception {
        String url = "https://detail.1688.com/offer/549425047212.html?spm=a261y.7663282.0.0.70383e4cdSGsN9";
        SourceLoader1688 loader = new SourceLoader1688();
        Project project = loader.loadProject(url);
        System.out.println(project);
    }
    @Test
    public void testloader1688111() throws Exception {
        CookieStore cookieStore = new BasicCookieStore();
        String url = "https://detail.1688.com/offer/549425047212.html?spm=a261y.7663282.0.0.70383e4cdSGsN9";
        HttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
        HttpGet get = new HttpGet(url);
        HttpResponse response = client.execute(get);
        List<Cookie> cookies = cookieStore.getCookies();
        for (Cookie cookie:cookies){
            System.out.println(cookie.getName() + "==" + cookie.getValue());
        }
        System.out.println(Jsoup.connect("https://detail.1688.com/offer/549425047212.html?spm=a261y.7663282.0.0.70383e4cdSGsN9")
                .get());
        System.currentTimeMillis();
    }

}
