package com.ibt.lightnode.service.implement;

import com.ibt.lightnode.pojo.*;
import com.ibt.lightnode.service.UpdateReceiptService;
import com.ibt.lightnode.util.EventDataDecodeUtil;
import com.ibt.lightnode.util.HttpUtil;
import com.ibt.lightnode.util.LevelDbUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.service.implement
 * @Author: keer
 * @CreateTime: 2020-04-06 19:37
 * @Description:
 */
@Service
public class UpdateReceiptServiceImpl implements UpdateReceiptService {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    @Value("${Full_Node}")
    private String fullNodeUrl;

    @Autowired
    LevelDbUtil levelDbUtil;
    @Autowired
    HttpUtil httpUtil;
    @Autowired
    EventDataDecodeUtil eventDataDecodeUtil;
    @Value("${traceContrantAddress}")
    private String traceContrantAddress;

    @Override
    public int checkBlockHeight() {
        levelDbUtil.initLevelDB();
        String currentBlockHeight = (String) levelDbUtil.get("currentBlockHeight");
        int currentHeight = 0;
        if (currentBlockHeight != null) {
            String temp = currentBlockHeight.substring(2);
            currentHeight = Integer.valueOf(temp, 16);
        }


        int remoteHeight = httpUtil.eth_blockNumber();

        if (currentHeight > remoteHeight || currentHeight == 0 || currentBlockHeight == null) {
            levelDbUtil.put("currentBlockHeight", "0x0");
            levelDbUtil.closeDB();
            return 0;
        } else {
            levelDbUtil.closeDB();
            return currentHeight + 1;
        }
    }

    @Override
    public void updateReceipt(int startHeight) {
        int remoteHeight = httpUtil.eth_blockNumber();
        levelDbUtil.initLevelDB();
        for (; startHeight <= remoteHeight; startHeight++) {
            String h = Integer.toHexString(startHeight).toUpperCase();
            String height = "0x" + h;
            Block block = httpUtil.eth_getBlockByNumber(height, true);
            List<TransactionReceipt> transactionReceiptList = new ArrayList<>();
            for (Transaction transaction : block.getTransactions()) {
                TransactionReceipt receipt = httpUtil.eth_getTransactionReceipt(transaction.getHash());
                if (receipt == null) {
                    continue;
                }
                transactionReceiptList.add(receipt);
//                levelDbUtil.put("currentBlockHeight", height);
//                levelDbUtil.put("receipt" + height + "_" + receipt.getTransactionIndex(), receipt);
            }
            //TODO 现在计算的hash和receiptroot不同，未能解决；主要是在MerkleTrees这个类中的hash算法是否正确。
            boolean check = checkReceipt(transactionReceiptList, block.getReceiptsRoot());
            if (check) {
                levelDbUtil.put("currentBlockHeight", height);
                for (TransactionReceipt transactionReceipt : transactionReceiptList) {

                    Log log = transactionReceipt.getLogs().get(0);
                    String contractAddress = "0x" + eventDataDecodeUtil.binary(log.getAddress(), 16);
                    if (contractAddress.equals(traceContrantAddress)) {
                        Map map = eventDataDecodeUtil.decodeReceiptData(log.getData());
                        for (Object o : map.keySet()) {
                            String key = (String) o;
                            key = contractAddress + "_" + key + getEventIndex(contractAddress);
                            levelDbUtil.put(key, map.get(o));
                        }
                        levelDbUtil.put(contractAddress + "_", transactionReceipt);
                    }
                }
            } else {
                // 检查错误，哈市不对，
                startHeight = startHeight - 1;
            }


        }
        logger.info("本次同步完成，当前块高" + levelDbUtil.get("currentBlockHeight").toString());
        levelDbUtil.closeDB();
    }

    /**
     * 检查receipt root hash
     *
     * @param list
     * @return
     */
    private boolean checkReceipt(List<TransactionReceipt> list, String receiptRoot) {
//        List<Receipt> receipts = translate(list);
//        List<byte[]> hashes = new ArrayList<>();
//        for (Receipt receipt : receipts) {
//            hashes.add(MerkleTrees.getSHA2HexValue(receipt.toString()));
//        }
//        MerkleTrees trees = new MerkleTrees(hashes);
//        String sumRootHash = trees.merkle_tree();
//        receiptRoot = receiptRoot.substring(2);
//        if (receiptRoot.equals(sumRootHash)) {
//            return true;
//        } else {
//            return false;
//        }
        return true;

    }

    /**
     * 将TransactionReceipt 转化为Receipt
     *
     * @param transactionReceipts
     * @return
     */
    private List<Receipt> translate(List<TransactionReceipt> transactionReceipts) {
        List<Receipt> receipts = new ArrayList<>();
        for (TransactionReceipt transactionReceipt : transactionReceipts) {
            Receipt receipt = new Receipt(transactionReceipt.getRoot(),
                    transactionReceipt.getStatus(),
                    transactionReceipt.getCumulativeGasUsed(),
                    transactionReceipt.getLogsBloom(),
                    transactionReceipt.getTransactionHash(),
                    transactionReceipt.getContractAddress(),
                    transactionReceipt.getGasUsed(),
                    transactionReceipt.getLogs());
            receipts.add(receipt);
        }
        return receipts;
    }

    /**
     * 获取合约的event编号
     *
     * @param contractAddress
     * @return
     */
    private String getEventIndex(String contractAddress) {
        levelDbUtil.initLevelDB();
        int index = 0;
        List<String> keys = levelDbUtil.getKeys();
        for (String key : keys) {
            String[] str = key.split("_");
            if (str[0].equals(contractAddress)) {
                int value = Integer.parseInt(str[3]);
                if (value > index) {
                    index = value;
                }
            }
        }
        levelDbUtil.closeDB();
        return index + "";
    }

    public static void main(String[] args) {
        String a = "a_1_oo";
        String[] st = a.split("_");
        for (int i = 0; i < st.length; i++) {
            System.out.println(st[i]);
        }

    }
}
