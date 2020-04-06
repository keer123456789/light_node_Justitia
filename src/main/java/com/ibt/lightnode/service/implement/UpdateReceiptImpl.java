package com.ibt.lightnode.service.implement;

import com.ibt.lightnode.pojo.Block;
import com.ibt.lightnode.pojo.Transaction;
import com.ibt.lightnode.pojo.TransactionReceipt;
import com.ibt.lightnode.service.UpdateReceipt;
import com.ibt.lightnode.util.HttpUtil;
import com.ibt.lightnode.util.LevelDbUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.service.implement
 * @Author: keer
 * @CreateTime: 2020-04-06 19:37
 * @Description:
 */
@Service
public class UpdateReceiptImpl implements UpdateReceipt {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    @Value("${Full_Node}")
    private String fullNodeUrl;

    @Autowired
    LevelDbUtil levelDbUtil;
    @Autowired
    HttpUtil httpUtil;

    @Override
    public int checkBlockHeight() {
        levelDbUtil.initLevelDB();
        String currentBlockHeight = (String) levelDbUtil.get("currentBlockHeight");
        int currentHeight=0;
        if(currentBlockHeight!=null){
            String temp = currentBlockHeight.substring(2);
            currentHeight = Integer.valueOf(temp, 16);
        }


        int remoteHeight = httpUtil.eth_blockNumber();

        if (currentHeight > remoteHeight || currentHeight == 0 || currentBlockHeight == null) {
            levelDbUtil.put("currentBlockHeight", "0x0");
            levelDbUtil.closeDB();
            return 0;
        } else {
            levelDbUtil.closeDB();
            return currentHeight + 1;
        }
    }

    @Override
    public void updateReceipt(int startHeight) {
        int remoteHeight = httpUtil.eth_blockNumber();
        levelDbUtil.initLevelDB();
        for (; startHeight <= remoteHeight; startHeight++) {
            String h = Integer.toHexString(startHeight).toUpperCase();
            String height = "0x" + h;
            Block block = httpUtil.eth_getBlockByNumber(height, true);
            for (Transaction transaction : block.getTransactions()) {
                TransactionReceipt receipt = httpUtil.eth_getTransactionReceipt(transaction.getHash());
                if(receipt==null){
                    continue;
                }
                levelDbUtil.put("currentBlockHeight", height);
                levelDbUtil.put("receipt" + height + "_" + receipt.getTransactionIndex(), receipt);
            }
            logger.info("同步receipt信息：块号" + height + ",transaction 个数：" + block.getTransactions().size());
        }
        logger.info("本次同步完成，当前块高"+levelDbUtil.get("currentBlockHeight").toString());
        levelDbUtil.closeDB();
    }


}
