package com.zzjz.zzdj.util;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * @author 房桂堂
 * @description Constant
 * @date 2018/7/31 14:43
 */
@Component
public class Constant {

    public static final String HEARTBEAT_INDEX = "heartbeat-*";

    public static final String NTOPNG_INDEX = "ntopng-*";

    public static final String SNORT_INDEX = "snort-*";

    public static final String NESSUS_INDEX = "nessus-*";

    public static final String SOCSYSTEM_INDEX = "soc-system";

    public static final String NMAP_INDEX = "nmap-logstash-*";

    public static final String DHCP_INDEX = "dhcp-*";

    public static final String EVENTLOG_INDEX = "eventlog_2*";

    public static final String SERVICEERROR_INDEX = "service_error-*";

    /**
     * 需要排除的ip 61是因为北京虚拟机没有固定对应的实体机而造出来的机器
     */
    public static final String[] except_ips = new String[] {"10.1.230.61"};

    /**
     * nmap扫描时间间隔
     */
    public static final int NMAP_SCAN_INTERVAL = 11;

    /**
     * 将bytes转为可读
     * @param size bytes
     * @return str
     */
    public static String readableFileSize(long size) {
        if (size <= 0) {
            return "0";
        }
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.0#").format(size / Math.pow(1024, digitGroups)) + units[digitGroups];
    }

    /**
     * byte(字节)根据长度转成kb(千字节)和mb(兆字节)或gb
     * @param bytes 字节
     * @return String
     */
    @Deprecated
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

    public static final String unReglularQuery = "{\n" +
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
            "                  \"IPV4_DST_ADDR\": \"10.1.242.87\"\n" +
            "                }\n" +
            "              },\n" +
            "              {\n" +
            "                \"match_phrase\": {\n" +
            "                  \"IPV4_DST_ADDR\": \"10.1.242.93\"\n" +
            "                }\n" +
            "              },\n" +
            "              {\n" +
            "                \"match_phrase\": {\n" +
            "                  \"IPV4_DST_ADDR\": \"10.1.242.86\"\n" +
            "                }\n" +
            "              },\n" +
            "              {\n" +
            "                \"match_phrase\": {\n" +
            "                  \"IPV4_DST_ADDR\": \"10.1.242.95\"\n" +
            "                }\n" +
            "              },\n" +
            "              {\n" +
            "                \"match_phrase\": {\n" +
            "                  \"IPV4_DST_ADDR\": \"10.1.242.104\"\n" +
            "                }\n" +
            "              },\n" +
            "              {\n" +
            "                \"match_phrase\": {\n" +
            "                  \"IPV4_DST_ADDR\": \"10.1.242.84\"\n" +
            "                }\n" +
            "              }\n" +
            "            ]\n" +
            "          }\n" +
            "        },\n" +
            "        {\n" +
            "          \"match_phrase\": {\n" +
            "            \"L7_PROTO_NAME\": {\n" +
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
            "              \"gte\": \"now-12h\",\n" +
            "              \"lte\": \"now\"\n" +
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
            "                  \"IPV4_SRC_ADDR\": \"10.1.242.111\"\n" +
            "                }\n" +
            "              },\n" +
            "              {\n" +
            "                \"match_phrase\": {\n" +
            "                  \"IPV4_SRC_ADDR\": \"10.1.230.159\"\n" +
            "                }\n" +
            "              },\n" +
            "              {\n" +
            "                \"match_phrase\": {\n" +
            "                  \"IPV4_SRC_ADDR\": \"172.17.0.8\"\n" +
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
        System.out.println(bytes2kb(1468774000000L));
        System.out.println(readableFileSize(1468774000000L));
    }

}
