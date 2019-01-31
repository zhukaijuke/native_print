package com.zhukai.print.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author zhukai
 * @date 2019/1/31
 */
@Getter
@Setter
@ToString
public class RequestModel implements Serializable {

    /**
     * 转发的url
     */
    private String url;

    /**
     * 打印机类型
     */
    private String printerType;

    /**
     * 单据类型
     */
    private String docType;
}
