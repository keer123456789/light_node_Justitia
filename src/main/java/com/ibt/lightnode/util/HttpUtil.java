package com.ibt.lightnode.util;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.ibt.lightnode.pojo.Block;
import com.ibt.lightnode.pojo.JsonRpcResponse;
import com.ibt.lightnode.pojo.RpcRequst;
import com.ibt.lightnode.pojo.TransactionReceipt;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.util
 * @Author: keer
 * @CreateTime: 2020-04-03 20:05
 * @Description: Http工具类
 */
@Component
public class HttpUtil {
    private Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    @Value("${Full_Node}")
    private String fullNodeUrl;

    /**
     * 发起get请求
     *
     * @param url
     * @return
     */
    public String httpGet(String url) {
        return httpGet(url, null);
    }

    /**
     * 发送带有参数的get请求
     *
     * @param url
     * @param params
     * @return
     */
    public String httpGet(String url, Map<String, String> params) {
        HttpUrl.Builder httpBuilder = HttpUrl.parse(url).newBuilder();
        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                httpBuilder.addQueryParameter(param.getKey(), param.getValue());
            }
        }
        Request request = new Request.Builder()
                .url(httpBuilder.build())
                .build();
        return execNewCall(request);
    }

    /**
     * 发送httppost请求
     *
     * @param url
     * @param data pojo形式的数据
     * @return
     */
    public String httpPost(String url, Object data) {
        Gson gson = new Gson();
        String json = gson.toJson(data);
        RequestBody requestBody = RequestBody.create(json, MediaType.parse("application/json;charset=utf-8"));

        Request request = new Request.Builder()
                .addHeader("Content-Type", "application/json;charset=UTF-8")
                .url(url)
                .post(requestBody)
                .build();

        return execNewCall(request);
    }

    /**
     * 调用Http的newCall方法
     *
     * @param request
     * @return
     */
    private String execNewCall(Request request) {
        Response response = null;
        try {
            OkHttpClient okHttpClient = new OkHttpClient();
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            }
        } catch (Exception e) {
            logger.error("发送POST请求失败！！，请求信息：" + request.body().toString());
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return "";
    }

    /**
     * 通过配置文件中的全节点地址，获取全节点的块高
     *
     * @return
     */
    public int eth_blockNumber() {
        String rep = httpGet(fullNodeUrl + "/eth_blockNumber");
        Gson gson = new Gson();
        JsonRpcResponse jsonRpcResponse = gson.fromJson(rep, JsonRpcResponse.class);
        String height = jsonRpcResponse.getResult().toString().substring(2);
        return Integer.valueOf(height, 16);
    }

    /**
     * 获得块中的交易个数
     *
     * @param number 必须是0x开头16进制的字符串
     * @return
     */
    public String eth_getBlockTransactionCountByNumber(String number) {
        List list = new ArrayList();
        list.add(number);
        RpcRequst rpcRequst = new RpcRequst("/eth_getBlockTransactionCountByNumber", list);
        String rep = httpPost(fullNodeUrl, rpcRequst);
        Gson gson = new Gson();
        JsonRpcResponse jsonRpcResponse = gson.fromJson(rep, JsonRpcResponse.class);
        return jsonRpcResponse.getResult().toString();
    }

    /**
     * 通过块高获取区块
     *
     * @param number 必须是0x开头16进制的字符串
     * @param fullTx
     * @return
     */
    public Block eth_getBlockByNumber(String number, boolean fullTx) {
        List list = new ArrayList();
        list.add(number);
        list.add(fullTx);
        RpcRequst rpcRequst = new RpcRequst("eth_getBlockByNumber", list);
        String rep = httpPost(fullNodeUrl, rpcRequst);
        Gson gson = new Gson();
        JsonRpcResponse jsonRpcResponse = gson.fromJson(rep, JsonRpcResponse.class);
        String blockStr = gson.toJson(jsonRpcResponse.getResult());
        return gson.fromJson(blockStr, Block.class);
    }

    /**
     * 通过交易hash获得交易receipt
     *
     * @param hash
     * @return
     */
    public TransactionReceipt eth_getTransactionReceipt(String hash) {
        List list = new ArrayList();
        list.add(hash);
        RpcRequst rpcRequst = new RpcRequst("eth_getTransactionReceipt", list);
        String rep = httpPost(fullNodeUrl, rpcRequst);
        JsonRpcResponse jsonRpcResponse = JSON.parseObject(rep, JsonRpcResponse.class);
        String transactionStr = JSON.toJSONString(jsonRpcResponse.getResult());
        return JSON.parseObject(transactionStr, TransactionReceipt.class);
    }

}
