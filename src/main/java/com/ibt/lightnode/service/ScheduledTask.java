package com.ibt.lightnode.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.service
 * @Author: keer
 * @CreateTime: 2020-04-06 20:27
 * @Description: 定时任务
 */
@Component
public class ScheduledTask {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    UpdateReceiptService updateReceipt;

    /**
     * 每两秒同步一次
     */
    @Scheduled(fixedDelay = 2000)
    public void updateReceiptTask(){
        int startHeight=updateReceipt.checkBlockHeight();
        logger.info("开始同步receipt");
        updateReceipt.updateReceipt(startHeight);
    }

    public void checkEventDataInRedis(){

    }
}
