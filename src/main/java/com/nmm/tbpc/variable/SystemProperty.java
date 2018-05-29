package com.nmm.tbpc.variable;

/**
 * @author nmm 2018/5/28
 * 系统配置项，不想用配置文件
 * @description
 */
public interface SystemProperty {
    public static final String RES_PATH = "F:/tbpc";
    //顶部左侧图片前缀
    public static final String LEFT_TOP_IMAGES = "leftPics";
    //顶部右侧图片前缀
    public static final String RIGHT_TOP_IMAGES = "rightPics";
    //详情图片前缀
    public static final String DETAIL_IMAGES = "detailPics";
    //图片链接协议
    public static final String PIC_SCHEMA = "http:";
    //图片名称分隔符
    public static final String FILE_SEPORTER = "_";
}
