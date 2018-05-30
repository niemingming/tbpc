package com.nmm.tbpc.po;

import lombok.Data;

/**
 * 运费实体到省份
 */
@Data
public class FreightCast {
    private String areaId;
    private String areaName;
    //运输费用
    private double cast;
}
