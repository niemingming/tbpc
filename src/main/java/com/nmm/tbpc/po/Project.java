package com.nmm.tbpc.po;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nmm 2018/5/28
 * 产品对象 productor
 * @description
 */
@Data
public class Project {
    //产品名称
    private String projectName;
    //缓存图片地址
    private String cachePath;
    //原页面地址
    private String sourcePath;
    private Logger logger = LoggerFactory.getLogger(Project.class);
    //属性集合
    private ProjectPropertyContainer propertyContainer = new ProjectPropertyContainer();
    //属性集合，用于获取属性
    private List<ProjectProperty> projectProperties = new ArrayList<ProjectProperty>();
    //价格属性图
    private List<ColorType> colorTypes = new ArrayList<ColorType>();
    //添加属性
    public void addProperty(String name ,String value){
        ProjectProperty projectProperty = propertyContainer.getPropertyByName(name);
        if (projectProperty != null) {
            projectProperty.setValue(value);
            projectProperties.add(projectProperty);
        }else {
            logger.info("属性{}，未配置，无法匹配。",name);
        }
    }

    public void addColor(ColorType colorType){
        colorTypes.add(colorType);
    }

}
