package com.nmm.test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.nmm.tbpc.po.Project;
import com.nmm.tbpc.service.SourceLoader;
import com.nmm.tbpc.service.SourceLoader1688;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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
        String url = "F:/tbpc/aaa.HTML";
        SourceLoader1688 loader = new SourceLoader1688();
        Project project = loader.loadProject(url);
        System.out.println(project);
    }
    @Test
    public void testloader1688111() throws Exception {
        CookieStore cookieStore = new BasicCookieStore();
        String url = "https://www.1688.com/";
        HttpClient client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
        HttpGet get = new HttpGet(url);
//        HttpResponse response = client.execute(get);
//        List<Cookie> cookies = cookieStore.getCookies();
//        for (Cookie cookie:cookies){
//            System.out.println(cookie.getName() + "==" + cookie.getValue());
//        }
        RequestConfig config = RequestConfig.custom().setRedirectsEnabled(true).setCircularRedirectsAllowed(true).build();

        get = new HttpGet("https://detail.1688.com/offer/43897643886.html?sk=consign?spm=a26239.10650180.jav1iv2w.2.267e21fd2AX79g");
        get.setConfig(config);
        HttpResponse response = client.execute(get);
//       System.out.println(EntityUtils.toString(response.getEntity()));
        System.currentTimeMillis();

        System.out.println(Jsoup.connect("https://detail.1688.com/offer/43897643886.html?sk=consign?spm=a26239.10650180.jav1iv2w.2.267e21fd2AX79g")

                .header("cookie","UM_distinctid=1639d1d7f810-05cb872230b3f9-5d4e211f-e1000-1639d1d7f826f2; cna=gW3FEM8w/10CAWonyBOWdGxV; ali_apache_track=c_mid=b2b-1930052628|c_lid=niemmlhj|c_ms=1; __guid=224050958.3106310940489438700.1527349442142.7827; _uab_collina=152734950294370236904663; h_keys=\"%u7ae5%u978b#%u6c6a%u6c6a%u961f%u7acb%u5927%u529f#%u98de%u817e#%u98de%u817e%u6bcd%u5a74%u73a9%u5177%u5e97\"; ad_prefer=\"2018/05/31 00:26:09\"; last_mid=b2b-1930052628; __last_loginid__=niemmlhj; _cn_slid_=txZco6MWP5; ali_ab=123.168.86.126.1527349364683.4; _csrf_token=1527776619837; JSESSIONID=9L78rXRi1-Q3cZlCcP4AxRBVVHh5-7M0TdtQ-l6ts2; cookie2=1455df4e3a46854330fa7a13dce7f24e; hng=CN%7Czh-CN%7CCNY%7C156; t=cf0d4f2e8fdc05a08a3f62d7c3c1cb9e; _tb_token_=5395e375e7eab; lid=niemmlhj; __cn_logon__=false; _tmp_ck_0=W7DaJXRytORqasDRLSrVOCXx9zjgPq02fzX8mjLRRe84QlfRbDCJ%2FzHyjPP5oiNpRWfqmw9f2lRk0KmDGrMobQFdrYG2RuWRKitoc48aVlVs2ZqZ%2F0iaY6BLCtgfJpWjFOPjtb8zbaCVfs8hEZOnt6N%2BncIbpAgGZMjArwhqAoVa1iGuUyePyyFhMh%2FTEGH6kot8nqtD0OZ%2F9EYo54QVRK5GourB56Bos2gHbGdxMMX5tDu5tnzGbhSswyRpi%2B%2F4l8%2Bw3pGiLiFFIZuBkair0RT0miPrYh8sdhCRTEZ%2FQaS5OPF73qo0IyKIRrctMZQFYwLdOiBSpaJuyArbKopPgQmq3n%2FWrYXGQ4P29ZZ6z8rUJvyBC8RQOySrOFTolIEphwWj8cQLGsY%3D; monitor_count=16; CNZZDATA1253659577=1324365686-1527344352-https%253A%252F%252Flogin.1688.com%252F%7C1527774600; isg=BJKSXcCpHT3_I2FYDD4qUY_X41i0C5RRKuCZnlzrvsUwbzJpRDPmTZiN28vTGA7V; _umdata=6AF5B463492A874D334623CD0B581B7EE17F7BDE079FF92EFA9065EADD3213575B5D15302685F755CD43AD3E795C914C4E83EBEDFA55522991EB3001A39D06D6; alicnweb=homeIdttS%3D67072885881064977700213359696481162507%7ChomeIdttSAction%3Dtrue%7Ctouch_tb_at%3D1527777086248%7Clastlogonid%3Dniemmlhj")
                .cookie("CNZZDATA1253659577","1324365686-1527344352-https%253A%252F%252Flogin.1688.com%252F%7C1527769200")
                .cookie("UM_distinctid","1639d1d7f810-05cb872230b3f9-5d4e211f-e1000-1639d1d7f826f2")
                .cookie("__guid","224050958.3106310940489438700.1527349442142.7827")
                .cookie("__last_loginid__","niemmlhj")
                .cookie("_cn_slid_","txZco6MWP5")
                .cookie("_uab_collina","152734950294370236904663")
                .cookie("_umdata","6AF5B463492A874D334623CD0B581B7EE17F7BDE079FF92EFA9065EADD3213575B5D15302685F755CD43AD3E795C914C4E83EBEDFA55522991EB3001A39D06D6")
                .cookie("ad_prefer","\"2018/05/31 00:26:09\"")
                .cookie("ali_ab","123.168.86.126.1527349364683.4")
                .cookie("ali_apache_track","c_mid=b2b-1930052628|c_lid=niemmlhj|c_ms=1")
                .cookie("alicnweb","homeIdttS%3D67072885881064977700213359696481162507%7ChomeIdttSAction%3Dtrue%7Ctouch_tb_at%3D1527774183371%7Clastlogonid%3Dniemmlhj")
                .cookie("cna","gW3FEM8w/10CAWonyBOWdGxV")
                .cookie("h_keys","\"%u7ae5%u978b#%u6c6a%u6c6a%u961f%u7acb%u5927%u529f#%u98de%u817e#%u98de%u817e%u6bcd%u5a74%u73a9%u5177%u5e97\"")
                .cookie("hng","CN%7Czh-CN%7CCNY%7C156")
                .cookie("isg","BBIS1vkknaKFv-HYjL6q0Q9XY9g0ixTUqmAZHtxroEWw77LpxLNmzRgmW0tTuo5V")
                .cookie("last_mid","b2b-1930052628")
                .cookie("lid","niemmlhj")
                .cookie("monitor_count","17")
                .cookie("t","cf0d4f2e8fdc05a08a3f62d7c3c1cb9e").get());
    }

}
