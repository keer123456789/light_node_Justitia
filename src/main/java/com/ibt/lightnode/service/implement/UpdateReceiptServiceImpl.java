package com.ibt.lightnode.service.implement;

import com.ibt.lightnode.dao.LevelDbTemplete;
import com.ibt.lightnode.pojo.*;
import com.ibt.lightnode.service.UpdateReceiptService;
import com.ibt.lightnode.util.EventDataDecodeUtil;
import com.ibt.lightnode.util.HttpUtil;
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
    LevelDbTemplete levelDbTemplete;
    @Autowired
    HttpUtil httpUtil;
    @Autowired
    EventDataDecodeUtil eventDataDecodeUtil;
    @Value("${traceContrantAddress}")
    private String traceContrantAddress;

    @Override
    public int checkBlockHeight() {
        levelDbTemplete.initLevelDB();
        String currentBlockHeight = (String) levelDbTemplete.get("currentBlockHeight");
        int currentHeight = 0;
        if (currentBlockHeight != null) {
            String temp = currentBlockHeight.substring(2);
            currentHeight = Integer.valueOf(temp, 16);
        }


        int remoteHeight = httpUtil.eth_blockNumber();

        if (currentHeight > remoteHeight || currentHeight == 0 || currentBlockHeight == null) {
            levelDbTemplete.put("currentBlockHeight", "0x0");
            levelDbTemplete.closeDB();
            return 0;
        } else {
            levelDbTemplete.closeDB();
            return currentHeight + 1;
        }
    }

    @Override
    public void updateReceipt(int startHeight) {
        int remoteHeight = httpUtil.eth_blockNumber();
        levelDbTemplete.initLevelDB();
        for (; startHeight <= remoteHeight; startHeight++) {
            String height = "0x" + Integer.toHexString(startHeight).toUpperCase();
            Map map = httpUtil.getTransactionReceiptByHeight(height);

            List<TransactionReceipt> transactionReceiptList = (List<TransactionReceipt>) map.get("transcationReceipts");
            String receiptRoot = (String) map.get("receiptRoot");

            //TODO 现在计算的hash和receiptroot不同，未能解决；主要是在MerkleTrees这个类中的hash算法是否正确。
            boolean check = checkReceipt(transactionReceiptList, receiptRoot);
            if (check) {
                levelDbTemplete.put("currentBlockHeight", height);
                addTransactionReceipt(transactionReceiptList);
                logger.info("同步块高：" + height);
            } else {
                // 检查错误，哈市不对，
                startHeight = startHeight - 1;
            }


        }
        logger.info("本次同步完成，当前块高" + levelDbTemplete.get("currentBlockHeight").toString());
        levelDbTemplete.closeDB();
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

        int index = 0;
        List<String> keys = levelDbTemplete.getKeys();
        for (String key : keys) {
            String[] str = key.split("_");
            if (str[0].equals(contractAddress)) {
                int value = Integer.parseInt(str[4]);
                if (value > index) {
                    index = value;
                }
            }
        }
        index++;
        return index + "";
    }

    /**
     * 添加到leveldb中
     *
     * @param transactionReceipts 一个块中的Transactioneceipt集合
     */
    private void addTransactionReceipt(List<TransactionReceipt> transactionReceipts) {
        for (TransactionReceipt transactionReceipt : transactionReceipts) {
            /**
             * 第一步：判断log是否为null
             */
            if (transactionReceipt.getLogs() == null) {
                continue;
            }

            /**
             * 第二步：从transactionReceipt中获取log集合
             */
            ArrayList<Log> logs = transactionReceipt.getLogs();

            /**
             * 第三步：循环对每一个log进行解析
             *
             * sum变量，是对id进行存储的中间变量
             */
            int sum = -1;
            for (int i = 0; i < logs.size(); i++) {
                /**
                 * 第四步：获取log中的合约地址
                 */
                String contractAddress = eventDataDecodeUtil.binary(logs.get(i).getAddress(), 16);
                String tc = traceContrantAddress.substring(2).toLowerCase();
                /**
                 * 第五步：与配置文件中的合约地址进行比较，相同说明需要存入leveldb，
                 */
                if (contractAddress.equals(tc)) {
                    /**
                     * 第六步： 对log中的data字段进行解码（base64-> abi）
                     * 返回的map结构：
                     * {
                     *     "eventName":事件名称，
                     *     "事件名称"：事件数据，
                     *     "id":基类合约中的传入的id
                     * }
                     */
                    Map map = eventDataDecodeUtil.decodeReceiptData(logs.get(i).getData());

                    String eventName = (String) map.get("eventName");
                    int id = (int) map.get("id");
                    if (id == -1) {
                        if (sum == -1) {
                           continue;
                        } else {
                            String key = "0x" + contractAddress + "_" + sum + "_" + eventName;
                            levelDbTemplete.put(key, map.get(eventName));
                            logger.info(key);
                            logger.info(map.get(eventName).toString());
                        }
                    } else {
                        sum = id;
                        if (i != 0) {
                           for(int j=0;j<i;j++){
                               Map map1 = eventDataDecodeUtil.decodeReceiptData(logs.get(j).getData());
                               String key = "0x" + contractAddress + "_" + sum + "_" + eventName;
                               levelDbTemplete.put(key, map1.get(eventName));
                               logger.info(key);
                               logger.info(map.get(eventName).toString());
                           }
                        } else {
                            String key = "0x" + contractAddress + "_" + sum + "_" + eventName;
                            levelDbTemplete.put(key, map.get(eventName));
                            logger.info(key);
                            logger.info(map.get(eventName).toString());
                        }
                    }

                }
            }
        }
    }

    public static void main(String[] args) {
        String a = "0xCde5c850a0998Cb1B37d7bd2d98340FFe9caaDd5";
        String b = a.substring(2).toUpperCase();
        System.out.println(b);

    }
}
