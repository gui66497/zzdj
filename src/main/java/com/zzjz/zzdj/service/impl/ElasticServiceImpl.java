package com.zzjz.zzdj.service.impl;

import com.zzjz.zzdj.service.ElasticService;
import com.zzjz.zzdj.util.Constant;
import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author 房桂堂
 * @description ElasticServiceImpl
 * @date 2018/12/1 17:42
 */
@Service
public class ElasticServiceImpl implements ElasticService {

    @Value("${ES_HOST}")
    String esHost;

    @Value("${ES_PORT}")
    int esPort;

    @Value("${ES_METHOD}")
    String esMethod;

    private String format = "yyyy-MM-dd HH:mm:ss";

    @Override
    public Map<String, Boolean> getAllNmap() {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(esHost, esPort, esMethod)));

        //查询最近NMAP_SCAN_INTERVAL分钟的nmap记录
        String oldTime = DateTime.now().minusMinutes(Constant.NMAP_SCAN_INTERVAL).toString(format);
        SearchRequest searchRequestN = new SearchRequest(Constant.NMAP_INDEX);
        SearchSourceBuilder searchSourceBuilderN = new SearchSourceBuilder();
        searchSourceBuilderN.size(1000);
        searchSourceBuilderN.query(QueryBuilders.rangeQuery("@timestamp")
                .format(format).gte(oldTime).timeZone("Asia/Shanghai"));
        searchSourceBuilderN.fetchSource(new String[]{"status", "@timestamp", "ipv4"}, null);
        searchRequestN.source(searchSourceBuilderN);
        try {
            SearchResponse searchResponse = client.search(searchRequestN);
            Map<String, Boolean> nmapMap = new HashMap<>();
            Iterator it = searchResponse.getHits().iterator();
            while (it.hasNext()) {
                SearchHit hit = (SearchHit) it.next();
                String state = ((HashMap) hit.getSourceAsMap().get("status")).get("state").toString();
                nmapMap.put(hit.getSourceAsMap().get("ipv4").toString(), "up".equals(state));
            }
            return nmapMap;
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
