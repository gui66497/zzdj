package com.zzjz.zzdj.bean;

import java.util.List;

/**
 * @author 梅宏振
 * @version 2015年2月27日 下午6:16:18
 * @ClassName: ResponseEntity
 * @Description: REST接口通用响应类
 * @param <T> 泛型
 */
public class BaseResponse<T> {

    /**
     * 结果状态码
     */
    private ResultCode resultCode;

    /**
     * 数据集
     */
    private List<T> data;

    /**
     * 其它数据
     */
    private List<Object> otherData;

    /**
     * 提示消息
     */
    private String message;

    //单个数据
    private T obj;

    /**
     * @return the resultCode
     */
    public ResultCode getResultCode() {
        return resultCode;
    }

    /**
     * @param resultCode the resultCode to set
     */
    public void setResultCode(ResultCode resultCode) {
        this.resultCode = resultCode;
    }

    /**
     * @return the data
     */
    public List<T> getData() {
        return data;
    }

    /**
     * @param data the data to set
     */
    public void setData(List<T> data) {
        this.data = data;
    }

    /**
     * @return the otherData
     */
    public List<Object> getOtherData() {
        return otherData;
    }

    /**
     * @param otherData the otherData to set
     */
    public void setOtherData(List<Object> otherData) {
        this.otherData = otherData;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 获取单个数据.
     * @return 单个数据
     */
    public T getObj() {
        return obj;
    }

    /**
     * 设置单个数据.
     * @param obj 单个数据
     */
    public void setObj(T obj) {
        this.obj = obj;
    }
}
