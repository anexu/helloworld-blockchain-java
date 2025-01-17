package com.xingkaichun.helloworldblockchain.netcore.client;

import com.xingkaichun.helloworldblockchain.netcore.dto.*;
import com.xingkaichun.helloworldblockchain.setting.NetworkSetting;
import com.xingkaichun.helloworldblockchain.util.JsonUtil;
import com.xingkaichun.helloworldblockchain.util.NetUtil;

/**
 *
 * @author 邢开春 409060350@qq.com
 */
public class NodeClientImpl implements NodeClient {

    private String ip;

    public NodeClientImpl(String ip) {
        this.ip = ip;
    }

    @Override
    public PostTransactionResponse postTransaction(PostTransactionRequest request) {
        String requestUrl = getUrl(API.POST_TRANSACTION);
        String requestBody = JsonUtil.toString(request);
        String responseHtml = NetUtil.get(requestUrl,requestBody);
        return JsonUtil.toObject(responseHtml,PostTransactionResponse.class);
    }

    @Override
    public PingResponse pingNode(PingRequest request) {
        String requestUrl = getUrl(API.PING);
        String requestBody = JsonUtil.toString(request);
        String responseHtml = NetUtil.get(requestUrl,requestBody);
        return JsonUtil.toObject(responseHtml,PingResponse.class);
    }

    @Override
    public GetBlockResponse getBlock(GetBlockRequest request) {
        String requestUrl = getUrl(API.GET_BLOCK);
        String requestBody = JsonUtil.toString(request);
        String responseHtml = NetUtil.get(requestUrl,requestBody);
        return JsonUtil.toObject(responseHtml,GetBlockResponse.class);
    }

    @Override
    public GetNodesResponse getNodes(GetNodesRequest request) {
        String requestUrl = getUrl(API.GET_NODES);
        String requestBody = JsonUtil.toString(request);
        String responseHtml = NetUtil.get(requestUrl,requestBody);
        return JsonUtil.toObject(responseHtml, GetNodesResponse.class);
    }

    @Override
    public PostBlockResponse postBlock(PostBlockRequest request) {
        String requestUrl = getUrl(API.POST_BLOCK);
        String requestBody = JsonUtil.toString(request);
        String responseHtml = NetUtil.get(requestUrl,requestBody);
        return JsonUtil.toObject(responseHtml,PostBlockResponse.class);
    }

    @Override
    public PostBlockchainHeightResponse postBlockchainHeight(PostBlockchainHeightRequest request) {
        String requestUrl = getUrl(API.POST_BLOCKCHAIN_HEIGHT);
        String requestBody = JsonUtil.toString(request);
        String responseHtml = NetUtil.get(requestUrl,requestBody);
        return JsonUtil.toObject(responseHtml, PostBlockchainHeightResponse.class);
    }

    @Override
    public GetBlockchainHeightResponse getBlockchainHeight(GetBlockchainHeightRequest request) {
        String requestUrl = getUrl(API.GET_BLOCKCHAIN_HEIGHT);
        String requestBody = JsonUtil.toString(request);
        String responseHtml = NetUtil.get(requestUrl,requestBody);
        return JsonUtil.toObject(responseHtml, GetBlockchainHeightResponse.class);
    }

    @Override
    public GetUnconfirmedTransactionsResponse getUnconfirmedTransactions(GetUnconfirmedTransactionsRequest request) {
        String requestUrl = getUrl(API.GET_UNCONFIRMED_TRANSACTIONS);
        String requestBody = JsonUtil.toString(request);
        String responseHtml = NetUtil.get(requestUrl,requestBody);
        return JsonUtil.toObject(responseHtml, GetUnconfirmedTransactionsResponse.class);
    }

    private String getUrl(String api) {
        return "http://" + ip + ":" + NetworkSetting.PORT + api;
    }
}