package com.ibt.lightnode.redis;

import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.redis
 * @Author: keer
 * @CreateTime: 2020-05-08 15:20
 * @Description: redis存储策略 接口
 */
public interface RedisStrategy {

    /**
     * 存入数据
     * @param eventDatas 一笔交易的event 数据
     */
    void put(Map eventDatas);
}
