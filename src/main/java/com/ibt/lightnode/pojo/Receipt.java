package com.ibt.lightnode.pojo;

import java.util.List;

/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.pojo
 * @Author: keer
 * @CreateTime: 2020-04-17 14:56
 * @Description: 计算 receipt hash
 */
public class Receipt {
    private byte[] root;
    private long status;
    private long cumulativeGasUsed;
    private byte[] logsBloom;
    private byte[] transactionHash;
    private byte[] contractAddress;
    private long gasUsed;
    private List<Log> logs;

    public Receipt(byte[] root, long status, long cumulativeGasUsed, byte[] logsBloom, byte[] transactionHash, byte[] contractAddress, long gasUsed, List<Log> logs) {
        this.root = root;
        this.status = status;
        this.cumulativeGasUsed = cumulativeGasUsed;
        this.logsBloom = logsBloom;
        this.transactionHash = transactionHash;
        this.contractAddress = contractAddress;
        this.gasUsed = gasUsed;
        this.logs = logs;
    }

    public byte[] getRoot() {
        return root;
    }

    public void setRoot(byte[] root) {
        this.root = root;
    }

    public long getStatus() {
        return status;
    }

    public void setStatus(long status) {
        this.status = status;
    }

    public long getCumulativeGasUsed() {
        return cumulativeGasUsed;
    }

    public void setCumulativeGasUsed(long cumulativeGasUsed) {
        this.cumulativeGasUsed = cumulativeGasUsed;
    }

    public byte[] getLogsBloom() {
        return logsBloom;
    }

    public void setLogsBloom(byte[] logsBloom) {
        this.logsBloom = logsBloom;
    }

    public byte[] getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(byte[] transactionHash) {
        this.transactionHash = transactionHash;
    }

    public byte[] getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(byte[] contractAddress) {
        this.contractAddress = contractAddress;
    }

    public long getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(long gasUsed) {
        this.gasUsed = gasUsed;
    }

    public List<Log> getLogs() {
        return logs;
    }

    public void setLogs(List<Log> logs) {
        this.logs = logs;
    }

}
