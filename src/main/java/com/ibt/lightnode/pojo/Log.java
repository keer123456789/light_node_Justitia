package com.ibt.lightnode.pojo;

import java.util.List;

/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.pojo
 * @Author: keer
 * @CreateTime: 2020-04-05 14:36
 * @Description: receipt中的log对象
 */
public class Log {
    private byte[] address;
    private List<byte[]> topics;
    private byte[] data;
    private int blockNumber;
    private byte[] transactionHash;
    private int transactionIndex;
    private byte[] blockHash;
    private int logIndex;
    private boolean removed;

    public Log() {
    }

    public byte[] getAddress() {
        return address;
    }

    public void setAddress(byte[] address) {
        this.address = address;
    }

    public List<byte[]> getTopics() {
        return topics;
    }

    public void setTopics(List<byte[]> topics) {
        this.topics = topics;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(int blockNumber) {
        this.blockNumber = blockNumber;
    }

    public byte[] getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(byte[] transactionHash) {
        this.transactionHash = transactionHash;
    }

    public int getTransactionIndex() {
        return transactionIndex;
    }

    public void setTransactionIndex(int transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

    public byte[] getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(byte[] blockHash) {
        this.blockHash = blockHash;
    }

    public int getLogIndex() {
        return logIndex;
    }

    public void setLogIndex(int logIndex) {
        this.logIndex = logIndex;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }
}