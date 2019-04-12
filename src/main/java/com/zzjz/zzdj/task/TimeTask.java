package com.zzjz.zzdj.task;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zzjz.zzdj.bean.UnRegularFlow;
import com.zzjz.zzdj.util.Constant;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 房桂堂
 * @description TimeTask
 * @date 2018/12/3 14:03
 */
@Component
public class TimeTask {

    private final static Logger LOGGER = LoggerFactory.getLogger(TimeTask.class);

    @Value("${ES_HOST}")
    String esHost;

    @Value("${ES_PORT}")
    int esPort;

    @Value("${ES_METHOD}")
    String esMethod;

    @Autowired
    RestTemplate restTemplate;

    /**
     * 流量异常分析执行周期(分钟)
     */
    private final int NTOPNG_INTERVAL = 1;

    /**
     * 每分钟执行一次
     */
    //@Scheduled(cron="* */1 * * * *")
    //todo 实现流量异常分析全部功能
    public void ss() {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(esHost, esPort, esMethod)));
        String format = "yyyy-MM-dd HH:mm:ss";
        String startTime = DateTime.now().minusMinutes(NTOPNG_INTERVAL).toString(format);
        String endTime = DateTime.now().toString(format);
        LOGGER.info("查询的起始时间为" + startTime);
        SearchRequest searchRequest = new SearchRequest(Constant.SNORT_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(0);
        searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp")
                .format(format).gte(startTime).lte(endTime).timeZone("Asia/Shanghai"));
        searchSourceBuilder.aggregation(
                AggregationBuilders.terms("src").field("IPV4_SRC_ADDR").size(10).order(BucketOrder.aggregation("src", false))
                .subAggregation(AggregationBuilders.terms("dst").field("IPV4_DST_ADDR").size(10).order(BucketOrder.aggregation("dst", false))));
        try {
            SearchResponse searchResponse = client.search(searchRequest);
            long snortCount = searchResponse.getHits().totalHits;
            //bigJson.addProperty("入侵检测", snortCount);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //todo 实现流量非常规时段访问分析(如在00:00发生的HTTP访问则为不正常)
    //@Scheduled(cron="* */1 * * * *")
    public void unRegularVisit() {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost(esHost, esPort, esMethod)));
        String format = "yyyy-MM-dd HH:mm:ss";
        String startTime = DateTime.now().minusMinutes(NTOPNG_INTERVAL).toString(format);
        String endTime = DateTime.now().toString(format);
        LOGGER.info("查询的起始时间为" + startTime);
        //SearchRequest searchRequest = new SearchRequest("dj_ntopng-*");
        SearchRequest searchRequest = new SearchRequest(Constant.NTOPNG_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(5000);
        searchSourceBuilder.query(QueryBuilders.boolQuery().must(
                QueryBuilders.boolQuery().should(QueryBuilders.matchPhraseQuery("IPV4_DST_ADDR.keyword", "10.1.242.87"))
                        .should(QueryBuilders.matchPhraseQuery("IPV4_DST_ADDR.keyword", "10.1.242.93"))
                        .should(QueryBuilders.matchPhraseQuery("IPV4_DST_ADDR.keyword", "10.1.242.86"))
                        .should(QueryBuilders.matchPhraseQuery("IPV4_DST_ADDR.keyword", "10.1.242.95"))
                        .should(QueryBuilders.matchPhraseQuery("IPV4_DST_ADDR.keyword", "10.1.242.104"))
                        .should(QueryBuilders.matchPhraseQuery("IPV4_DST_ADDR.keyword", "10.1.242.84"))
                        .minimumShouldMatch(1)
                        .must(QueryBuilders.matchPhraseQuery("L7_PROTO_NAME.keyword", "HTTP"))
                        .must(QueryBuilders.boolQuery().should(QueryBuilders.matchPhraseQuery("L4_DST_PORT", "80"))
                            .should(QueryBuilders.matchPhraseQuery("L4_DST_PORT", "8080")))
                        .must(QueryBuilders.rangeQuery("@timestamp").gte("1545643710144").lte("1545647676141").format("epoch_millis"))
        ).mustNot(QueryBuilders.boolQuery().should(QueryBuilders.matchPhraseQuery("IPV4_SRC_ADDR.keyword", "10.1.242.111"))
                        .should(QueryBuilders.matchPhraseQuery("IPV4_SRC_ADDR.keyword", "10.1.242.111"))
                        .should(QueryBuilders.matchPhraseQuery("IPV4_SRC_ADDR.keyword", "172.17.0.8"))
        ));
        searchSourceBuilder.sort("@timestamp", SortOrder.ASC);
        try {
            SearchResponse searchResponse = client.search(searchRequest);
            long snortCount = searchResponse.getHits().totalHits;
            //bigJson.addProperty("入侵检测", snortCount);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //@Scheduled(cron="0 25 11 * * *")
    public void sss2() throws IOException {
        RestClient restClient = RestClient.builder(
                new HttpHost("10.1.242.79", 9200, "http"),
                new HttpHost("10.1.242.78", 9200, "http"),
                new HttpHost("10.1.242.80", 9200, "http")).build();
        String jsonString = Constant.unReglularQuery;
        HttpEntity entity = new StringEntity(jsonString, "utf-8");
        Header header = new BasicHeader("Content-Type", "application/json");
        Response response = restClient.performRequest("GET", "/ntopng-*/_doc/_search?pretty", Collections.emptyMap(), entity, header);
        String res = EntityUtils.toString(response.getEntity());
        System.out.println(res);
        JsonObject resObject = new JsonParser().parse(res).getAsJsonObject();
        JsonObject hitsObject = resObject.getAsJsonObject("hits");
        JsonArray hitsArray = hitsObject.getAsJsonArray("hits");

        Map<String, List<JsonObject>> map = new HashMap<>();
        for (JsonElement ele : hitsArray) {
            JsonObject hitObj = ele.getAsJsonObject();
            JsonObject sourceObj = hitObj.getAsJsonObject("_source");
            String key = sourceObj.get("IPV4_SRC_ADDR").getAsString() + "-" + sourceObj.get("IPV4_DST_ADDR").getAsString();
            if (map.get(key) == null) {
                List<JsonObject> list = new ArrayList<>();
                list.add(hitObj);
                map.put(key, list);
            } else {
                List<JsonObject> list = map.get(key);
                list.add(hitObj);
                map.put(key, list);
            }
        }
        System.out.println(map);

        List<UnRegularFlow> unRegularFlows = new ArrayList<>();
        for (Map.Entry<String, List<JsonObject>> entry : map.entrySet()) {
            List<JsonObject> list = entry.getValue();
            List<JsonObject> targetList = new ArrayList<>();

            for (int i = 0; i < list.size(); i++) {
                JsonObject hitObj = list.get(i).getAsJsonObject();
                JsonObject sourceObj = hitObj.getAsJsonObject("_source");
                DateTime timestamp = new DateTime(sourceObj.get("@timestamp").getAsString());
                if (targetList.size() == 0) {
                    //第一个元素跳过
                    targetList.add(hitObj);
                    continue;
                }
                JsonObject lastObj = targetList.get(targetList.size() - 1);
                JsonObject lastSourceObj = lastObj.getAsJsonObject("_source");
                DateTime lastTimestamp = new DateTime(lastSourceObj.get("@timestamp").getAsString());
                if (timestamp.minusMinutes(10).isAfter(lastTimestamp)) {
                    //间隔超过10分钟 targetList清空 并将旧的输出到异常
                    UnRegularFlow flow = combineData(targetList);
                    unRegularFlows.add(flow);
                    targetList = new ArrayList<>();
                    targetList.add(hitObj);
                    if (i == list.size() - 1) {
                        //是最后一条数据
                        UnRegularFlow flow1 = combineData(targetList);
                        unRegularFlows.add(flow1);
                    }

                } else {
                    //不超过10分钟
                    targetList.add(hitObj);
                    if (i == list.size() - 1) {
                        //是最后一条数据
                        UnRegularFlow flow = combineData(targetList);
                        unRegularFlows.add(flow);
                    }
                }
            }
        }

        insertIntoUnRegular(unRegularFlows);
        insertIntoEventlog(unRegularFlows);

    }

    /**
     * 聚合成一条异常信息
     * @param targetList targetList
     * @return
     */
    private UnRegularFlow combineData(List<JsonObject> targetList) {
        UnRegularFlow flow = new UnRegularFlow();
        JsonObject firstSource = targetList.get(0).getAsJsonObject("_source");
        JsonObject lastSource = targetList.get(targetList.size() - 1).getAsJsonObject("_source");
        flow.setCount(targetList.size());
        flow.setSrcIp(firstSource.get("IPV4_SRC_ADDR").getAsString());
        flow.setDstIp(firstSource.get("IPV4_DST_ADDR").getAsString());
        flow.setStartTime(firstSource.get("@timestamp").getAsString());
        flow.setEndTime(lastSource.get("@timestamp").getAsString());
        long outBytes = 0L;
        long inBytes = 0L;
        for (JsonElement ele : targetList) {
            JsonObject hitsObj = ele.getAsJsonObject();
            JsonObject sourceObj = hitsObj.getAsJsonObject("_source");
            outBytes += sourceObj.get("OUT_BYTES").getAsLong();
            inBytes += sourceObj.get("IN_BYTES").getAsLong();
        }

        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(esHost, esPort, esMethod)));
        GetRequest getRequest = new GetRequest("soc-system", "res", flow.getDstIp());
        try {
            GetResponse getResponse = client.get(getRequest);
            if (!getResponse.isSourceEmpty()) {
                String service = (String) getResponse.getSourceAsMap().get("used");
                flow.setService(service);
            } else {
                flow.setService("未知");
            }
            //flow.setService("测试系统");
        } catch (IOException e) {
            e.printStackTrace();
        }
        flow.setOutBytes(outBytes);
        flow.setInBytes(inBytes);
        return flow;
    }

    /**
     * 插入UnRegularVist表
     * @param flows flows
     * @throws IOException IOException
     */
    private void insertIntoUnRegular(List<UnRegularFlow> flows) throws IOException {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(esHost, esPort, esMethod)));
        String dayStr = new DateTime().toString("yyyy.MM.dd");
        BulkRequest request = new BulkRequest();
        for (UnRegularFlow flow : flows) {
            Map<String, Object> jsonMap = new HashMap<>();
            jsonMap.put("count", flow.getCount());
            jsonMap.put("srcIp", flow.getSrcIp());
            jsonMap.put("dstIp", flow.getDstIp());
            jsonMap.put("startTime", flow.getStartTime());
            jsonMap.put("endTime", flow.getEndTime());
            jsonMap.put("inBytes", flow.getInBytes());
            jsonMap.put("outBytes", flow.getOutBytes());
            jsonMap.put("service", flow.getService());
            jsonMap.put("@timestamp", new Date());
            request.add(new IndexRequest("unregularvisit-" + dayStr, "doc").source(jsonMap));
        }
        BulkResponse bulkResponse = client.bulk(request);
        LOGGER.info("unregularvisit插入执行结果:" +  (bulkResponse.hasFailures() ? "有错误" : "成功"));
        LOGGER.info("unregularvisit插入执行用时:" + bulkResponse.getTook().getMillis() + "毫秒");
        client.close();
    }

    /**
     * 插入eventlog表
     * @param flows flows
     * @throws IOException IOException
     */
    public void insertIntoEventlog(List<UnRegularFlow> flows) throws IOException {
        String format = "yyyy-MM-dd HH:mm:ss";
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(esHost, esPort, esMethod)));
        BulkRequest request = new BulkRequest();
        String dayStr = new DateTime().toString("yyyy.MM.dd");
        for (UnRegularFlow flow : flows){
            Map<String, Object> jsonMap = new HashMap<>();
            String ip = flow.getDstIp();
            //通过查询资产表获取相关信息
            GetRequest getRequest = new GetRequest("soc-system", "res", ip);
            GetResponse getResponse = client.get(getRequest);
            if (!getResponse.isSourceEmpty()) {
                jsonMap.put("dept", getResponse.getSourceAsMap().get("dept"));
                String manager = (String) getResponse.getSourceAsMap().get("manager");
                jsonMap.put("assetName", manager + "-" + ip);
                jsonMap.put("location", getResponse.getSourceAsMap().get("area"));
            } else {
                jsonMap.put("dept", "无");
                jsonMap.put("assetName", flow.getDstIp());
            }
            String timeFormatted = new DateTime(flow.getStartTime()).toString(format);
            jsonMap.put("dataType", "用户");
            jsonMap.put("eventType", "网络访问异常");
            jsonMap.put("eventPriority", "高");
            jsonMap.put("eventScore", 85);
            jsonMap.put("assetIP", flow.getDstIp());
            jsonMap.put("eventName", "非常规时段受访问");
            jsonMap.put("eventUserMsg", "ip为" + flow.getSrcIp() + "的用户在" +
                    timeFormatted + "时间访问了ip为" + flow.getDstIp() + "的" + flow.getService() + "服务" + flow.getCount() + "次");
            jsonMap.put("@timestamp", new Date());
            request.add(new IndexRequest("eventlog_" + dayStr, "doc").source(jsonMap));
        }
        BulkResponse bulkResponse = client.bulk(request);
        LOGGER.info("unRegularVisit插入执行结果:" +  (bulkResponse.hasFailures() ? "有错误" : "成功"));
        LOGGER.info("unRegularVisit插入执行用时:" + bulkResponse.getTook().getMillis() + "毫秒");
        client.close();
    }

   /* private synchronized <T> HttpEntity<T> getRequestBody(T requestStr) {
        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaType.parseMediaType("application/json; charset=UTF-8");
        headers.setContentType(type);
        headers.add("Accept", MediaType.APPLICATION_JSON.toString());
        return new HttpEntity<>(requestStr, headers);
    }*/


    public static void main(String[] args) {

    }
    /*public static void main(String[] args) {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("es1", 9200, "http")));
        String format = "yyyy-MM-dd HH:mm:ss";
        String startTime = DateTime.now().minusMinutes(2).toString(format);
        String endTime = DateTime.now().toString(format);
        LOGGER.info("查询的起始时间为" + startTime);
        SearchRequest searchRequest = new SearchRequest(Constant.NTOPNG_INDEX);
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.size(10000);
        searchSourceBuilder.fetchSource(new String[] {"IPV4_SRC_ADDR", "IPV4_DST_ADDR", "OUT_BYTES", "IN_BYTES"}, null);
        searchSourceBuilder.query(QueryBuilders.rangeQuery("@timestamp")
                .format(format).gte(startTime).lte(endTime).timeZone("Asia/Shanghai"));
        *//*searchSourceBuilder.aggregation(
                AggregationBuilders.terms("src").field("IPV4_SRC_ADDR").size(10).order(BucketOrder.aggregation("_count", false))
                        .subAggregation(AggregationBuilders.terms("dst").field("IPV4_DST_ADDR").size(10).order(BucketOrder.aggregation("_count", false))));*//*
        searchRequest.source(searchSourceBuilder);
        Map<String, Integer> countMap = new HashMap<>();
        Map<String, Long> bytesMap = new HashMap<>();
        try {
            SearchResponse searchResponse = client.search(searchRequest);
            long count = searchResponse.getHits().totalHits;
            Iterator it = searchResponse.getHits().iterator();
            while (it.hasNext()) {
                SearchHit hit = (SearchHit) it.next();
                String key = hit.getSourceAsMap().get("IPV4_SRC_ADDR") + "->" + hit.getSourceAsMap().get("IPV4_DST_ADDR");
                if (countMap.get(key) == null) {
                    countMap.put(key, 1);
                    bytesMap.put(key, ((Integer) hit.getSourceAsMap().get("OUT_BYTES")).longValue());
                } else {
                    countMap.put(key, countMap.get(key) + 1);
                    bytesMap.put(key, (((Integer) hit.getSourceAsMap().get("OUT_BYTES")).longValue() + bytesMap.get(key)));
                }
            }
            System.out.println(1);

            Map<String, Object> eventlogMap = new HashMap<>();
            //次数超过每分钟60次为不正常    流量每分钟超过100M为不正常
            for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
                if (entry.getValue() > (2 * 120)) {
                    eventlogMap.put("dateType", "用户");
                    eventlogMap.put("eventType", "网络访问异常");
                    eventlogMap.put("eventName", "访问频率过高");
                    eventlogMap.put("eventScore", 60);
                    eventlogMap.put("eventUserMsg", "用户");
                }
            }

            //bigJson.addProperty("入侵检测", snortCount);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}
