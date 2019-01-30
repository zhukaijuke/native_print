package com.zhukai.print.listener;

import com.zhukai.print.dao.CommonDao;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import lombok.extern.slf4j.Slf4j;

/**
 * ChoiceBox¼àÌý
 *
 * @author zhukai
 * @date 2019/1/30
 */
@Slf4j
public class MyChangeListener<T> implements ChangeListener<T> {

    private String sysCode;

    public MyChangeListener(String sysCode) {
        this.sysCode = sysCode;
    }

    @Override
    public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
        if (newValue == null) {
            return;
        }
        CommonDao.updateSysConfig(sysCode, newValue.toString());
    }

}
