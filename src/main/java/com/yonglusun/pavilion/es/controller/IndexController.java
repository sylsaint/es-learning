package com.yonglusun.pavilion.es.controller;

import com.yonglusun.pavilion.es.common.ServerResponse;
import com.yonglusun.pavilion.es.controller.vo.CreateIndexOptions;
import com.yonglusun.pavilion.es.controller.vo.DeleteIndexOptions;
import com.yonglusun.pavilion.es.controller.vo.ExistIndexOptions;
import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.*;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Objects;

@RestController
@RequestMapping("/index")
public class IndexController {
  static RestHighLevelClient client = new RestHighLevelClient(
    RestClient.builder(
      new HttpHost("localhost", 9200, "http"),
      new HttpHost("localhost", 9201, "http")));

  static ActionListener<CreateIndexResponse> listener =
    new ActionListener<CreateIndexResponse>() {

      @Override
      public void onResponse(CreateIndexResponse createIndexResponse) {

      }

      @Override
      public void onFailure(Exception e) {

      }
    };

  @PostMapping("/create/{indexName}")
  public ServerResponse createIndex(@PathVariable String indexName, @RequestBody CreateIndexOptions cs) {
    // 设置创建索引请求变量
    CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
    // 设置索引的分片和副本数
    int shards = cs.getShards() > 0 ? cs.getShards() : 3;
    int replicas = cs.getReplicas() > 0 ? cs.getReplicas() : 2;
    createIndexRequest.settings(Settings.builder()
      .put("index.number_of_shards", shards)
      .put("index.number_of_replicas", replicas)
    );
    // 设置索引的文档类型映射
    if (cs.getMapping() != null) {
      createIndexRequest.mapping(cs.getMapping(), XContentType.JSON);
    }
    // 设置索引别名
    if (cs.getAlias() != null && !cs.getAlias().equals("")) {
      createIndexRequest.alias(new Alias(cs.getAlias()));
    }
    // 设置可选参数

    // 设置所有节点的超时时间
    if (cs.getTimeout() > 0) {
      createIndexRequest.setTimeout(TimeValue.timeValueSeconds(cs.getTimeout()));
    }
    // 设置master节点的超时时间
    if (cs.getMasterTimeout() > 0) {
      createIndexRequest.setMasterTimeout(TimeValue.timeValueSeconds(cs.getMasterTimeout()));
    }

    // 设置至少等待多少个活跃的分片才返回结果
    if (Objects.nonNull(cs.getActiveShards())) {
      createIndexRequest.waitForActiveShards(ActiveShardCount.from(cs.getActiveShards()));
    }

    // 同步执行
    try {
      CreateIndexResponse response = client.indices().create(createIndexRequest, RequestOptions.DEFAULT);
      return ServerResponse.succ("新建索引成功", response);
    } catch (IOException e) {
      return ServerResponse.fail("新建索引失败", e);
    }

    // TODO: 异步执行
  }

  @GetMapping("/exists/{indexName}")
  public ServerResponse existIndex(@PathVariable String indexName, @RequestBody ExistIndexOptions ei) {
    if (indexName.equals(null)) {
      return ServerResponse.fail("请输入索引名称", null);
    }
    GetIndexRequest existRequest = new GetIndexRequest(indexName);
    // 是否返回本地信息或者从master获取状态信息
    existRequest.local(ei.isLocal());
    // 返回结果以友好的方式显示
    existRequest.humanReadable(ei.isHumanReadable());
    // 是否返回每个分片的默认设置
    existRequest.includeDefaults(ei.isIncludeDefaults());
    existRequest.indicesOptions(IndicesOptions.lenientExpand());
    try {
      boolean exists = client.indices().exists(existRequest, RequestOptions.DEFAULT);
      return ServerResponse.succ("查询成功", exists);
    } catch (IOException e) {
      return ServerResponse.fail("查询失败", indexName);
    }
  }

  @DeleteMapping("/delete/{indexName}")
  public ServerResponse deleteIndex(@PathVariable String indexName, @RequestBody DeleteIndexOptions di) {
    if (indexName.equals(null)) {
      return ServerResponse.fail("请提供索引名称", null);
    }
    DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest(indexName);
    // 设置超时时间
    if (di.getTimeout()> 0) {
      deleteIndexRequest.timeout(TimeValue.timeValueSeconds(di.getTimeout()));
    }
    // 设置主节点超时时间
    if (di.getMasterTimeout() > 0) {
      deleteIndexRequest.masterNodeTimeout(TimeValue.timeValueSeconds(di.getMasterTimeout()));
    }
    // 设置不可用索引如何解析，以及通配符如何扩展
    if (di.getOption() != null) {
      deleteIndexRequest.indicesOptions(di.getOption());
    }
    try {
       client.indices().delete(deleteIndexRequest, RequestOptions.DEFAULT);
      return ServerResponse.succ("删除索引成功", indexName);
    } catch (IOException e) {
      return ServerResponse.fail("删除索引失败", e);
    }
  }
}
