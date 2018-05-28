package com.nmm.tbpc.po;

import lombok.Data;

/**
 * @author nmm 2018/5/28
 * 产品属性，为了从页面对应到字段key
 * @description
 */
@Data
public class ProjectProperty {
    private String name;
    private String key;
    private String value;
    public  ProjectProperty(){
    }
    public ProjectProperty(String name){
        this.name = name;
    }
    public ProjectProperty(String name,String key){
        this.name = name;
        this.key = key;
    }
    public ProjectProperty(String name,String key,String value){
        this.name = name;
        this.key = key;
        this.value = value;
    }

    /**
     * 克隆自身
     * @return
     */
    public ProjectProperty clone(){
        return new ProjectProperty(name,key,value);
    }
}
