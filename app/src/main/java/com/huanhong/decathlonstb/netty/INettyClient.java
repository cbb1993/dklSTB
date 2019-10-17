package com.huanhong.decathlonstb.netty;


public interface INettyClient
{
    void connect(String host, int port);//1. 建立连接

    void sendMessage(String msg, long delayed);//2. 发送消息

    void setNettyClientListener(NettyClientListener listener);//3. 为不同的请求添加监听器

    interface NettyClientListener
    {
        void onInited();

        void onConnected();

        void onConnectFailed(Exception e);

        void onSent(String message);

        void onSendFailed(String message, Exception e);

        void onDataReceive(String data);

        void onError(Exception e);

        void onDisconnected();
    }
}
