package com.ibt.lightnode.pojo;

import java.util.ArrayList;
import java.util.List;

/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.pojo
 * @Author: keer
 * @CreateTime: 2020-04-05 14:44
 * @Description: eth_getTransactionReceipt接口返回值
 */
public class TransactionReceipt {
    private String blockHash;
    private String blockNumber;
    private byte[] transactionHash;
    private String transactionIndex;
    private String from;
    private String to;
    private byte[] root;
    private long status;
    private long cumulativeGasUsed;
    private byte[] logsBloom;
    private ArrayList<Log> logs;
    private byte[] contractAddress;
    private long gasUsed;
    public TransactionReceipt() {
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public String getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(String blockNumber) {
        this.blockNumber = blockNumber;
    }

    public byte[] getTransactionHash() {
        return transactionHash;
    }

    public void setTransactionHash(byte[] transactionHash) {
        this.transactionHash = transactionHash;
    }

    public String getTransactionIndex() {
        return transactionIndex;
    }

    public void setTransactionIndex(String transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
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

    public void setStatus(String status) {
        this.status = Long.parseLong(status.substring(2,status.length()),16);
    }

    public long getCumulativeGasUsed() {
        return cumulativeGasUsed;
    }

    public void setCumulativeGasUsed(String cumulativeGasUsed) {
        this.cumulativeGasUsed = Long.parseLong(cumulativeGasUsed.substring(2,cumulativeGasUsed.length()),16);
    }

    public byte[] getLogsBloom() {
        return logsBloom;
    }

    public void setLogsBloom(byte[] logsBloom) {
        this.logsBloom = logsBloom;
    }

    public ArrayList<Log> getLogs() {
        return logs;
    }

    public void setLogs(ArrayList<Log> logs) {
        this.logs = logs;
    }

    public byte[] getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        contractAddress=contractAddress.substring(2);
        this.contractAddress = contractAddress.getBytes();
    }

    public long getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(String gasUsed) {
        this.gasUsed = Long.parseLong(gasUsed.substring(2,gasUsed.length()),16);
    }
}
