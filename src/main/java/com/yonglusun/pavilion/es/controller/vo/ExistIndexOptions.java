package com.yonglusun.pavilion.es.controller.vo;

import lombok.Data;

@Data
public class ExistIndexOptions {
  private boolean isLocal;
  private boolean humanReadable;
  private boolean includeDefaults;
}
