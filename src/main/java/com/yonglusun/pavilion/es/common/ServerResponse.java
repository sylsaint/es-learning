package com.yonglusun.pavilion.es.common;

import lombok.Data;

@Data
public class ServerResponse {
  private int code;
  private String message;
  private Object data;

  public static ServerResponse succ(String mesg, Object data) {
    ServerResponse sr = new ServerResponse();
    sr.setCode(200);
    sr.setMessage(mesg);
    sr.setData(data);
    return sr;
  }

  public static ServerResponse fail(String mesg, Object data) {
    ServerResponse sr = new ServerResponse();
    sr.setCode(400);
    sr.setMessage(mesg);
    sr.setData(data);
    return sr;
  }
}
