package com.ibt.lightnode.init;

import com.google.gson.Gson;
import com.ibt.lightnode.pojo.JsonRpcResponse;
import com.ibt.lightnode.util.HttpUtil;
import com.ibt.lightnode.util.LevelDbUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.init
 * @Author: keer
 * @CreateTime: 2020-04-03 19:37
 * @Description: 同步receipt
 */
@Component
public class ReceiptUpdate implements CommandLineRunner {
    @Value("${Full_Node}")
    private String fullNodeUrl;

    @Autowired
    LevelDbUtil levelDbUtil;
    @Autowired
    HttpUtil httpUtil;

    @Override
    public void run(String... args) throws Exception {
        levelDbUtil.initLevelDB();
        String currentBlockHeight = (String) levelDbUtil.get("currentBlockHeight");

        String getBlockNumberUrl=fullNodeUrl+"/eth_blockNumber";
        String rep=httpUtil.httpGet(getBlockNumberUrl);
        Gson gson=new Gson();
        JsonRpcResponse jsonRpcResponse=gson.fromJson(rep,JsonRpcResponse.class);

    }


}
