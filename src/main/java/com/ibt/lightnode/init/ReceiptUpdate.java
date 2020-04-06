package com.ibt.lightnode.init;

import com.ibt.lightnode.pojo.Block;
import com.ibt.lightnode.service.UpdateReceipt;
import com.ibt.lightnode.util.HttpUtil;
import com.ibt.lightnode.util.LevelDbUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.init
 * @Author: keer
 * @CreateTime: 2020-04-03 19:37
 * @Description: 同步receipt
 */
@Component
public class ReceiptUpdate implements CommandLineRunner {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    UpdateReceipt updateReceipt;
    @Override
    public void run(String... args) throws Exception {

        int startHeight=updateReceipt.checkBlockHeight();
        logger.info("开始同步receipt");
        updateReceipt.updateReceipt(startHeight);
    }




}
