package com.nmm.tbpc.po;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author nmm 2018/5/28
 * 产品分类
 * @description
 */
@Data
public class ColorType {
    //颜色分类的价格是和尺码一类属性关联的，属于一对多
    private List<ColorPrice> colorPrices = new ArrayList<>();
    //颜色描述
    private String desc;
    //图片名称
    private String pic;
    //颜色分类的key值
    private String dataValue;

}
