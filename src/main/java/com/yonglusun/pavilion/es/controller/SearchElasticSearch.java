package com.yonglusun.pavilion.es.controller;


import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.support.replication.ReplicationResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.rest.RestStatus;
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
  static RestHighLevelClient client = new RestHighLevelClient(
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

    // 创建操作不支持指定版本或者外部版本，仅支持内部版本
    // request.version(1);
    // request.versionType(VersionType.EXTERNAL);

    // 设置操作类型
    request.opType(DocWriteRequest.OpType.CREATE);
    request.opType("create");

    // A pipeline is a definition of a series of processors that are to be executed
    // in the same order as they are declared.
    // request.setPipeline("pipeline");

    IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);

    String index = indexResponse.getIndex();
    String id = indexResponse.getId();

    System.out.println(String.format("index: {}, id: {}", index, id));

    if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
      System.out.println("index created");
    } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
      System.out.println("index updated");
    }

    ReplicationResponse.ShardInfo shardInfo = indexResponse.getShardInfo();
    if (shardInfo.getTotal() != shardInfo.getSuccessful()) {
      System.out.println("There are failures");
    }

    if (shardInfo.getFailed() > 0) {
      for (ReplicationResponse.ShardInfo.Failure failure : shardInfo.getFailures()) {
        String reason = failure.reason();
        System.out.println(reason);
      }
    }

    return "ok";
  }

  @GetMapping("/conflict")
  public String confict() throws IOException {
    IndexRequest request = new IndexRequest("posts")
            .id("1")
            .source("field", "value")
            .setIfSeqNo(10L)
            .setIfPrimaryTerm(20);
    try {
      IndexResponse response = client.index(request, RequestOptions.DEFAULT);
    } catch(ElasticsearchException e) {
      if (e.status() == RestStatus.CONFLICT) {
        e.printStackTrace();
        return e.getDetailedMessage();
      }
    }
    return "ok";
  }
}
