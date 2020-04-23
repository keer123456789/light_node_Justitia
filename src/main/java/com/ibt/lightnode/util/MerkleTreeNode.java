package com.ibt.lightnode.util;

/**
 * @BelongsProject: lightnode
 * @BelongsPackage: com.ibt.lightnode.util
 * @Author: keer
 * @CreateTime: 2020-04-20 15:06
 * @Description:
 */
public class MerkleTreeNode {
    private byte[] hash;
    private MerkleTreeNode left;
    private MerkleTreeNode right;

    public MerkleTreeNode(byte[] hash) {
        this.hash = hash;
    }

    public MerkleTreeNode(byte[] hash, MerkleTreeNode left, MerkleTreeNode right) {
        this.hash = hash;
        this.left = left;
        this.right = right;
    }

    public byte[] getHash() {
        return hash;
    }

    public void setHash(byte[] hash) {
        this.hash = hash;
    }

    public MerkleTreeNode getLeft() {
        return left;
    }

    public void setLeft(MerkleTreeNode left) {
        this.left = left;
    }

    public MerkleTreeNode getRight() {
        return right;
    }

    public void setRight(MerkleTreeNode right) {
        this.right = right;
    }
}
