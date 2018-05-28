package com.nmm.tbpc.po;

import java.util.HashMap;

/**
 * @author nmm 2018/5/28
 * 属性集合
 * @description
 */
public class ProjectPropertyContainer {

    private static HashMap<String,ProjectProperty> projectProperties = new HashMap<String, ProjectProperty>();
    //静态代码块初始化基础属性，用于匹配key值，key值后期替换
    //产品属性，用于页面名称与上货name对应。
    public static final String PRODUCTED_AREA_NAME = "产地";
    public static final String PRODUCTED_AREA_KEY = "productedarea";
    public static final String BRAND_NAME = "品牌";
    public static final String BRAND_KEY = "brand";
    public static final String MODEL_NAME = "型号";
    public static final String MODEL_KEY = "model";
    public static final String ARTICLE_NAME = "货号";
    public static final String ARTICLE_KEY = "article";
    public static final String COLOR_TYPE_NAME = "颜色分类";
    public static final String COLOR_TYPE_KEY = "colorType";
    public static final String TOY_TYPE_NAME = "玩具类型";
    public static final String TOY_TYPE_KEY = "toyType";
    public static final String QUALITY_NAME = "材质";
    public static final String QUALITY_KEY = "quality";
    public static final String AGE_RANGE_NAME = "使用年龄";
    public static final String AGE_RANGE_KEY = "ageRange";
    public static final String SAFE_NAME = "安全等级";
    public static final String SAFE_KEY = "safe";
    public static final String QUALITY_MEMBER_NAME = "材质成分";
    public static final String QUALITY_MEMBER_KEY = "qualityMember";
    public static final String SEX_TYPE_NAME = "适用性别";
    public static final String SEX_TYPE_KEY = "sexType";
    public static final String HIGHT_NAME = "参考身高";
    public static final String HIGHT_KEY = "hight";
    public static final String REASON_NAME = "适用季节";
    public static final String REASON_KEY = "reason";
    public static final String MODEL_PIC_NAME = "模特实拍";
    public static final String MODEL_PIC_KEY = "modelPic";
    public static final String HAS_HAT_NAME = "是否带帽子";
    public static final String HAS_HAT_KEY = "hasHat";
    public static final String STYLE_NAME = "风格";
    public static final String STYLE_KEY = "style";
    public static final String PICTURE_NAME = "图案";
    public static final String PICTURE_KEY = "picture";
    public static final String THICKNESS_NAME = "厚薄";
    public static final String THICKNESS_KEY = "thickness";

    public ProjectPropertyContainer(){
        init();
    }

    private  void init(){
        projectProperties.clear();
        projectProperties.put(PRODUCTED_AREA_NAME,new ProjectProperty(PRODUCTED_AREA_NAME,PRODUCTED_AREA_KEY));
        projectProperties.put(BRAND_NAME,new ProjectProperty(BRAND_NAME,BRAND_KEY));
        projectProperties.put(MODEL_NAME,new ProjectProperty(MODEL_NAME,MODEL_KEY));
        projectProperties.put(ARTICLE_NAME,new ProjectProperty(ARTICLE_NAME,ARTICLE_KEY));
        projectProperties.put(COLOR_TYPE_NAME,new ProjectProperty(COLOR_TYPE_NAME,COLOR_TYPE_KEY));
        projectProperties.put(TOY_TYPE_NAME,new ProjectProperty(TOY_TYPE_NAME,TOY_TYPE_KEY));
        projectProperties.put(QUALITY_NAME,new ProjectProperty(QUALITY_NAME,QUALITY_KEY));
        projectProperties.put(AGE_RANGE_NAME,new ProjectProperty(AGE_RANGE_NAME,AGE_RANGE_KEY));
        projectProperties.put(SAFE_NAME,new ProjectProperty(SAFE_NAME,SAFE_KEY));
        projectProperties.put(QUALITY_MEMBER_NAME,new ProjectProperty(QUALITY_MEMBER_NAME,QUALITY_MEMBER_KEY));
        projectProperties.put(SEX_TYPE_NAME,new ProjectProperty(SEX_TYPE_NAME,SEX_TYPE_KEY));
        projectProperties.put(HIGHT_NAME,new ProjectProperty(HIGHT_NAME,HIGHT_KEY));
        projectProperties.put(REASON_NAME,new ProjectProperty(REASON_NAME,REASON_KEY));
        projectProperties.put(MODEL_PIC_NAME,new ProjectProperty(MODEL_PIC_NAME,MODEL_PIC_KEY));
        projectProperties.put(HAS_HAT_NAME,new ProjectProperty(HAS_HAT_NAME,HAS_HAT_KEY));
        projectProperties.put(STYLE_NAME,new ProjectProperty(STYLE_NAME,STYLE_KEY));
        projectProperties.put(PICTURE_NAME,new ProjectProperty(PICTURE_NAME,PICTURE_KEY));
        projectProperties.put(THICKNESS_NAME,new ProjectProperty(THICKNESS_NAME,THICKNESS_KEY));

    }
    public  ProjectProperty getPropertyByName(String name){
        ProjectProperty projectProperty = projectProperties.get(name);
        if (projectProperty != null){
            return projectProperty.clone();
        }
        return projectProperty;
    }

}
