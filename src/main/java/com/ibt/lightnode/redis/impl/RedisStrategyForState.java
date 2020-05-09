package com.ibt.lightnode.redis.impl;

import com.ibt.lightnode.pojo.RedisKeyInfo;
import com.ibt.lightnode.redis.RedisStrategy;
import com.ibt.lightnode.util.LevelDbTemplete;
import com.ibt.lightnode.util.SimpleRedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.redis
 * @Author: keer
 * @CreateTime: 2020-05-08 15:15
 * @Description: redis存储策略 状态存储码
 */
@Component
public class RedisStrategyForState implements RedisStrategy {
    @Autowired
    SimpleRedisTemplate redisTemplate;
    @Autowired
    LevelDbTemplete levelDbTemplete;

    @Value("#{'${base_event_names}'.split(',')}")
    private List<String> baseEventNames;

    private final String KEY_INFO = "keyInfo";

    private final int CREATE = 1;
    private final int CHANGE = 2;
    private final int TRANSFER = 3;
    private final int REMOVE = 4;

    //TODO 第一个需要从配置中读取，其余两个需要通过以太坊事件获取。
    /**
     * 规定redis中存储多少个数据
     */
    private int keySize = 3;
    /**
     * 交易状态
     */
    private int transferState = 10;
    /**
     * 移除状态
     */
    private int removeState = 20;

    @Override
    public void put(Map eventDatas) {

        for (Object o : eventDatas.keySet()) {
            String key = (String) o;
            Map eventData = (Map) eventDatas.get(key);
            updateKeyInfo(eventData);
        }
    }

    /**
     * 最终将evnetdata数据写入redis
     *
     * @param id
     * @param value
     */
    private void writeEventData(String id, Object value) {
        redisTemplate.del(id);

        List<String>keys=LevelDbTemplete.getInstance().getKeys();
        for(String key:keys){
            String[] values=key.split("_");
            if(values.length==1){
                continue;
            }
            if(values[0].equals(id)){
                Map map= (Map) LevelDbTemplete.getInstance().get(key);
                redisTemplate.sSet(id, map);
            }
        }
//        redisTemplate.sSet(id, value);
    }


    /**
     * 更新redis数据库中key值为“keyInfo”的数据
     *
     * @param eventData
     */
    private void updateKeyInfo(Map eventData) {

        int id = (int) eventData.get("id");
        int currentState = (int) eventData.get("state");
        long currentKeySize = redisTemplate.zGetCount(KEY_INFO);


        Set<RedisKeyInfo> redisKeyInfos = redisTemplate.zRangeByScore(KEY_INFO, 1, transalteStateToScore(currentState));
        List<RedisKeyInfo> createKeyInfo = new ArrayList<>();
        List<RedisKeyInfo> changeKeyInfo = new ArrayList<>();
        List<RedisKeyInfo> transferKeyInfo = new ArrayList<>();

        for (RedisKeyInfo redisKeyInfo : redisKeyInfos) {
            switch (transalteStateToScore(redisKeyInfo.getState())) {
                case CREATE: {
                    createKeyInfo.add(redisKeyInfo);
                    break;
                }
                case CHANGE: {
                    changeKeyInfo.add(redisKeyInfo);
                    break;
                }
                default: {
                    transferKeyInfo.add(redisKeyInfo);
                    break;
                }

            }
        }

        /**
         * 第一步：检查当前的redis中的存储数据的个数currentKeySize
         * currentKeySize<keySize 直接增加
         * currentKeySize>=keySize 根据策略删除
         *
         */

        if (currentKeySize < keySize) {
            if (currentKeySize == 0) {
                writeKeyInfo(id, currentState);
                writeEventData(id + "", eventData);
            } else {
                int sum = 0;
                for (RedisKeyInfo redisKeyInfo : redisKeyInfos) {
                    if (redisKeyInfo.getId() == id) {
                        redisTemplate.zRemove(KEY_INFO, redisKeyInfo);
                        writeKeyInfo(id, currentState);
                        writeEventData(id + "", eventData);
                    } else {
                        sum++;
                    }
                }
                if (sum == redisKeyInfos.size()) {
                    writeKeyInfo(id, currentState);
                    writeEventData(id + "", eventData);
                }
            }
            return;
        }


        /**
         * keyInfo 表满
         */
        int count = 0;
        /**
         * 查看id是否在keyInfo中
         */
        for (RedisKeyInfo redisKeyInfo : redisKeyInfos) {
            if (redisKeyInfo.getId() == id) {
                redisTemplate.zRemove(KEY_INFO, redisKeyInfo);
                writeKeyInfo(id, currentState);
                return;
            } else {
                count++;
            }
        }
        /**
         * 如果id不在
         * 如果是新RedisKeyInfo 加入到已经满的keyInfo中
         */
        if (count == redisKeyInfos.size()) {
            if (redisKeyInfos.size() == 0) {
                return;
            } else {
                if (createKeyInfo.size() != 0) {
                    RedisKeyInfo remov = findLastUse(createKeyInfo);
                    redisTemplate.zRemove(KEY_INFO, remov);
                    redisTemplate.del(remov.getId() + "");
                    writeKeyInfo(id, currentState);
                    writeEventData(id + "", eventData);
                    return;
                }
                if (changeKeyInfo.size() != 0) {
                    RedisKeyInfo remov = findLastUse(changeKeyInfo);
                    redisTemplate.zRemove(KEY_INFO, remov);
                    redisTemplate.del(remov.getId() + "");
                    writeKeyInfo(id, currentState);
                    writeEventData(id + "", eventData);
                    return;
                }
                if (transferKeyInfo.size() != 0) {
                    RedisKeyInfo remov = findLastUse(transferKeyInfo);
                    redisTemplate.zRemove(KEY_INFO, remov);
                    redisTemplate.del(remov.getId() + "");
                    writeKeyInfo(id, currentState);
                    writeEventData(id + "", eventData);
                    return;
                }

            }
        }


    }

    /**
     * 将新的id信息写入redis
     *
     * @param id
     * @param currentState
     */
    private void writeKeyInfo(int id, int currentState) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = df.format(new Date());
        RedisKeyInfo redisKeyInfo = new RedisKeyInfo(time, id, currentState);
        redisTemplate.zAdd(KEY_INFO, redisKeyInfo, transalteStateToScore(currentState));
    }

    /**
     * 将状态值转化为比重
     *
     * @param state
     * @return
     */
    private int transalteStateToScore(int state) {
        if (state == 0) {
            return CREATE;
        }
        if (state > 0 && state < transferState) {
            return CHANGE;
        }
        if (state >= transferState && state < removeState) {
            return TRANSFER;
        }
        if (state == removeState) {
            return REMOVE;
        }
        return 0;
    }

    /**
     * 寻找最久未使用的key
     *
     * @param set
     * @return
     */
    private RedisKeyInfo findLastUse(List<RedisKeyInfo> set) {
        RedisKeyInfo info = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long time = System.currentTimeMillis();
        for (RedisKeyInfo redisKeyInfo : set) {
            long lastTime = 0;
            try {
                lastTime = df.parse(redisKeyInfo.getLastUseTime()).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (lastTime <= time) {
                time = lastTime;
                info = redisKeyInfo;
            }

        }
        return info;
    }

    public static void main(String[] args) {
        String id="1info";
        String[] ids=id.split("_");
        System.out.println(ids.length);
    }
}
