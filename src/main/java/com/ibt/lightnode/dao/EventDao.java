package com.ibt.lightnode.dao;

import com.ibt.lightnode.util.LevelDbTemplete;
import com.ibt.lightnode.util.SimpleRedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Value("#{'${base_event_names}'.split(',')}")
    private List<String> baseEventNames;

    public void setEventData(String key, Object data){
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
        Map eventData=new HashMap();
        eventData.put(eventName,data);
        redisTemplate.sSet(id,eventData);

    }

}
