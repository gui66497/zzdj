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
     * 需要排除的ip 61是因为北京虚拟机没有固定对应的实体机而造出来的机器
     */
    public static final String[] except_ips = new String[] {"10.1.230.61"};

    /**
     * nmap扫描时间间隔
     */
    public static final int NMAP_SCAN_INTERVAL = 11;

    /**
     * nessus地址
     */
    public static final String NESSUS_IP = "10.1.242.80";

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

    public static float bytes2mb(long bytes) {
        BigDecimal filesize = new BigDecimal(bytes);
        BigDecimal megabyte = new BigDecimal(1024 * 1024);
        float returnValue = filesize.divide(megabyte, 1, BigDecimal.ROUND_UP)
                .floatValue();
        return (returnValue);

    }

    public static String unReglularQuery = "{\n" +
            "  \"size\": 5000,\n" +
            "  \"query\": {\n" +
            "    \"bool\": {\n" +
            "      \"must\": [\n" +
            "        {\n" +
            "          \"bool\": {\n" +
            "            \"minimum_should_match\": 1,\n" +
            "            \"should\": [\n" +
            "              {\n" +
            "                \"match_phrase\": {\n" +
            "                  \"IPV4_DST_ADDR.keyword\": \"10.1.242.87\"\n" +
            "                }\n" +
            "              },\n" +
            "              {\n" +
            "                \"match_phrase\": {\n" +
            "                  \"IPV4_DST_ADDR.keyword\": \"10.1.242.93\"\n" +
            "                }\n" +
            "              },\n" +
            "              {\n" +
            "                \"match_phrase\": {\n" +
            "                  \"IPV4_DST_ADDR.keyword\": \"10.1.242.86\"\n" +
            "                }\n" +
            "              },\n" +
            "              {\n" +
            "                \"match_phrase\": {\n" +
            "                  \"IPV4_DST_ADDR.keyword\": \"10.1.242.95\"\n" +
            "                }\n" +
            "              },\n" +
            "              {\n" +
            "                \"match_phrase\": {\n" +
            "                  \"IPV4_DST_ADDR.keyword\": \"10.1.242.104\"\n" +
            "                }\n" +
            "              },\n" +
            "              {\n" +
            "                \"match_phrase\": {\n" +
            "                  \"IPV4_DST_ADDR.keyword\": \"10.1.242.84\"\n" +
            "                }\n" +
            "              }\n" +
            "            ]\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"match_phrase\": {\n" +
            "            \"L7_PROTO_NAME.keyword\": {\n" +
            "              \"query\": \"HTTP\"\n" +
            "            }\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"bool\": {\n" +
            "            \"should\": [\n" +
            "              {\n" +
            "                \"match_phrase\": {\n" +
            "                  \"L4_DST_PORT\": \"80\"\n" +
            "                }\n" +
            "              },\n" +
            "              {\n" +
            "                \"match_phrase\": {\n" +
            "                  \"L4_DST_PORT\": \"8080\"\n" +
            "                }\n" +
            "              }\n" +
            "            ],\n" +
            "            \"minimum_should_match\": 1\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"range\": {\n" +
            "            \"@timestamp\": {\n" +
            "              \"gte\": 1545741000000,\n" +
            "              \"lte\": 1545742800000,\n" +
            "              \"format\": \"epoch_millis\"\n" +
            "            }\n" +
            "          }\n" +
            "        }\n" +
            "      ],\n" +
            "      \"must_not\": [\n" +
            "        {\n" +
            "          \"bool\": {\n" +
            "            \"minimum_should_match\": 1,\n" +
            "            \"should\": [\n" +
            "              {\n" +
            "                \"match_phrase\": {\n" +
            "                  \"IPV4_SRC_ADDR.keyword\": \"10.1.242.111\"\n" +
            "                }\n" +
            "              },\n" +
            "              {\n" +
            "                \"match_phrase\": {\n" +
            "                  \"IPV4_SRC_ADDR.keyword\": \"10.1.230.159\"\n" +
            "                }\n" +
            "              },\n" +
            "              {\n" +
            "                \"match_phrase\": {\n" +
            "                  \"IPV4_SRC_ADDR.keyword\": \"172.17.0.8\"\n" +
            "                }\n" +
            "              }\n" +
            "            ]\n" +
            "          }\n" +
            "        }\n" +
            "      ]\n" +
            "    }\n" +
            "  },\n" +
            "  \"sort\": [\n" +
            "    {\n" +
            "      \"@timestamp\": {\n" +
            "        \"order\": \"asc\"\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    public static void main(String[] args) {
        System.out.println(bytes2mb(1468774));
    }

}
