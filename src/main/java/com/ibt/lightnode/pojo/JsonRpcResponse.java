package com.ibt.lightnode.pojo;

import org.springframework.beans.factory.annotation.Value;

import java.util.Map;


/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.pojo
 * @Author: keer
 * @CreateTime: 2020-04-03 23:28
 * @Description:
 */
public class JsonRpcResponse {
    private String id;
    @Value("jsonrpc")
    private String jsonRpc;

    private Object result;

    public JsonRpcResponse() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getJsonRpc() {
        return jsonRpc;
    }

    public void setJsonRpc(String jsonRpc) {
        this.jsonRpc = jsonRpc;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
