package com.yonglusun.pavilion.es.controller;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/document")
public class DocumentController {

  static RestHighLevelClient client = new RestHighLevelClient(
    RestClient.builder(
      new HttpHost("localhost", 9200, "http"),
      new HttpHost("localhost", 9201, "http")));


}
