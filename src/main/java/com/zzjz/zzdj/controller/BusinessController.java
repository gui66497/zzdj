package com.zzjz.zzdj.controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.zzjz.zzdj.bean.Alarm;
import com.zzjz.zzdj.bean.Business;
import com.zzjz.zzdj.service.BusinessService;
import com.zzjz.zzdj.service.ElasticService;
import com.zzjz.zzdj.util.AutoRunner;
import com.zzjz.zzdj.util.Constant;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.avg.ParsedAvg;
import org.elasticsearch.search.aggregations.metrics.cardinality.ParsedCardinality;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * @author 房桂堂
 * @description BusinessController
 * @date 2019/1/18 9:05
 */
@RestController
@RequestMapping("/bs")
public class BusinessController {

    private final static Logger LOGGER = LoggerFactory.getLogger(BusinessController.class);

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Autowired
    ElasticService elasticService;

    @Autowired
    BusinessService businessService;

    @Value("${spring.profiles.active}")
    String active;

    String format = "yyyy-MM-dd HH:mm:ss";

    /**
     * 根据时间获取指定ip对应的mac(先从ntop的ip和mac对应数据中查找[服务器网段]，如果没有再从dhcp数据中找[办公区网段])
     * @param ip ip
     * @param time 时间(yyyy-MM-dd HH:mm:ss)
     * @return mac地址
     */
    @RequestMapping(value = "/getMacByIp", method = RequestMethod.GET)
    public String getMacByIp(@RequestParam(value = "ip") String ip, @RequestParam(value = "time", required = false) String time) {
        LOGGER.info("开始调用getMacByIp接口,ip参数为:" + ip + ",time参数为:" + time);
        if (StringUtils.isBlank(ip)) {
            throw new IllegalArgumentException("lost ip argument");
        }
        if (AutoRunner.ipMacMap.containsKey(ip)) {
            LOGGER.info("直接在ipMacMap中找到了对应mac");
            return AutoRunner.ipMacMap.get(ip);
        }

        LOGGER.info("没有再ipMacMap中找到对应mac，开始查找dhcp数据");
        //查询指定时间过去30天之内指定ip最新的一条mac记录
        //若没有指定时间 则默认当前时间
        DateTime nowTime;
        try {
            nowTime = StringUtils.isBlank(time) ? new DateTime() : new DateTime(time);
        } catch (Exception e) {
            e.printStackTrace();
            nowTime = new DateTime();
        }
        DateTime oldTime = nowTime.minusDays(30);
        SearchRequest searchRequest = new SearchRequest(Constant.DHCP_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("@timestamp")
                        .gte(oldTime.toString(format)).lte(nowTime.toString(format)).format(format))
                .must(QueryBuilders.matchPhraseQuery("ip", ip)));
        searchSourceBuilder.size(1);
        searchRequest.source(searchSourceBuilder);
        String mac = "";
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            if (searchResponse.getHits().totalHits > 0) {
                mac = (String) searchResponse.getHits().getAt(0).getSourceAsMap().get("mac");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        LOGGER.info("getMacByIp结果为:" + mac);
        return mac;
    }

    /**
     * 获取所有业务和其下ip
     * @return 业务信息
     * @throws FileNotFoundException FileNotFoundException
     */
    @RequestMapping(value = "/getAllBusiness", method = RequestMethod.GET)
    public List<Business> getAllBusiness() throws FileNotFoundException {
        return businessService.getAllBusiness();
    }

    /**
     * 实时监测数据[电建2期中间]
     * @param businessName 业务名称
     * @param hours 小时数
     * @return 结果
     */
    @RequestMapping(value = "/realMonitor/{hours}", method = RequestMethod.GET)
    public JsonObject realMonitor(@RequestParam("businessName") String businessName, @PathVariable("hours") int hours) {
        LOGGER.info("开始调用realMonitor接口,时间参数为:" + hours + " 业务名为:" + businessName);
        JsonObject bigJson = new JsonObject();
        String oldTime = DateTime.now().minusHours(hours).toString(format);
        LOGGER.info("查询的起始时间为" + oldTime);

        // 1.业务平均响应时间
        SearchRequest searchRequest = new SearchRequest(Constant.HEARTBEAT_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("@timestamp")
                        .format(format).gte(oldTime).timeZone("Asia/Shanghai"))
                .must(QueryBuilders.matchPhraseQuery("server_name", businessName)));
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(AggregationBuilders.avg("duration").field("monitor.duration.us"));
        searchRequest.source(searchSourceBuilder);
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            ParsedAvg parsedAvg = searchResponse.getAggregations().get("duration");
            double microDuration = parsedAvg.getValue();
            if (Double.isInfinite(microDuration)) {
                microDuration = 0;
            }
            int millsDuration = (int) (microDuration / 1000);
            bigJson.addProperty("响应时间", millsDuration + "ms");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 2.应用流量(包括输入输出) 可以通过原总流量增加ip限制得到
        // 通过ntopng数据得出
        SearchRequest searchRequest4 = new SearchRequest(Constant.NTOPNG_INDEX);
        SearchSourceBuilder searchSourceBuilder4 = new SearchSourceBuilder();
        searchSourceBuilder4.size(0);
        searchSourceBuilder4.query(QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("@timestamp")
                        .format(format).gte(oldTime).timeZone("Asia/Shanghai"))
                .must(getBoolQueryByBsName(businessName, "IPV4_DST_ADDR")));
        searchSourceBuilder4.aggregation(AggregationBuilders.sum("inBytes").field("IN_BYTES"));
        searchSourceBuilder4.aggregation(AggregationBuilders.sum("outBytes").field("OUT_BYTES"));
        searchRequest4.source(searchSourceBuilder4);
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest4);
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
            bigJson.add("今日流量", flowObject);

        } catch (IOException e) {
            e.printStackTrace();
        }

        //3.访客数
        SearchRequest searchRequest1 = new SearchRequest(Constant.NTOPNG_INDEX);
        SearchSourceBuilder searchSourceBuilder1 = new SearchSourceBuilder();
        searchSourceBuilder1.size(0);
        searchSourceBuilder1.query(QueryBuilders.boolQuery()
                        .must(QueryBuilders.rangeQuery("@timestamp")
                                .format(format).gte(oldTime).timeZone("Asia/Shanghai"))
                        .must(getBoolQueryByBsName(businessName, "IPV4_DST_ADDR")));
        searchSourceBuilder1.aggregation(AggregationBuilders.cardinality("IPV4").field("IPV4_SRC_ADDR"));
        searchRequest1.source(searchSourceBuilder1);
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest1);
            ParsedCardinality parsedCardinality = searchResponse.getAggregations().get("IPV4");
            bigJson.addProperty("访客数", parsedCardinality.getValue());
        } catch (IOException e) {
            e.printStackTrace();
        }

        //4.服务器数量
        List<String> ips = businessService.getIpListByName(businessName);
        bigJson.addProperty("服务器数量", ips.size());

        LOGGER.info("realMonitor结果为" + bigJson);
        return bigJson;
    }

    /**
     * 指定业务服务器报警次数Top5[电建2期左1]
     * @param hours 小时数(正整数)
     * @return 结果
     */
    @RequestMapping(value = "/alertsCount/{hours}", method = RequestMethod.GET)
    public JsonObject alertsCount(@RequestParam("businessName") String businessName, @PathVariable("hours") int hours) {
        String oldTime = DateTime.now().minusHours(hours).toString(format);
        LOGGER.info("开始调用alertsCount指定业务访问次数Top5,业务名为" + businessName);
        LOGGER.info("查询的起始时间为" + oldTime);
        SearchRequest searchRequest = new SearchRequest(Constant.EVENTLOG_INDEX);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("@timestamp")
                        .format(format).gte(oldTime).timeZone("Asia/Shanghai"))
                .must(getBoolQueryByBsName(businessName, "assetIP.keyword")));
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(AggregationBuilders.terms("assetIP")
                .size(5).field("assetIP.keyword").order(BucketOrder.aggregation("_count", false)));
        searchRequest.source(searchSourceBuilder);

        JsonObject finalJson = new JsonObject();
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            JsonArray keyArray = new JsonArray();
            JsonArray valueArray = new JsonArray();
            ParsedStringTerms parsedStringTerms = searchResponse.getAggregations().get("assetIP");
            List<? extends Terms.Bucket> termsBuckets = parsedStringTerms.getBuckets();
            for (Terms.Bucket bucket : termsBuckets) {
                keyArray.add(bucket.getKeyAsString());
                long value = bucket.getDocCount();
                valueArray.add(value);
            }
            finalJson.add("keyArr", keyArray);
            finalJson.add("keyData", valueArray);
            LOGGER.info("结果为" + finalJson);
            return finalJson;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 指定业务(下载)流量趋势图[电建2期右1]
     * @param hours 小时数(正整数)
     * @param businessName 业务名
     * @return 结果
     */
    @RequestMapping(value = "/flowTrend/{hours}", method = RequestMethod.GET)
    public JsonObject flowTrend(@RequestParam String businessName, @PathVariable("hours") int hours) {
        String oldTime = DateTime.now().minusHours(hours).toString(format);
        LOGGER.info("开始调用指定业务flowTrend流量趋势,业务名为" + businessName);
        LOGGER.info("查询的起始时间为" + oldTime);
        //分组时间间隔为 6分钟 * hours
        int interval = 6 * hours;
        SearchRequest searchRequest = new SearchRequest(Constant.NTOPNG_INDEX);
        //构建查询语句 start
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("@timestamp")
                        .format(format).gte(oldTime).timeZone("Asia/Shanghai"))
                .must(getBoolQueryByBsName(businessName, "IPV4_DST_ADDR")));
        //构建查询语句 end
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(AggregationBuilders.dateHistogram("times").field("@timestamp")
                .timeZone(DateTimeZone.forID("Asia/Shanghai")).dateHistogramInterval(DateHistogramInterval.minutes(interval)).minDocCount(1)
                .subAggregation(AggregationBuilders.sum("outBytes").field("OUT_BYTES")));
        searchRequest.source(searchSourceBuilder);
        JsonObject finalJson = new JsonObject();
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            JsonArray timeArray = new JsonArray();
            JsonArray valueArray = new JsonArray();
            ParsedDateHistogram timesTerms = searchResponse.getAggregations().get("times");
            List<? extends Histogram.Bucket> timesBuckets = timesTerms.getBuckets();
            for (Histogram.Bucket bucket : timesBuckets) {
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
        }
        return null;
    }

    /**
     * 指定业务系统访问次数Top5[电建2期右2]
     * @param hours 小时数(正整数)
     * @return 结果
     */
    @RequestMapping(value = "/businessVisitCounts/{hours}", method = RequestMethod.GET)
    public JsonObject businessVisitCounts(@RequestParam("businessName") String businessName, @PathVariable("hours") int hours) {
        String oldTime = DateTime.now().minusHours(hours).toString(format);
        LOGGER.info("开始调用businessVisitCounts指定业务访问次数Top5,业务名为" + businessName);
        LOGGER.info("查询的起始时间为" + oldTime);
        SearchRequest searchRequest = new SearchRequest(Constant.NTOPNG_INDEX);

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("@timestamp")
                        .format(format).gte(oldTime).timeZone("Asia/Shanghai"))
                .must(getBoolQueryByBsName(businessName, "IPV4_DST_ADDR")));
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(AggregationBuilders.terms("IPV4")
                .size(5).field("IPV4_SRC_ADDR").order(BucketOrder.aggregation("_count", false)));
        searchRequest.source(searchSourceBuilder);

        JsonObject finalJson = new JsonObject();
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            JsonArray keyArray = new JsonArray();
            JsonArray valueArray = new JsonArray();
            ParsedStringTerms parsedStringTerms = searchResponse.getAggregations().get("IPV4");
            List<? extends Terms.Bucket> termsBuckets = parsedStringTerms.getBuckets();
            for (Terms.Bucket bucket : termsBuckets) {
                keyArray.add(bucket.getKeyAsString());
                long value = bucket.getDocCount();
                valueArray.add(value);
            }
            finalJson.add("keyArr", keyArray);
            finalJson.add("keyData", valueArray);
            return finalJson;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 业务响应时间趋势图[电建2期左2]
     * @param hours 小时数(正整数)
     * @return 结果
     */
    @RequestMapping(value = "/serverResponseTrend/{hours}", method = RequestMethod.GET)
    public JsonObject serverResponseTrend(@RequestParam("businessName") String businessName, @PathVariable("hours") int hours) {
        if ("dev".equals(active)) { businessName = "统一权限管理"; }
        String oldTime = DateTime.now().minusHours(hours).toString(format);
        LOGGER.info("开始调用serverResponseTrend指定业务服务响应时间趋势,业务名为" + businessName);
        LOGGER.info("查询的起始时间为" + oldTime);
        //分组时间间隔为 6分钟 * hours
        int interval = 6 * hours;
        SearchRequest searchRequest = new SearchRequest(Constant.HEARTBEAT_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.boolQuery()
                .must(QueryBuilders.rangeQuery("@timestamp")
                        .format(format).gte(oldTime).timeZone("Asia/Shanghai"))
                .must(QueryBuilders.matchPhraseQuery("server_name", businessName)));
        searchSourceBuilder.size(0);
        searchSourceBuilder.aggregation(AggregationBuilders.dateHistogram("times").field("@timestamp")
                .timeZone(DateTimeZone.forID("Asia/Shanghai")).dateHistogramInterval(DateHistogramInterval.minutes(interval)).minDocCount(1)
                .subAggregation(AggregationBuilders.avg("duration").field("monitor.duration.us")));
        searchRequest.source(searchSourceBuilder);
        JsonObject finalJson = new JsonObject();
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            JsonArray timeArray = new JsonArray();
            JsonArray valueArray = new JsonArray();
            ParsedDateHistogram timesTerms = searchResponse.getAggregations().get("times");
            List<? extends Histogram.Bucket> timesBuckets = timesTerms.getBuckets();
            for (Histogram.Bucket bucket : timesBuckets) {
                DateTime dateTime = new DateTime(bucket.getKeyAsString());
                String dateTimeStr = dateTime.toString(format);
                timeArray.add(dateTimeStr);
                ParsedAvg parsedAvg = bucket.getAggregations().get("duration");
                int value = (int) parsedAvg.getValue();
                valueArray.add(value);
            }
            finalJson.add("timeArr", timeArray);
            finalJson.add("timeData", valueArray);
            return finalJson;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据业务名生成ip约束
     * @param businessName 业务名
     * @return {"bool":{"should":[{"match_phrase":{"IPV4_DST_ADDR":{"query":"192.168.1.188"}}},{"match_phrase":{"IPV4_DST_ADDR":{"query":"192.168.1.243"}}}]}}
     */
    private BoolQueryBuilder getBoolQueryByBsName(String businessName, String key) {
        List<String> ipList = businessService.getIpListByName(businessName);
        if (ipList == null || ipList.size() < 1) {
            throw new IllegalArgumentException("没有找到[" + businessName + "]系统");
        }
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        for (String ip : ipList) {
            MatchPhraseQueryBuilder matchPhraseQueryBuilder = QueryBuilders.matchPhraseQuery(key, ip);
            boolQueryBuilder.should(matchPhraseQueryBuilder);
        }
        return boolQueryBuilder;
    }

    /**
     * 记录上次的报警结果
     */
    private Set<Alarm> lastAlarm;

    /**
     * 获取报警信息
     * @return 报警信息
     */
    @RequestMapping(value = "/getAlarms", method = RequestMethod.GET)
    public Set<Alarm> getAlarm() {
        //目前只有nmap不通(即nmap_lost)这一种类型会报警,数据从service_error表中来,且handled==未处理
        SearchRequest searchRequest = new SearchRequest(Constant.SERVICEERROR_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.boolQuery()
                .must(QueryBuilders.matchPhraseQuery("errorReason", "nmap_lost"))
                .must(QueryBuilders.matchPhraseQuery("handled", "未处理")));
        searchSourceBuilder.size(1000);
        searchRequest.source(searchSourceBuilder);
        Set<Alarm> alarms = new HashSet<>();
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);
            Iterator it = searchResponse.getHits().iterator();
            while (it.hasNext()) {
                SearchHit hit = (SearchHit) it.next();
                String msg = hit.getSourceAsMap().get("errorType").toString() + ":"
                        + hit.getSourceAsMap().get("errorMsg").toString();
                if (lastAlarm != null && getAlarmByMsg(lastAlarm, msg) != null) {
                    //已经有这条消息
                    Alarm oldAlarm = getAlarmByMsg(lastAlarm, msg);
                    if (oldAlarm.isNew()) {
                        //每次调用其count-1 直到小于0则代表不是新消息了 默认是三次后变为旧消息
                        if (oldAlarm.getCount() == 0) {
                            alarms.add(new Alarm(msg, false, 0));
                        } else {
                            alarms.add(new Alarm(msg, true, oldAlarm.getCount() - 1));
                        }
                    } else {
                        alarms.add(new Alarm(msg, false, 0));
                    }
                } else  {
                    //没有这条消息
                    alarms.add(new Alarm(msg, true));
                }

            }
            System.out.println(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        lastAlarm = alarms;
        return alarms;
    }

    /**
     * 通过msg在alarms中找
     * @param alarms alarms
     * @param msg msg
     * @return alarm
     */
    public Alarm getAlarmByMsg(Set<Alarm> alarms, String msg) {
        for (Alarm alarm : alarms) {
            if (msg.equals(alarm.getMsg())) {
                return alarm;
            }
        }
        return null;
    }

    public static void main(String[] args) {
    }
}
