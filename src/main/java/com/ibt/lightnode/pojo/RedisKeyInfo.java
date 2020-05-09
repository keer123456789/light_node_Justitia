package com.ibt.lightnode.pojo;

/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.pojo
 * @Author: keer
 * @CreateTime: 2020-05-08 22:07
 * @Description:
 */
public class RedisKeyInfo {
    private String lastUseTime;
    private int id;
    private int state;

    public RedisKeyInfo(String lastUseTime, int id, int state) {
        this.lastUseTime = lastUseTime;
        this.id = id;
        this.state = state;
    }

    public String getLastUseTime() {
        return lastUseTime;
    }

    public void setLastUseTime(String lastUseTime) {
        this.lastUseTime = lastUseTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
