package com.ibt.lightnode.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.DBFactory;
import org.iq80.leveldb.DBIterator;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.impl.Iq80DBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.dao
 * @Author: keer
 * @CreateTime: 2020-05-06 20:30
 * @Description:
 */
@Component
public class LevelDbTemplete {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private DB db = null;
    @Value("${LevelDB_filePath}")
    private String dbFolder = "db/lightNode";
    private String charset = "utf-8";

    private volatile static LevelDbTemplete uniqueInstance;

    private LevelDbTemplete() {
    }

    public static LevelDbTemplete getInstance() {
        if (uniqueInstance == null) {
            synchronized (LevelDbTemplete.class) {
                if (uniqueInstance == null) {
                    uniqueInstance = new LevelDbTemplete();
                    uniqueInstance.initLevelDB();
                }
            }
        }
        return uniqueInstance;
    }

    /**
     * 初始化LevelDB
     * 每次使用levelDB前都要调用此方法，无论db是否存在
     */
    private void initLevelDB() {
        DBFactory factory = new Iq80DBFactory();
        Options options = new Options();
        options.createIfMissing(true);
        try {
            this.db = factory.open(new File(dbFolder), options);
        } catch (IOException e) {
            logger.error("levelDB启动异常", e);
        }
    }

    /**
     * 基于fastjson的对象序列化
     *
     * @param obj
     * @return
     */
    private byte[] serializer(Object obj) {
        byte[] jsonBytes = JSON.toJSONBytes(obj, new SerializerFeature[]{SerializerFeature.DisableCircularReferenceDetect});
        return jsonBytes;

    }

    /**
     * 基于fastJson的对象反序列化
     *
     * @param bytes
     * @return
     */
    private Object deserializer(byte[] bytes) {
        String str = new String(bytes);
        return JSON.parse(str);
    }

    /**
     * 存放数据
     *
     * @param key
     * @param val
     */

    public void put(String key, Object val) {

        try {
            this.db.put(key.getBytes(charset), this.serializer(val));
        } catch (UnsupportedEncodingException e) {
            logger.error("编码转化异常", e);
        }
    }

    /**
     * 根据key获取数据
     *
     * @param key
     * @return
     */
    public Object get(String key) {

        byte[] val = null;
        try {
            val = db.get(key.getBytes(charset));
        } catch (Exception e) {
            logger.error("levelDB get error", e);
            closeDB();
            return null;
        }
        if (val == null) {
            closeDB();
            return null;
        }

        return deserializer(val);
    }

    /**
     * 根据key删除数据
     *
     * @param key
     */
    public void delete(String key) {

        try {
            db.delete(key.getBytes(charset));
        } catch (Exception e) {
            logger.error("levelDB delete error", e);
        }

    }


    /**
     * 关闭数据库连接
     * 每次只要调用了initDB方法，就要在最后调用此方法
     */
    public static void closeDB() {
        if(uniqueInstance!=null){
            synchronized (LevelDbTemplete.class){
                if(uniqueInstance!=null){
                    if (uniqueInstance.db != null) {
                        try {
                            uniqueInstance.db.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    uniqueInstance=null;
                }
            }
        }

    }

    /**
     * 获取所有key
     *
     * @return
     */
    public List<String> getKeys() {

        List<String> list = new ArrayList<>();
        DBIterator iterator = null;
        try {
            iterator = db.iterator();
            while (iterator.hasNext()) {
                Map.Entry<byte[], byte[]> item = iterator.next();
                String key = new String(item.getKey(), charset);
                list.add(key);
            }
        } catch (Exception e) {
            logger.error("遍历发生异常", e);
        } finally {
            if (iterator != null) {
                try {
                    iterator.close();

                } catch (IOException e) {
                    logger.error("遍历发生异常", e);
                }

            }
        }
        return list;
    }

    public static void main(String[] args) {
        LevelDbTemplete levelDbTemplete = LevelDbTemplete.getInstance();
        for (String key : levelDbTemplete.getKeys()) {
            System.out.println(key + ":" + levelDbTemplete.get(key));
        }
        LevelDbTemplete.closeDB();
    }

}
