package com.yonglusun.pavilion.es.controller;

import org.apache.http.HttpHost;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.document.DocumentField;
import org.elasticsearch.index.VersionType;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/get")
public class GetApiController {
  static RestHighLevelClient client = new RestHighLevelClient(
    RestClient.builder(
      new HttpHost("localhost", 9200, "http"),
      new HttpHost("localhost", 9201, "http")));

  @GetMapping("/req")
  public String getRequest() throws IOException {
    // 设置请求的索引和文档id
    GetRequest getRequest = new GetRequest("posts", "1");
    //
    getRequest.fetchSourceContext(FetchSourceContext.DO_NOT_FETCH_SOURCE);

    // 设置返回中包含的字段和排除的字段
    String[] includes = new String[]{"message", "*Date"};
    String[] excludes = Strings.EMPTY_ARRAY;
    FetchSourceContext fetchSourceContext = new FetchSourceContext(false, includes, excludes);
    getRequest.fetchSourceContext(fetchSourceContext);
    // 显示指定要返回的字段
    getRequest.storedFields("message");
    // 设置请求参数
    getRequest.routing("routing");
    getRequest.preference("preference");
    // 实时返回
    getRequest.realtime(false);
    // 是否在获取值之前刷新索引，以便获取最新的值
    getRequest.refresh(true);
    // 指定获取的版本
    // 如果当前不包含此版本，则提示版本冲突
    // getRequest.version(1);
    // getRequest.versionType(VersionType.EXTERNAL);

    GetResponse getResponse = client.get(getRequest, RequestOptions.DEFAULT);

    // 获取返回值
    /**
    String index = getResponse.getIndex();
    String id = getResponse.getId();
    if (getResponse.isExists()) {
      long version = getResponse.getVersion();
      String sourceAsString = getResponse.getSourceAsString();
      Map<String, Object> sourceAsMap = getResponse.getSourceAsMap();
      byte[] sourceAsBytes = getResponse.getSourceAsBytes();
    } else {

    }
     */
    String index = getResponse.getIndex();
    String id = getResponse.getId();
    System.out.println(String.format("index: %s, id: %s", index, id));
    DocumentField field = getResponse.getField("message");
    if (field == null) {
      return "result is null";
    }
    return field.getValue();
  }
}
