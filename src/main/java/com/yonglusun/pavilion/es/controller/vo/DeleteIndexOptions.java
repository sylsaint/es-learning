package com.yonglusun.pavilion.es.controller.vo;

import lombok.Data;
import org.elasticsearch.action.support.IndicesOptions;

@Data
public class DeleteIndexOptions {
  private int timeout; // 超时时间，单位秒
  private int masterTimeout; // 主节点超时时间，单位秒
  private IndicesOptions option;
}
