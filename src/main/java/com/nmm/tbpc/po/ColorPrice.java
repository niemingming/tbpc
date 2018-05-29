package com.nmm.tbpc.po;

import lombok.Data;

/**
 * 尺寸价格
 * @author nmm 2018/5/29
 * @description
 */
@Data
public class ColorPrice {
    //默认尺寸，如果是默认尺寸，表示该分类下没有区分。
    public static final String DEFAULT_STYLE = "default";
    //价格
    private double price;
    //尺寸
    private String style = DEFAULT_STYLE;
}
