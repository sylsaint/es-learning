package com.yonglusun.pavilion.es.controller.vo;

import lombok.Data;

@Data
public class CreateIndexOptions {
  private int shards;
  private int replicas;
  private String mapping;
  private String alias;
  private int timeout; // 单位为秒
  private int masterTimeout; // 单位为秒
  private int activeShards; // 活跃分片数
}
