package com.zzjz.zzdj.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.zzjz.zzdj.service.ElasticService;
import com.zzjz.zzdj.util.Constant;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.avg.ParsedAvg;
import org.elasticsearch.search.aggregations.metrics.sum.ParsedSum;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram.Bucket;
import java.io.IOException;
import java.util.*;

/**
 * @author 房桂堂
 * @description ElasticController
 * @date 2018/7/27 13:01
 */
@RestController
@RequestMapping("/es")
public class ElasticController {

    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticController.class);

    @Value("${ES_HOST}")
    String esHost;

    @Value("${ES_PORT}")
    int esPort;

    @Value("${ES_METHOD}")
    String esMethod;

    @Autowired
    ElasticService elasticService;

    /**
     * nessus地址(无锡是192.168.1.195,电建是10.1.242.80)
     */
    @Value("${NESSUS_IP}")
    public String nessusIp;

    /**
     * 实时监测数据[电建左一]
     * @param hours 小时数
     * @return 结果
     */
    @RequestMapping(value = "/realMonitor/{hours}", method = RequestMethod.GET)
    public JsonObject realMonitor(@PathVariable("hours") int hours) {
        LOGGER.info("开始调用realMonitor接口,时间参数为" + hours);
        JsonObject bigJson = new JsonObject();
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(esHost, esPort, esMethod)));
        String format = "yyyy-MM-dd HH:mm:ss";
        String oneHourOldTime = DateTime.now().minusHours(1).toString(format);
        String oldTime = DateTime.now().minusHours(hours).toString(format);
        LOGGER.info("查询的起始时间为" + oldTime);
        // 1.入侵检测数量
        /*SearchRequest searchRequest1 = new SearchRequest(Constant.SNORT_INDEX);
        SearchSourceBuilder searchSourceBuilder1 = new SearchSourceBuilder();
        searchSourceBuilder1.size(0);
        searchSourceBuilder1.query(QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("@timestamp")
                        .format(format).gte(oldTime).timeZone("Asia/Shanghai"))
                .mustNot(QueryBuilders.matchPhraseQuery("src.ip",  Constant.NESSUS_IP))
                .mustNot(QueryBuilders.boolQuery()
                        .should(QueryBuilders.matchPhraseQuery("event.sid", "2010516"))
                        .should(QueryBuilders.matchPhraseQuery("event.sid", "2101201"))
                        .should(QueryBuilders.matchPhraseQuery("event.sid", "2100366"))
                        .should(QueryBuilders.matchPhraseQuery("event.sid", "2015898"))
                        .should(QueryBuilders.matchPhraseQuery("event.sid", "2101603"))
                        .should(QueryBuilders.matchPhraseQuery("event.sid", "2002842"))
                        .should(QueryBuilders.matchPhraseQuery("event.sid", "2101776"))
                        .should(QueryBuilders.matchPhraseQuery("event.sid", "2010493"))
                        .should(QueryBuilders.matchPhraseQuery("event.sid", "2018193"))
                ));
        searchRequest1.source(searchSourceBuilder1);
        try {
            SearchResponse searchResponse = client.search(searchRequest1);
            long snortCount = searchResponse.getHits().totalHits;
            bigJson.addProperty("入侵检测", snortCount);
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        int realMachineCount = 0;
        SearchRequest searchRequest1 = new SearchRequest(Constant.SOCSYSTEM_INDEX);
        SearchSourceBuilder searchSourceBuilder1 = new SearchSourceBuilder();
        searchSourceBuilder1.size(500);
        searchSourceBuilder1.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder1.fetchSource(new String[]{"ip", "manager", "dept", "location"}, null);
        searchRequest1.source(searchSourceBuilder1);
        try {
            SearchResponse searchResponse = client.search(searchRequest1);
            Iterator it = searchResponse.getHits().iterator();
            List<String> realPC = new ArrayList<>();
            while (it.hasNext()) {
                SearchHit hit = (SearchHit) it.next();
                String ip = hit.getSourceAsMap().get("ip").toString();
                //过滤掉不需要监控的机器 目前是北京物理机61
                if (! Arrays.asList(Constant.except_ips).contains(ip)) {
                    realPC.add(ip);
                }
            }
            realMachineCount = realPC.size();
            bigJson.addProperty("服务器数量", realMachineCount);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 2.漏洞检测数量
        SearchRequest searchRequest2 = new SearchRequest(Constant.NESSUS_INDEX);
        SearchSourceBuilder searchSourceBuilder2 = new SearchSourceBuilder();
        searchSourceBuilder2.size(0);
        searchSourceBuilder2.query(QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("@timestamp")
                        .format(format).gte(oldTime).timeZone("Asia/Shanghai"))
                .mustNot(QueryBuilders.matchPhraseQuery("ip",  nessusIp))
                .mustNot(QueryBuilders.boolQuery()
                        .should(QueryBuilders.matchPhraseQuery("severity", "0"))
                        .should(QueryBuilders.matchPhraseQuery("severity", "1"))));
        searchRequest2.source(searchSourceBuilder2);
        try {
            SearchResponse searchResponse = client.search(searchRequest2);
            long nessusCount = searchResponse.getHits().totalHits;
            bigJson.addProperty("漏洞扫描", nessusCount);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 3.服务器可用率
        // 通过soc-system和nmap数据得出
        SearchRequest searchRequest3 = new SearchRequest(Constant.SOCSYSTEM_INDEX);
        SearchSourceBuilder searchSourceBuilder3 = new SearchSourceBuilder();
        searchSourceBuilder3.size(500);
        searchSourceBuilder3.query(QueryBuilders.matchAllQuery());
        searchSourceBuilder3.fetchSource(new String[]{"ip", "manager", "dept", "location"}, null);
        searchRequest3.source(searchSourceBuilder3);
        try {
            SearchResponse searchResponse = client.search(searchRequest3);
            long allMachineCount = searchResponse.getHits().totalHits;
            List<String> onlineSocSystem = new ArrayList<>();
            List<String> offlineSocSystem = new ArrayList<>();
            //查询最近一次的nmap记录
            Map<String, Boolean> nmapMap = elasticService.getAllNmap();
            Iterator it = searchResponse.getHits().iterator();
            while (it.hasNext()) {
                SearchHit hit = (SearchHit) it.next();
                String ip = hit.getSourceAsMap().get("ip").toString();
                if (nmapMap.get(ip) != null && nmapMap.get(ip)) {
                    onlineSocSystem.add(ip);
                } else {
                    offlineSocSystem.add(ip);
                }
            }
            LOGGER.info("在线主机有:" + onlineSocSystem.toString());
            LOGGER.info("离线线主机有:" + offlineSocSystem.toString());
            LOGGER.info("在线主机数为:" + onlineSocSystem.size());
            LOGGER.info("总主机数为:" + realMachineCount);
            int res = (int) ((onlineSocSystem.size() * 100) / realMachineCount);
            bigJson.addProperty("服务器可用率", res + "%");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 4.网络总流量
        // 通过ntopng数据得出
        SearchRequest searchRequest4 = new SearchRequest(Constant.NTOPNG_INDEX);
        SearchSourceBuilder searchSourceBuilder4 = new SearchSourceBuilder();
        searchSourceBuilder4.size(0);
        searchSourceBuilder4.query(QueryBuilders.rangeQuery("@timestamp")
                .format(format).gte(oldTime).timeZone("Asia/Shanghai"));
        searchSourceBuilder4.aggregation(AggregationBuilders.sum("inBytes").field("IN_BYTES"));
        searchSourceBuilder4.aggregation(AggregationBuilders.sum("outBytes").field("OUT_BYTES"));
        searchRequest4.source(searchSourceBuilder4);
        try {
            SearchResponse searchResponse = client.search(searchRequest4);
            ParsedSum inParsedSum = searchResponse.getAggregations().get("inBytes");
            String inStr = Constant.readableFileSize((long) inParsedSum.getValue());
            ParsedSum outParsedSum = searchResponse.getAggregations().get("outBytes");
            String outStr = Constant.readableFileSize((long) outParsedSum.getValue());
            long total = (long) (inParsedSum.getValue() + outParsedSum.getValue());
            String totalStr = Constant.readableFileSize(total);
            JsonObject flowObject = new JsonObject();
            flowObject.addProperty("in", inStr);
            flowObject.addProperty("out", outStr);
            flowObject.addProperty("total", totalStr);
            bigJson.add("网络总流量", flowObject);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //5 服务系统数量
        SearchRequest searchRequest5 = new SearchRequest(Constant.HEARTBEAT_INDEX);
        SearchSourceBuilder searchSourceBuilder5 = new SearchSourceBuilder();
        searchSourceBuilder5.size(0);
        searchSourceBuilder5.query(QueryBuilders.rangeQuery("@timestamp")
                .format(format).gte(oneHourOldTime).timeZone("Asia/Shanghai"));
        searchSourceBuilder5.aggregation(AggregationBuilders.terms("serverName").field("server_name").size(50));
        searchRequest5.source(searchSourceBuilder5);
        try {
            SearchResponse searchResponse = client.search(searchRequest5);
            ParsedStringTerms parsedStringTerms = searchResponse.getAggregations().get("serverName");
            int count = parsedStringTerms.getBuckets().size();
            bigJson.addProperty("服务系统数量", count);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        LOGGER.info("realMonitor结果为" + bigJson);
        return bigJson;
    }



    public static void main(String[] args) {
        int hours = 24;
        String format = "yyyy-MM-dd HH:mm:ss";
        String oldTime = DateTime.now().minusHours(hours).toString(format);
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("es1", 9200, "http")));

        SearchRequest searchRequest2 = new SearchRequest(Constant.NESSUS_INDEX);
        SearchSourceBuilder searchSourceBuilder2 = new SearchSourceBuilder();
        searchSourceBuilder2.size(0);
        searchSourceBuilder2.query(QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("@timestamp")
                        .format(format).gte(oldTime).timeZone("Asia/Shanghai"))
                .mustNot(QueryBuilders.matchPhraseQuery("ip",  "192.168.1.195"))
                .mustNot(QueryBuilders.boolQuery()
                        .should(QueryBuilders.matchPhraseQuery("severity", "0"))
                        .should(QueryBuilders.matchPhraseQuery("severity", "1"))));
        searchRequest2.source(searchSourceBuilder2);
        try {
            SearchResponse searchResponse = client.search(searchRequest2);
            long nessusCount = searchResponse.getHits().totalHits;
            System.out.println(1);
            //bigJson.addProperty("漏洞扫描", nessusCount);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * 服务响应时间趋势图[电建左2]
     * @param hours 小时数(正整数)
     * @return 结果
     */
    @RequestMapping(value = "/serverResponseTrend/{hours}", method = RequestMethod.GET)
    public JsonObject serverResponseTrend(@PathVariable("hours") int hours) {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(esHost, esPort, esMethod)));
        String format = "yyyy-MM-dd HH:mm:ss";
        String oldTime = DateTime.now().minusHours(hours).toString(format);
        LOGGER.info("开始调用serverResponseTrend服务响应时间趋势");
        LOGGER.info("查询的起始时间为" + oldTime);
        //分组时间间隔为 6分钟 * hours
        int interval = 6 * hours;
        SearchRequest searchRequest = new SearchRequest(Constant.HEARTBEAT_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("@timestamp")
                        .format(format).gte(oldTime).timeZone("Asia/Shanghai")));
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(AggregationBuilders.dateHistogram("times").field("@timestamp")
                .timeZone(DateTimeZone.forID("Asia/Shanghai")).dateHistogramInterval(DateHistogramInterval.minutes(interval)).minDocCount(1)
                .subAggregation(AggregationBuilders.terms("serverName").field("server_name").size(50).order(BucketOrder.aggregation("duration", false))
                        .subAggregation(AggregationBuilders.avg("duration").field("monitor.duration.us"))));
        searchRequest.source(searchSourceBuilder);
        try {
            SearchResponse searchResponse = client.search(searchRequest);
            ParsedDateHistogram timesTerms = searchResponse.getAggregations().get("times");
            List<? extends Bucket> timesBuckets = timesTerms.getBuckets();

            JsonObject finalJson = new JsonObject();
            JsonArray timeArray = new JsonArray();
            Set<String> serverNameList = new HashSet<>();
            LinkedList<Map<String, Double>> linkedList = new LinkedList<>();
            for (Bucket bucket : timesBuckets) {
                //外层times时间数组循环
                DateTime dateTime = new DateTime(bucket.getKeyAsString());
                String dateTimeStr = dateTime.toString(format);
                timeArray.add(dateTimeStr);
                ParsedStringTerms serverNameTerms = bucket.getAggregations().get("serverName");
                List<? extends Terms.Bucket> serverNameBuckets = serverNameTerms.getBuckets();
                Map<String, Double> oneMap = new HashMap<>();
                for (Terms.Bucket serverNameBucket : serverNameBuckets) {
                    //内层各时间下不同服务的响应时间循环
                    String serverName = serverNameBucket.getKey().toString();
                    serverNameList.add(serverName);
                    ParsedAvg parsedAvg = serverNameBucket.getAggregations().get("duration");
                    oneMap.put(serverName, parsedAvg.getValue());
                }
                linkedList.add(oneMap);
            }

            JsonArray dataArray = new JsonArray();
            for (String serverName : serverNameList) {
                JsonObject oneJson = new JsonObject();
                oneJson.addProperty("serverName", serverName);
                JsonArray durationArray = new JsonArray();
                for (Map<String, Double> map : linkedList) {
                    //转为int 去掉小数点
                    if (map.get(serverName) != null) {
                        durationArray.add(map.get(serverName).intValue());
                    } else {
                        durationArray.add(0);
                    }
                }
                oneJson.add("datas", durationArray);
                dataArray.add(oneJson);
            }
            finalJson.add("timeArr", timeArray);
            finalJson.add("timeData", dataArray);
            return finalJson;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * (下载)流量趋势图[电建右2]
     * @param hours 小时数(正整数)
     * @return 结果
     */
    @RequestMapping(value = "/flowTrend/{hours}", method = RequestMethod.GET)
    public JsonObject flowTrend(@PathVariable("hours") int hours) {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(esHost, esPort, esMethod)));
        String format = "yyyy-MM-dd HH:mm:ss";
        String oldTime = DateTime.now().minusHours(hours).toString(format);
        LOGGER.info("开始调用flowTrend流量趋势");
        LOGGER.info("查询的起始时间为" + oldTime);
        //分组时间间隔为 6分钟 * hours
        int interval = 6 * hours;
        SearchRequest searchRequest = new SearchRequest(Constant.NTOPNG_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("@timestamp")
                        .format(format).gte(oldTime).timeZone("Asia/Shanghai")));
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(AggregationBuilders.dateHistogram("times").field("@timestamp")
                .timeZone(DateTimeZone.forID("Asia/Shanghai")).dateHistogramInterval(DateHistogramInterval.minutes(interval)).minDocCount(1)
                .subAggregation(AggregationBuilders.sum("outBytes").field("OUT_BYTES")));
        searchRequest.source(searchSourceBuilder);
        JsonObject finalJson = new JsonObject();
        try {
            SearchResponse searchResponse = client.search(searchRequest);
            JsonArray timeArray = new JsonArray();
            JsonArray valueArray = new JsonArray();
            ParsedDateHistogram timesTerms = searchResponse.getAggregations().get("times");
            List<? extends Bucket> timesBuckets = timesTerms.getBuckets();
            for (Bucket bucket : timesBuckets) {
                DateTime dateTime = new DateTime(bucket.getKeyAsString());
                String dateTimeStr = dateTime.toString(format);
                timeArray.add(dateTimeStr);
                ParsedSum parsedSum = bucket.getAggregations().get("outBytes");
                float value = Constant.bytes2mb((long) parsedSum.getValue());
                valueArray.add(value);
            }
            finalJson.add("timeArr", timeArray);
            finalJson.add("timeData", valueArray);
            return finalJson;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }



}
