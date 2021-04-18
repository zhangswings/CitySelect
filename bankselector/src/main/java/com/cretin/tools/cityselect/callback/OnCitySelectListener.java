package com.cretin.tools.cityselect.callback;

import com.cretin.tools.cityselect.model.DataModel;

/**
 * @date: on 2019-10-30
 * @author: a112233
 * @email: mxnzp_life@163.com
 * @desc: 添加描述
 */
public interface OnCitySelectListener {

    /**
     * 选择Item
     *
     * @param dataModel
     */
    void onCitySelect(DataModel dataModel);

    /**
     * 选择取消
     */
    void onSelectCancel();

}
