package com.nmm.tbpc;

import com.nmm.tbpc.po.Project;
import com.nmm.tbpc.service.SourceLoader1688;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
public class MainClass {

    public static void main(String[]args) throws Exception {
        SpringApplication.run(MainClass.class);
        String url = "https://detail.1688.com/offer/567816566120.html?spm=b26110380.sw1688.mof001.26.19a952eahjNYdv";
        SourceLoader1688 loader = new SourceLoader1688();
        Project project = loader.loadProject(url);
        System.out.println(project);
    }

}
