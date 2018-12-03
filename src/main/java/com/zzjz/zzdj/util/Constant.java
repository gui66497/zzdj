package com.zzjz.zzdj.util;

import java.math.BigDecimal;

/**
 * @author 房桂堂
 * @description Constant
 * @date 2018/7/31 14:43
 */
public class Constant {

    public static final String HEARTBEAT_INDEX = "heartbeat-*";

    public static final String NTOPNG_INDEX = "ntopng-*";

    public static final String SNORT_INDEX = "snort-*";

    public static final String NESSUS_INDEX = "nessus-*";

    public static final String SOCSYSTEM_INDEX = "soc-system";

    public static final String NMAP_INDEX = "nmap-logstash-*";

    /**
     * nmap扫描时间间隔
     */
    public static final int NMAP_SCAN_INTERVAL = 10;

    /**
     * nessus地址
     */
    public static final String NESSUS_IP = "192.168.1.195";

    /**
     * byte(字节)根据长度转成kb(千字节)和mb(兆字节)或gb
     * @param bytes 字节
     * @return String
     */
    public static String bytes2kb(long bytes) {
        BigDecimal filesize = new BigDecimal(bytes);
        BigDecimal gigabyte = new BigDecimal(1024 * 1024 * 1024);
        float returnValue = filesize.divide(gigabyte, 2, BigDecimal.ROUND_UP)
                .floatValue();
        if (returnValue > 1) {
            return (returnValue + "GB");
        }
        BigDecimal megabyte = new BigDecimal(1024 * 1024);
        returnValue = filesize.divide(megabyte, 2, BigDecimal.ROUND_UP)
                .floatValue();
        if (returnValue > 1) {
            return (returnValue + "MB");
        }
        BigDecimal kilobyte = new BigDecimal(1024);
        returnValue = filesize.divide(kilobyte, 2, BigDecimal.ROUND_UP)
                .floatValue();
        return (returnValue + "KB");
    }


}
