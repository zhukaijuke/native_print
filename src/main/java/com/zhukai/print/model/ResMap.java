package com.zhukai.print.model;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhukai
 * @date 2018/9/20
 */
public class ResMap extends HashMap<String, Object> {

    public ResMap() {
        put("success", true);
    }

    public static ResMap error() {
        return error("系统异常，请联系管理员");
    }

    public static ResMap error(String msg) {
        ResMap r = new ResMap();
        r.put("success", false);
        r.put("msg", msg);
        return r;
    }

    public static ResMap success() {
        return new ResMap();
    }

    public static ResMap success(String msg) {
        ResMap r = new ResMap();
        r.put("msg", msg);
        return r;
    }

    public static ResMap success(Map<String, Object> map) {
        ResMap r = new ResMap();
        r.putAll(map);
        return r;
    }

    public ResMap put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    public boolean isSuccess() {
        Object obj = this.get("success");
        return obj != null && ((boolean) obj);
    }

}
