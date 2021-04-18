package com.xs.widget.model;

import java.io.Serializable;

/**
 * @author author
 * @desc: 用户需要传入的数据
 */
public class DataModel implements Serializable {

    private String dataName;
    private Object extra;

    public String bankName;
    public String cardNo;
    public String cardType;
    public String parentBankNo;

    public DataModel() {
    }

    public DataModel(String dataName, Object extra) {
        this.dataName = dataName;
        this.extra = extra;
    }

    public String getDataName() {
        return dataName;
    }

    public void setDataName(String dataName) {
        this.dataName = dataName;
    }

    public Object getExtra() {
        return extra;
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }


}
