package com.xingkaichun.blockchain.core.impl;

import com.xingkaichun.blockchain.core.BlockChainDataBase;
import com.xingkaichun.blockchain.core.BlockChainSynchronizer;
import com.xingkaichun.blockchain.core.ForMinerSynchronizeNodeDataBase;
import com.xingkaichun.blockchain.core.model.Block;
import com.xingkaichun.blockchain.core.utils.atomic.BlockChainCoreConstants;
import com.xingkaichun.blockchain.core.utils.atomic.EqualsUtils;

public class BlockChainSynchronizerDefaultImpl implements BlockChainSynchronizer {

    //需要同步的区块链
    private BlockChainDataBase blockChainDataBase;
    //需要同步的区块链的副本
    private BlockChainDataBase blockChainDataBaseDuplicate;
    //节点同步数据库 TODO 重命名
    private ForMinerSynchronizeNodeDataBase forMinerSynchronizeNodeDataBase;
    //同步其它节点的区块数据:默认同步其它节点区块数据
    private boolean synchronizeBlockChainNodeOption = true;

    public BlockChainSynchronizerDefaultImpl(BlockChainDataBase blockChainDataBase, ForMinerSynchronizeNodeDataBase forMinerSynchronizeNodeDataBase) {
        this.blockChainDataBase = blockChainDataBase;
        this.forMinerSynchronizeNodeDataBase = forMinerSynchronizeNodeDataBase;
    }

    @Override
    public void synchronizeBlockChainNode() throws Exception {
        while (synchronizeBlockChainNodeOption){
            String availableSynchronizeNodeId = forMinerSynchronizeNodeDataBase.getDataTransferFinishFlagNodeId();
            if(availableSynchronizeNodeId == null){
                return;
            }
            synchronizeBlockChainNode(availableSynchronizeNodeId);
        }
    }


    public void pauseSynchronizeBlockChainNode(){
        synchronizeBlockChainNodeOption = false;
    }

    @Override
    public void resumeSynchronizeBlockChainNode() throws Exception {
        synchronizeBlockChainNodeOption = true;
    }

    @Override
    public boolean isActive() throws Exception {
        return synchronizeBlockChainNodeOption;
    }

    private void synchronizeBlockChainNode(String availableSynchronizeNodeId) throws Exception {
        adjustMasterSlave(blockChainDataBase,blockChainDataBaseDuplicate);
        boolean hasDataTransferFinishFlag = forMinerSynchronizeNodeDataBase.hasDataTransferFinishFlag(availableSynchronizeNodeId);
        if(!hasDataTransferFinishFlag){
            return;
        }
        Block block = forMinerSynchronizeNodeDataBase.getNextBlock(availableSynchronizeNodeId);
        if(block != null){
            reduceBlockChain(blockChainDataBaseDuplicate,block.getHeight()-1);
            while(true){
                boolean isBlockApplyToBlockChain = blockChainDataBaseDuplicate.isBlockCanApplyToBlockChain(block);
                if(isBlockApplyToBlockChain){
                    blockChainDataBaseDuplicate.addBlock(block);
                }else {
                    break;
                }
                block = forMinerSynchronizeNodeDataBase.getNextBlock(availableSynchronizeNodeId);
                if(block == null){
                    break;
                }
            }
        }
        forMinerSynchronizeNodeDataBase.deleteTransferData(availableSynchronizeNodeId);
        forMinerSynchronizeNodeDataBase.clearDataTransferFinishFlag(availableSynchronizeNodeId);
        adjustMasterDuplicate(blockChainDataBase,blockChainDataBaseDuplicate);
    }


    private void reduceBlockChain(BlockChainDataBase blockChainDataBase, int blockHeight) throws Exception {
        Block tailBlock = blockChainDataBase.findTailBlock();
        if(tailBlock == null){
            return;
        }
        int currentBlockHeight = tailBlock.getHeight();
        while(currentBlockHeight > blockHeight){
            blockChainDataBase.removeTailBlock();
            tailBlock = blockChainDataBase.findTailBlock();
            if(tailBlock == null){
                return;
            }
            currentBlockHeight = tailBlock.getHeight();
        }
    }

    //region 私有方法
    /**
     * 若blockChainDataBaseMaster的高度小于blockChainDataBaseDuplicate的高度，
     * 则blockChainDataBaseMaster同步blockChainDataBaseDuplicate的区块链数据。
     */
    private void adjustMasterDuplicate(BlockChainDataBase blockChainDataBaseMaster,BlockChainDataBase blockChainDataBaseDuplicate) throws Exception {
        Block masterTailBlock = blockChainDataBaseMaster.findTailBlock() ;
        Block duplicateTailBlock = blockChainDataBaseDuplicate.findTailBlock() ;
        //不需要调整
        if(duplicateTailBlock == null){
            return;
        }
        if(masterTailBlock == null){
            Block block = blockChainDataBaseDuplicate.findBlockByBlockHeight(BlockChainCoreConstants.FIRST_BLOCK_HEIGHT);
            blockChainDataBaseMaster.addBlock(block);
        }
        int masterTailBlockHeight = blockChainDataBaseMaster.findTailBlock().getHeight() ;
        int duplicateTailBlockHeight = blockChainDataBaseDuplicate.findTailBlock().getHeight() ;
        while(true){
            if(masterTailBlockHeight >= duplicateTailBlockHeight){
                break;
            }
            masterTailBlockHeight++;
            Block currentBlock = blockChainDataBaseDuplicate.findBlockByBlockHeight(masterTailBlockHeight) ;
            blockChainDataBaseMaster.addBlock(currentBlock);
        }
    }
    /**
     * 使得blockChainDataBaseSlave和blockChainDataBaseMaster的区块链数据一模一样
     */
    private void adjustMasterSlave(BlockChainDataBase blockChainDataBaseMaster,BlockChainDataBase blockChainDataBaseSlave) throws Exception {
        Block masterTailBlock = blockChainDataBaseMaster.findTailBlock() ;
        Block slaveTailBlock = blockChainDataBaseSlave.findTailBlock() ;
        //不需要调整
        if(masterTailBlock == null){
            return;
        }
        //删除Duplicate区块链直到尚未分叉位置停止
        while(true){
            if(slaveTailBlock == null){
                break;
            }
            if(isBlockEqual(masterTailBlock,slaveTailBlock)){
                break;
            }
            blockChainDataBaseSlave.removeTailBlock();
            slaveTailBlock = blockChainDataBaseSlave.findTailBlock() ;
        }
        if(slaveTailBlock == null){
            Block block = blockChainDataBaseMaster.findBlockByBlockHeight(BlockChainCoreConstants.FIRST_BLOCK_HEIGHT);
            blockChainDataBaseSlave.addBlock(block);
        }
        int masterTailBlockHeight = blockChainDataBaseMaster.findTailBlock().getHeight() ;
        int slaveTailBlockHeight = blockChainDataBaseSlave.findTailBlock().getHeight() ;
        while(true){
            if(slaveTailBlockHeight >= masterTailBlockHeight){
                break;
            }
            slaveTailBlockHeight++;
            Block currentBlock = blockChainDataBaseMaster.findBlockByBlockHeight(slaveTailBlockHeight) ;
            blockChainDataBaseSlave.addBlock(currentBlock);
        }
    }

    private boolean isBlockEqual(Block masterTailBlock, Block slaveTailBlock) {
        if(masterTailBlock == null && slaveTailBlock == null){
            return true;
        }
        if(masterTailBlock == null || slaveTailBlock == null){
            return false;
        }
        //不严格校验,这里没有具体校验每一笔交易
        if(EqualsUtils.isEquals(masterTailBlock.getPreviousHash(),slaveTailBlock.getPreviousHash())
                && EqualsUtils.isEquals(masterTailBlock.getHeight(),slaveTailBlock.getHeight())
                && EqualsUtils.isEquals(masterTailBlock.getMerkleRoot(),slaveTailBlock.getMerkleRoot())
                && EqualsUtils.isEquals(masterTailBlock.getNonce(),slaveTailBlock.getNonce())
                && EqualsUtils.isEquals(masterTailBlock.getHash(),slaveTailBlock.getHash())){
            return true;
        }
        return false;
    }
    //endregion
}