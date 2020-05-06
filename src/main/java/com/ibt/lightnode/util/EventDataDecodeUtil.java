package com.ibt.lightnode.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ibt.lightnode.pojo.Log;
import com.ibt.lightnode.pojo.TransactionReceipt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.*;

/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.util
 * @Author: keer
 * @CreateTime: 2020-05-04 14:02
 * @Description:
 */
@Component
public class EventDataDecodeUtil {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Value("#{'${base_event_names}'.split(',')}")
    private List<String> baseEventNames;
    /**
     * 解析receipt中log中的data字段
     *
     * @param data
     */
    public Map decodeReceiptData(String data) {
        Map map = new HashMap();
        byte[] eventData = decodeByBase64(data);
        byte[] eventNameBytes = subBytes(eventData, 0, 31);
        int index = Integer.parseInt(binary(eventNameBytes, 10));
        String eventName = findStringValueByIndex(index, eventData);
        JSONArray jsonArray = JSON.parseArray(readFile("src/main/java/com/ibt/lightnode/contract/Source/source.abi"));
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
            String type = (String) jsonObject.get("type");
            String name = (String) jsonObject.get("name");
            if (type.equals("event") && name.equals(eventName)) {
                JSONArray inputs = (JSONArray) jsonObject.get("inputs");
                for (int j = 1; j < inputs.size(); j++) {

                    JSONObject input = (JSONObject) inputs.get(j);
                    byte[] valueByte = subBytes(eventData, 32 * j, 32 * j + 31);
                    if (input.get("type").equals("uint256")) {
                        int value = Integer.parseInt(binary(valueByte, 10));
                        map.put(input.get("name"), value);
                    } else if (input.get("type").equals("address")) {
                        String address = "0x" + binary(valueByte, 16);
                        map.put(input.get("name"), address);
                    } else if (input.get("type").equals("string")) {
                        int stringIndex = Integer.parseInt(binary(valueByte, 10));
                        String res = findStringValueByIndex(stringIndex, eventData);
                        map.put(input.get("name"), res);
                    }
                }
            }
        }

        Map res=new HashMap();
        res.put(eventName,map);
        res.put("id",-1);
        res.put("eventName",eventName);
        for(String name:baseEventNames){
            if(name.equals(eventName)){
                res.put("id",map.get("id"));
            }
        }
        return res;

    }

    /**
     * 截取byte数组
     *
     * @param data  原始数组
     * @param start 开始位置，数组下标
     * @param end   结束位置，数组下标
     * @return
     */
    private byte[] subBytes(byte[] data, int start, int end) {
        byte[] res = new byte[end - start + 1];
        for (int i = 0; i < res.length; i++) {
            res[i] = data[start + i];
        }
        return res;
    }

    /**
     * base64 解码
     *
     * @param data
     * @return
     */
    private byte[] decodeByBase64(String data) {
        final Base64.Decoder decoder = Base64.getDecoder();
        return decoder.decode(data);
    }

    /**
     * 将byte数组转化为不同的进制的数
     *
     * @param bytes
     * @param radix 进制数
     * @return
     */
    public String binary(byte[] bytes, int radix) {
        // 这里的1代表正数
        return new BigInteger(1, bytes).toString(radix);
    }

    /**
     * 通过偏移量解析String
     *
     * @param index 偏移量
     * @param data  原始数据
     * @return
     */
    private String findStringValueByIndex(int index, byte[] data) {
        byte[] stringValueBytes = subBytes(data, index + 32, index + 63);
        String res;
        try {
            res = new String(stringValueBytes, "UTF-8");
            byte[] stringValueLength = subBytes(data, index, index + 31);
            int length = Integer.parseInt(binary(stringValueLength, 10));
            res = res.substring(0, length);
            return res;
        } catch (Exception e) {
            logger.error("事件名称解析失败");
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 读取abi文件
     *
     * @param abiFile
     * @return
     */
    private String readFile(String abiFile) {
        File file = new File(abiFile);
        if (file.isFile() && file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuffer sb = new StringBuffer();
                String text = null;
                while ((text = bufferedReader.readLine()) != null) {
                    sb.append(text);
                }
                inputStreamReader.close();
                fileInputStream.close();
                return sb.toString();
            } catch (Exception e) {
                logger.info("文件读取失败");
                return null;
            }
        } else {
            logger.info("文件路径不存在！！！！");
            return null;
        }
    }

    public static void main(String[] args) {
        EventDataDecodeUtil decode = new EventDataDecodeUtil();
//        String path ="src/main/java/com/ibt/lightnode/contract/Source/source.abi";
//        String data= decode.readFile(path);
//        JSONArray jsonArray=JSON.parseArray(data);
//        System.out.println(jsonArray);
        HttpUtil httpUtil = new HttpUtil();
        TransactionReceipt receipt = httpUtil.eth_getTransactionReceipt("0x234fa87fd4a0a3809699f921df23c467591d026c5351965b5980e570dae9b8c4");

        List logs = receipt.getLogs();
        Log log = (Log) logs.get(0);
        String contadd=decode.binary(log.getAddress(),16);
        System.out.println(contadd);
        String bytes = log.getData();
        Map name = decode.decodeReceiptData(bytes);
        System.out.println(name.toString());


    }
}

