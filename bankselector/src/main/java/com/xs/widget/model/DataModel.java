package com.xs.widget.model;

import java.io.Serializable;

/**
 * @date: on 2019-10-30
 * @author: a112233
 * @email: mxnzp_life@163.com
 * @desc: 用户需要传入的数据
 */
public class DataModel implements Serializable {

    private String dataName;
    private Object extra;

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
