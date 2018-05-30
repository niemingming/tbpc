package com.nmm.tbpc.variable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
    //省份编码
    // 11 北京市；12 天津市；13 河北省；14 山西省；15 内蒙古自治区；21 辽宁省；22 吉林省；23 黑龙江省；31 上海市；
    // 32 江苏省；33 浙江省；34 安徽省；35 福建省；36 江西省；37 山东省；41 河南省；42 湖北省；43 湖南省；44 广东省；
    // 45 广西壮族自治区；46 海南省；50 重庆市；51 四川省；52 贵州省；53 云南省；54 西 藏 自治区；61 陕西省；62 甘肃省；63 青海省；
    // 64 宁夏回族自治区；65 新 疆 维吾尔自治区；71 台湾省；81 香港特别行政区；82 澳门特别行政区
    public static final List<String> AREA_IDS = Arrays.asList("110000","120000","130000","140000","150000","210000","220000","230000",
            "310000","320000","330000","340000","350000","360000","370000","410000","420000","430000","440000",
            "450000","460000","500000","510000","520000","530000","540000","610000","620000","630000",
            "640000","650000","710000","810000","820000");
}
