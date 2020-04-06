package com.ibt.lightnode.pojo;

import java.util.List;

/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.pojo
 * @Author: keer
 * @CreateTime: 2020-04-06 17:28
 * @Description: 发送post请求Justitia的接口使用的格式
 */
public class RpcRequst {
    private int id = 1;
    private String jsonrpc="2.0";
    private String method;
    private List params;

    public RpcRequst() {
    }

    public RpcRequst(String method, List params) {
        this.method = method;
        this.params = params;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List getParams() {
        return params;
    }

    public void setParams(List params) {
        this.params = params;
    }
}
