package com.ibt.lightnode.service;

/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.service
 * @Author: keer
 * @CreateTime: 2020-04-06 19:34
 * @Description:
 */
public interface UpdateReceiptService {
    /**
     * 检查全节点块高和本地块高，确定同步的起始点
     * 如果本地块高=0 =null >全节点块高，在leveldb中设置当前块高为0，return 0；
     * 其他情况返回当前块高+1
     *
     * @return 返回开始同步的起点块高
     */
    int checkBlockHeight();

    /**
     * 同步receipt
     * @param startHeight 同步位置，开始同步的块高
     */
    void updateReceipt(int startHeight);
}
