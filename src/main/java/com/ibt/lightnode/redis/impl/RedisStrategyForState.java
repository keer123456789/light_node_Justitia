package com.ibt.lightnode.redis.impl;

import com.ibt.lightnode.pojo.RedisKeyInfo;
import com.ibt.lightnode.redis.RedisStrategy;
import com.ibt.lightnode.util.LevelDbTemplete;
import com.ibt.lightnode.util.SimpleRedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

            int id = (int) eventData.get("id");
            int state = (int) eventData.get("state");

            if (state == removeState) {
                Set<RedisKeyInfo> keyInfos = redisTemplate.zRange(KEY_INFO, 0, -1);
                for (RedisKeyInfo redisKeyInfo : keyInfos) {
                    if (id == redisKeyInfo.getId()) {
                        redisTemplate.zRemove(KEY_INFO, redisKeyInfo);
                        redisTemplate.del(id + "");
                        return;
                    }
                }

            }

            int keyInfoState = checkKeyInfo(id, state);
            switch (keyInfoState) {
                case 0: {
                    updateRedisFor0Write(id, state);
                }
                break;
                case 1: {
                    updateRedisFor1Write(id, state);
                }
                break;
                case 2: {
                    updateRedisFor2Write(id, state);
                }
                break;
                case 3: {
                    updateRedisFor3Write(id, state);
                }
                break;
                case 5: {
                    updateRedisFor5Write(id, state);
                }
                break;
                case 7: {
                    updateRedisFor7Write(id, state);
                }
                break;
            }
        }
    }

    @Override
    public Map getTraceInfoById(int id) {
        Map map = new HashMap();
        int state = getIdCurrentStateInLevelDB(id);

        if(state==removeState){
            List list=getEvnetInfoFromLevelDb(id);
            map.put(id+"",list);
            return map;
        }


        int keyInfoState = checkKeyInfo(id, state);
        switch (keyInfoState) {
            case 0: {
                map = updateRedisFor0Read(id, state);
            }
            break;
            case 1: {
                map = updateRedisFor1Read(id, state);
            }
            break;
            case 2: {
                map = updateRedisFor2Read(id, state);
            }
            break;
            case 3: {
                map = updateRedisFor3Read(id, state);
            }
            break;
            case 5: {
                map = updateRedisFor5Read(id, state);
            }
            break;
            case 7: {
                map = updateRedisFor7Read(id, state);
            }
            break;
            default:
                map = null;

        }
        return map;
    }

    /**
     * 根据id和state判断 返回id在KEY_INFO中的状态
     *
     * @param id
     * @param state 输入的状态
     * @return 会产生6个状态，0,1,2,3，5,7 ，分别转化为三位二进制数：
     * 第一位（最低位 isExist）：代表输入的id是否在表中，是：1，否：0
     * 第二位（中间位 isFull）：代表KEY_INFO表是否已经满，是：1，否：0
     * 第三位（最高位 isEqual）：输入状态与表中对应id的状态的关系，只有在第一位为1的时候，这位才有效 1：大于，0：等于
     * 下面是6个状态各自代表的意义：
     * <p>
     * =0 KEY_INFO没有满 id不在表中    000
     * =4 无意义  id不在表中           100
     * <p>
     * =1 KEY_INFO没有满 id在表中 输入状态=表中状态   001
     * =5 KEY_INFO没有满 id在表中 输入状态>表中状态   101
     * <p>
     * =2 KEY_INFO满，id不在表中  010
     * =6 无意义  id不在表中      110
     * <p>
     * =3 KEY_INFO满，id在表中  输入状态=表中状态    011
     * =7 KEY_INFO满，id在表中  输入状态>表中状态    111
     */
    private int checkKeyInfo(int id, int state) {
        long currentKeySize = redisTemplate.zGetCount(KEY_INFO);
        int isFull = 0;
        int isExist = 0;
        int isEqual = 0;
        /**
         * 判断表是否 满
         */
        if (currentKeySize == keySize) {
            isFull = 1;
        }

        /**
         * 判断id是否在KEY_INFO中
         * 同时判断输入状态和表中状态
         */
        Set<RedisKeyInfo> redisKeyInfos = redisTemplate.zRange(KEY_INFO, 0, -1);
        for (RedisKeyInfo redisKeyInfo : redisKeyInfos) {
            if (redisKeyInfo.getId() == id) {
                isExist = 1;
                if (redisKeyInfo.getState() < state) {
                    isEqual = 1;
                }
            }
        }

        String result = isEqual + "" + isFull + "" + isExist;
        return Integer.parseInt(result, 2);
    }


    /**
     * 通过id查找Leveldb中最新状态
     *
     * @param id
     */
    private int getIdCurrentStateInLevelDB(int id) {
        int state = 0;
        LevelDbTemplete levelDbTemplete = LevelDbTemplete.getInstance();
        List<String> keys = levelDbTemplete.getKeys();
        for (String key : keys) {
            String[] strs = key.split("_");
            if (strs.length == 1) {
                continue;
            }
            if (Integer.parseInt(strs[0]) == id && baseEventNames.contains(strs[1])) {
                Map map = (Map) levelDbTemplete.get(key);
                int currentState = (int) map.get("state");
                if (currentState > state) {
                    state = currentState;
                }
            }
        }
        return state;
    }

    /**
     * checkKeyInfo返回2状态，写操作
     *
     * @param id
     * @param state
     */
    private void updateRedisFor2Write(int id, int state) {
        Set<RedisKeyInfo> redisKeyInfos = redisTemplate.zRangeByScore(KEY_INFO, 1, transalteStateToScore(state));
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

        if (createKeyInfo.size() != 0) {
            replace(id, state, createKeyInfo);
            return;
        }
        if (changeKeyInfo.size() != 0) {
            replace(id, state, changeKeyInfo);
            return;
        }
        if (transferKeyInfo.size() != 0) {
            replace(id, state, transferKeyInfo);
            return;
        }
    }

    /**
     * 替换KeyInfo中的id信息
     *
     * @param id
     * @param state
     * @param redisKeyInfos
     */
    private void replace(int id, int state, List<RedisKeyInfo> redisKeyInfos) {
        RedisKeyInfo remov = findLastUse(redisKeyInfos);
        redisTemplate.zRemove(KEY_INFO, remov);
        redisTemplate.del(remov.getId() + "");
        writeKeyInfo(id, state);
        writeEventData(id + "");
    }

    /**
     * checkKeyInfo返回2状态，读操作
     *
     * @param id
     * @param state
     */
    private Map updateRedisFor2Read(int id, int state) {
        List res = new ArrayList();
        LevelDbTemplete levelDbTemplete = LevelDbTemplete.getInstance();
        List<String> keys = levelDbTemplete.getKeys();
        for (String key : keys) {
            String[] strs = key.split("_");
            if (strs.length == 1) {
                continue;
            }
            if (id == Integer.parseInt(strs[0])) {
                res.add(levelDbTemplete.get(key));
            }
        }
        Map map = new HashMap();
        map.put("" + id, res);
        /**
         * 开启线程更新redis
         */
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                updateRedisFor2Write(id, state);
            }
        });
        thread.start();
        return map;
    }

    /**
     * checkKeyInfo返回0状态，读操作
     *
     * @param id
     * @param state
     */
    private Map updateRedisFor0Read(int id, int state) {
        List list = getEvnetInfoFromLevelDb(id);
        Map map = new HashMap();
        map.put(id + "", list);
        /**
         * 开启线程，在redis中添加相关数据
         */
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                writeKeyInfo(id, state);
                for (Object o : list) {
                    writeEventData(id + "");
                }
            }
        });
        thread.start();
        return map;
    }

    /**
     * 根据id从leveldb中查找事件信息
     *
     * @param id
     * @return
     */
    private List getEvnetInfoFromLevelDb(int id) {
        List res = new ArrayList();
        LevelDbTemplete levelDbTemplete = LevelDbTemplete.getInstance();
        List<String> keys = levelDbTemplete.getKeys();
        for (String key : keys) {
            String[] strs = key.split("_");
            if (strs.length == 1) {
                continue;
            }
            if (id == Integer.parseInt(strs[0])) {
                res.add(levelDbTemplete.get(key));
            }
        }
        return res;
    }

    /**
     * checkKeyInfo返回0状态，写操作
     *
     * @param id
     * @param state
     */
    private void updateRedisFor0Write(int id, int state) {
        writeKeyInfo(id, state);
        writeEventData(id + "");
    }

    /**
     * checkKeyInfo返回1状态，读操作
     *
     * @param id
     * @param state
     */
    private Map updateRedisFor1Read(int id, int state) {
        Set set = redisTemplate.sGet("" + id);
        Map map = new HashMap();
        map.put(id + "", set);
        /**
         * 更新KeyInfo表
         */
        writeKeyInfo(id, state);
        return map;
    }

    /**
     * checkKeyInfo返回1状态，写操作
     *
     * @param id
     * @param state
     */
    private void updateRedisFor1Write(int id, int state) {
        writeKeyInfo(id, state);
        writeEventData(id + "");
    }

    /**
     * 出现此种状态，说明redis中的keyInfo表出现问题，更新这个id的信息
     *
     * @param id
     * @param state
     */
    private Map updateRedisFor5Read(int id, int state) {
        writeKeyInfo(id, state);
        writeEventData(id + "");
        Set set = redisTemplate.sGet(id + "");
        Map map = new HashMap();
        map.put(id + "", set);
        return map;
    }

    /**
     * checkKeyInfo返回5状态，写操作
     *
     * @param id
     * @param state
     */
    private void updateRedisFor5Write(int id, int state) {
        writeKeyInfo(id, state);
        writeEventData(id + "");
    }

    /**
     * checkKeyInfo返回3状态，读操作
     *
     * @param id
     * @param state
     */
    private Map updateRedisFor3Read(int id, int state) {
        Set set = redisTemplate.sGet(id + "");
        Map map = new HashMap();
        map.put(id + "", set);
        return map;
    }

    /**
     * checkKeyInfo返回3状态，写操作
     *
     * @param id
     * @param state
     */
    private void updateRedisFor3Write(int id, int state) {
        writeEventData(id + "");
        writeKeyInfo(id, state);
    }

    /**
     * checkKeyInfo返回7状态，读操作
     * 出现此种状态，说明redis中的keyInfo表出现问题，更新这个id的信息
     *
     * @param id
     * @param state
     * @return
     */
    private Map updateRedisFor7Read(int id, int state) {
        writeKeyInfo(id, state);
        writeEventData(id + "");
        Set set = redisTemplate.sGet(id + "");
        Map map = new HashMap();
        map.put(id + "", set);
        return map;
    }

    /**
     * checkKeyInfo返回7状态，写操作
     *
     * @param id
     * @param state
     */
    private void updateRedisFor7Write(int id, int state) {
        writeEventData(id + "");
        writeKeyInfo(id, state);
    }


    /**
     * 将新的id信息写入redis
     *
     * @param id
     * @param currentState
     */
    private void writeKeyInfo(int id, int currentState) {
        Set<RedisKeyInfo> redisKeyInfos = redisTemplate.zRange(KEY_INFO, 0, -1);
        if (redisKeyInfos.size() == 0) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String time = df.format(new Date());
            RedisKeyInfo redisKeyInfo = new RedisKeyInfo(time, id, currentState);
            redisTemplate.zAdd(KEY_INFO, redisKeyInfo, transalteStateToScore(currentState));
            return;
        }
        for (RedisKeyInfo redisKeyInfo : redisKeyInfos) {
            if (redisKeyInfo.getId() == id) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String time = df.format(new Date());
                redisTemplate.zRemove(KEY_INFO, redisKeyInfo);
                redisKeyInfo.setLastUseTime(time);
                redisKeyInfo.setState(currentState);
                redisTemplate.zAdd(KEY_INFO, redisKeyInfo, transalteStateToScore(currentState));
                return;
            }
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String time = df.format(new Date());
        RedisKeyInfo redisKeyInfo = new RedisKeyInfo(time, id, currentState);
        redisTemplate.zAdd(KEY_INFO, redisKeyInfo, transalteStateToScore(currentState));
    }

    /**
     * 最终将evnetdata数据写入redis
     *
     * @param id
     */
    private void writeEventData(String id) {
        redisTemplate.del(id);

        List<String> keys = LevelDbTemplete.getInstance().getKeys();

        for (String key : keys) {
            String[] values = key.split("_");
            if (values.length == 1) {
                continue;
            }
            if (values[0].equals(id)) {
                Map map = (Map) LevelDbTemplete.getInstance().get(key);
                redisTemplate.sSet(id, map);
            }
        }

//        redisTemplate.sSet(id, value);
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


}
