package com.ibt.lightnode.dao;

import com.ibt.lightnode.util.LevelDbTemplete;
import com.ibt.lightnode.util.SimpleRedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.SimpleFileVisitor;

/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.dao
 * @Author: keer
 * @CreateTime: 2020-05-08 08:59
 * @Description: 区块相关信息存入db
 */
@Component
public class BlockDao {
    @Autowired
    LevelDbTemplete levelDbTemplete;

    @Autowired
    SimpleRedisTemplate redisTemplate;

    public void setBlockHeight(String height){
        levelDbTemplete.put("currentBlockHeight",height);
    }

    public String getBlockHeight(){
        return (String) levelDbTemplete.get("currentBlockHeight");
    }
}
