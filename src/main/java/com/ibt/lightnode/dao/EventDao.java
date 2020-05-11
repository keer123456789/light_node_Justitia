package com.ibt.lightnode.dao;

import com.ibt.lightnode.pojo.Log;
import com.ibt.lightnode.redis.RedisStrategy;
import com.ibt.lightnode.util.LevelDbTemplete;
import com.ibt.lightnode.util.SimpleRedisTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.dao
 * @Author: keer
 * @CreateTime: 2020-05-08 08:42
 * @Description: 事件信息 存入相关 db
 */
@Component
public class EventDao {
    @Autowired
    LevelDbTemplete levelDbTemplete;

    @Autowired
    SimpleRedisTemplate redisTemplate;

    @Autowired
    RedisStrategy redisStrategy;

    @Value("#{'${base_event_names}'.split(',')}")
    private List<String> baseEventNames;

    private Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * 添加事件数据
     * @param map
     */
    public void addEventData(Map map) {
        int id = -1;
        int state = -1;
        Set<String> keys = map.keySet();
        for (String key : keys) {
            if (baseEventNames.contains(key)) {
                Map event = (Map) map.get(key);
                id = (int) event.get("id");
                state = (int) event.get("state");
            }
        }
        for (String key : keys) {
            levelDbTemplete = LevelDbTemplete.getInstance();
            levelDbTemplete.put(id + "_" + key, map.get(key));
            logger.info("key:" + id + "_" + key);
            logger.info("value:" + map.get(key));
        }

        for (String key : keys) {
            if (!baseEventNames.contains(key)) {
                Map event = (Map) map.get(key);
                event.put("id", id);
                event.put("state", state);
                map.put(key, event);
            }
        }
        redisStrategy.put(map);
    }

    /**
     * 根据id查找事件数据
     * @param id
     */
    public Map getEventDataByID(int id){
        return redisStrategy.getTraceInfoById(id);
    }
}
