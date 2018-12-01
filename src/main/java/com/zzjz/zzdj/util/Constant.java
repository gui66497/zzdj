package com.zzjz.zzdj.util;

/**
 * @author 房桂堂
 * @description Constant
 * @date 2018/7/31 14:43
 */
public class Constant {

    /**
     * Elasticsearch的ip
     */
    public static final String ES_HOST = "es1";

    /**
     * Elasticsearch的rest端口
     */
    public static final int ES_PORT = 9200;

    /**
     * Elasticsearch的rest端口
     */
    public static final String ES_METHOD = "http";

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


}
