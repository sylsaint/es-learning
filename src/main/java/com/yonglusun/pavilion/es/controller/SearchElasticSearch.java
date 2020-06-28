package com.yonglusun.pavilion.es.controller;


import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/connect")
public class SearchElasticSearch {
  RestHighLevelClient client = new RestHighLevelClient(
      RestClient.builder(
          new HttpHost("localhost", 9200, "http"),
          new HttpHost("localhost", 9201, "http")));
  @GetMapping("/hello")
  public String getEsInstance() {
    return client.license().toString();
  }

  @GetMapping("/index/create")
  public String createIndex(@RequestParam(value = "name") String indexName) {
    CreateIndexRequest request = new CreateIndexRequest(indexName);
    request.settings(Settings.builder().put("index.number_of_shards", 3).put("index.number_of_replicas", 2));
    return "ok";
  }

  // Document API
  @GetMapping("/document")
  public String document() throws IOException {
    IndexRequest request = new IndexRequest("posts");
    request.id("1");
    String jsonString = "{" +
        "\"user\":\"kimchy\"," +
        "\"postDate\":\"2013-01-30\"," +
        "\"message\":\"trying out Elasticsearch\"" +
        "}";
    request.source(jsonString, XContentType.JSON);

    // providing the document source
    Map<String, Object> jsonMap = new HashMap<>();
    jsonMap.put("user", "kimchy");
    jsonMap.put("postDate", new Date());
    jsonMap.put("message", "trying out Elasticsearch");
    IndexRequest request1 = new IndexRequest().id("1").source(jsonMap);

    // build object
    XContentBuilder builder = XContentFactory.jsonBuilder();
    builder.startObject();
    {
      builder.field("user", "kimchy");
      builder.timeField("postDate", new Date());
      builder.field("message", "trying out Elasticsearch");
    }
    builder.endObject();
    IndexRequest indexRequest = new IndexRequest("posts")
        .id("1").source(builder);

    // 设置路由
    request.routing("routing");

    // 设置主分片获取的超时时间
    request.timeout(TimeValue.timeValueSeconds(1));
    request.timeout("1s");

    // 设置索引刷新
    // 等待
    request.setRefreshPolicy(WriteRequest.RefreshPolicy.WAIT_UNTIL);
    request.setRefreshPolicy("wait_for");

    request.version(2);
    request.versionType(VersionType.EXTERNAL);

    return "ok";
  }
}
