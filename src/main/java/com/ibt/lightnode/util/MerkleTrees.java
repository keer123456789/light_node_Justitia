package com.ibt.lightnode.util;

import com.alibaba.fastjson.JSON;
import com.ibt.lightnode.pojo.Receipt;
import com.ibt.lightnode.pojo.TransactionReceipt;
import fr.cryptohash.Digest;
import fr.cryptohash.Keccak256;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.util
 * @Author: keer
 * @CreateTime: 2020-04-17 15:30
 * @Description:
 */
public class MerkleTrees {


    private long depth;
    private MerkleTreeNode root;
    private List<byte[]> hashes;

    public MerkleTrees(List<byte[]> hashes) {
        this.hashes = hashes;
    }

    /**
     * execute merkle_tree and set root.
     */
    public String merkle_tree() {

        if (hashes.size() == 0) {
            return null;
        }

        if (hashes.size() == 1) {
            this.root = new MerkleTreeNode(hashes.get(0));
            StringBuilder sb = new StringBuilder(2 * hashes.get(0).length);
            for (byte b : hashes.get(0)) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        }

        /**
         * 生成叶子节点
         */
        List<MerkleTreeNode> nodes = generateLeaves();

        int height = 1;
        for (; nodes.size() > 1; ) {
            nodes = levelUp(nodes);
            height++;
        }

        this.depth = height;
        this.root = nodes.get(0);
        StringBuilder sb = new StringBuilder(2 * root.getHash().length);
        for (byte b : root.getHash()) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();


    }

    /**
     * 生成叶子节点
     *
     * @return
     */
    private List<MerkleTreeNode> generateLeaves() {
        List<MerkleTreeNode> nodes = new ArrayList<>();
        for (byte[] hash : hashes) {
            MerkleTreeNode node = new MerkleTreeNode(hash);
            nodes.add(node);
        }
        return nodes;
    }

    /**
     * 计算下一层的哈希
     *
     * @param list
     * @return
     */
    private List<MerkleTreeNode> levelUp(List<MerkleTreeNode> list) {
        List<MerkleTreeNode> nextLevel = new ArrayList<>();
        for (int i = 0; i < list.size() / 2; i++) {
            byte[] data = concat(list.get(i * 2).getHash(), list.get(i * 2 + 1).getHash());
            byte[] hash = countHash(data);
            nextLevel.add(new MerkleTreeNode(hash, list.get(i * 2), list.get(i * 2 + 1)));
        }

        if (list.size() % 2 == 1) {
            byte[] data = concat(list.get(list.size() - 1).getHash(), list.get(list.size() - 1).getHash());
            byte[] hash = countHash(data);
            nextLevel.add(new MerkleTreeNode(hash, list.get(list.size() - 1), list.get(list.size() - 1)));
        }
        return nextLevel;
    }

    /**
     * 合并byte数组
     *
     * @param first
     * @param second
     * @return
     */
    private byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }


    /**
     * 计算哈希
     *
     * @param data
     * @return
     */
    private byte[] countHash(byte[] data) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        md.update(data);
        return md.digest();
    }

    /**
     * Return hex string
     *
     * @param str
     * @return
     */
    public static byte[] getSHA2HexValue(String str) {
        byte[] cipher_byte;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(str.getBytes());
            cipher_byte = md.digest();

            return cipher_byte;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] getKeccak256Value(String str) {
        Digest sha3 = new Keccak256();
        return sha3.digest(str.getBytes());


    }

    /**
     * Get Root
     *
     * @return
     */
    public String getRoot() {
        StringBuilder sb = new StringBuilder(2 * (this.root.getHash().length));
        for (byte b : root.getHash()) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        HttpUtil httpUtil = new HttpUtil();
        TransactionReceipt txReceipt = httpUtil.eth_getTransactionReceipt("0xb58e05bd72ae6e2a6e4ad669a789239ab3a24cbaa085fc5ab5213e7269781cff");
        Receipt receipt =
                new Receipt(txReceipt.getRoot(),
                        txReceipt.getStatus(),
                        txReceipt.getCumulativeGasUsed(),
                        txReceipt.getLogsBloom(),
                        txReceipt.getTransactionHash(),
                        txReceipt.getContractAddress(),
                        txReceipt.getGasUsed(),
                        txReceipt.getLogs());
        String hash = JSON.toJSONString(receipt);

        byte[] cipher_byte = getSHA2HexValue(hash);

        ArrayList<byte[]> list = new ArrayList<>();
        list.add(cipher_byte);
        MerkleTrees trees = new MerkleTrees(list);
        trees.merkle_tree();
        System.out.println(trees.getRoot());


    }
}

