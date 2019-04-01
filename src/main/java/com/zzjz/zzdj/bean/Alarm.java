package com.zzjz.zzdj.bean;

import java.util.Objects;

/**
 * @author 房桂堂
 * @description 报警实体
 * @date 2019/3/22 17:12
 */
public class Alarm {

    /**
     * 报警信息
     */
    private String msg;

    /**
     * 是否新来的
     */
    private boolean isNew;

    /**
     * 报警次数
     */
    private int count = 2;

    public Alarm(String msg, boolean isNew) {
        this.msg = msg;
        this.isNew = isNew;
    }

    public Alarm(String msg, boolean isNew, int count) {
        this.msg = msg;
        this.isNew = isNew;
        this.count = count;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Alarm) {
            Alarm alarm = (Alarm) object;
            return Objects.equals(this.msg, alarm.getMsg());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(msg);
    }
}
