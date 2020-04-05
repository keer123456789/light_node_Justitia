package com.ibt.lightnode.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.ibt.lightnode.pojo.JsonRpcResponse;
import com.ibt.lightnode.pojo.TransactionReceipt;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.util
 * @Author: keer
 * @CreateTime: 2020-04-03 20:05
 * @Description: Http工具类
 */
//@Component
public class HttpUtil {
    private Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    /**
     * 发起get请求
     *
     * @param url
     * @return
     */
    public String httpGet(String url) {
        Request request = new Request.Builder().url(url).build();
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
    private  String execNewCall(Request request) {
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

    public static void main(String[] args) {
        HttpUtil httpUtil=new HttpUtil();
        String res=httpUtil.httpGet("http://127.0.0.1:47768/eth_getTransactionReceipt?hash=%220xec1885dc6e756fe1ed92405e837e3c21b8dfd021c5c7a4d987922e27ba77960e%22");

        JsonRpcResponse jsonRpcResponse= JSON.parseObject(res, JsonRpcResponse.class);
        String strReceipt=JSON.toJSONString(jsonRpcResponse.getResult());
        TransactionReceipt receipt=JSON.parseObject(strReceipt,TransactionReceipt.class);
    }
}
