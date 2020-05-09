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

    private Logger logger=LoggerFactory.getLogger(this.getClass());

    public void setEventData(String key, Object data){
        levelDbTemplete=LevelDbTemplete.getInstance();
        levelDbTemplete.put(key,data);


        /**
         * =======================redis===========================
         */
        String[] keys=key.split("_");
        String id=keys[0];
        String eventName=keys[1];
        int state=-1;
        if(baseEventNames.contains(eventName)){
            Map map= (Map) data;
            state= (int) map.get("state");
        }

        /**
         * 更新id的当前状态,时间
         */
        if(state!=-1){
            Map keyInfo=new HashMap();
            keyInfo.put("currentState",state);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time =df.format(new Date());
            keyInfo.put("lastUseTime",time);
            redisTemplate.set(id+"_info",keyInfo);
        }


        /**
         * 将事件数据放入缓存
         */
//        Map eventData=new HashMap();
//        eventData.put(eventName,data);
//        redisTemplate.sSet(id,eventData);
        redisTemplate.sSet(id,data);

    }

    public void addEventData(Map map){
        int id=-1;
        int state=-1;
        Set<String> keys= map.keySet();
        for(String key:keys){
            if(baseEventNames.contains(key)){
                Map event= (Map) map.get(key);
                id= (int) event.get("id");
                state=(int)event.get("state");
            }
        }
        for(String key:keys){
            levelDbTemplete=LevelDbTemplete.getInstance();
            levelDbTemplete.put(id+"_"+key,map.get(key));
            logger.info("key:"+id+"_"+key);
            logger.info("value:"+map.get(key));
        }

        for(String key:keys){
            if(!baseEventNames.contains(key)){
                Map event= (Map) map.get(key);
                event.put("id",id);
                event.put("state",state);
                map.put(key,event);
            }
        }

        redisStrategy.put(map);


    }
}
