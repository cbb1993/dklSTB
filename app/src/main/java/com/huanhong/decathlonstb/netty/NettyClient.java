package com.huanhong.decathlonstb.netty;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

public class NettyClient implements INettyClient {
    private static NettyClient mInstance;
    private final String ACTION_SEND_MSG = "action_send";
    private final int MESSAGE_INIT = 0x1;
    private final int MESSAGE_CONNECT = 0x2;
    private final int MESSAGE_SEND = 0x3;
    private Bootstrap mBootstrap;
    private Channel mChannel;
    private String mHost;
    private int mPort;
    private HandlerThread mWorkThread;
    private Handler mWorkHandler;
    private Handler mMainHandler;
    private NettyClientHandler mNettyClientHandler;
    private NettyClientListener mNettyClientListener;
    private String mHeartBeat;
    private NioEventLoopGroup mGroup;

    private Handler.Callback mWorkHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(final Message msg) {
            switch (msg.what) {
                case MESSAGE_INIT: {
                    if (mGroup == null || mBootstrap == null) {
                        mGroup = new NioEventLoopGroup();
                        mBootstrap = new Bootstrap();
                        mBootstrap.channel(NioSocketChannel.class);
                        mBootstrap.group(mGroup);
                        mBootstrap.option(ChannelOption.SO_KEEPALIVE, true);
                        mBootstrap.option(ChannelOption.TCP_NODELAY, true);
                    }
                    mBootstrap.handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("framer", new DelimiterBasedFrameDecoder(1024 * 2 * 1024, Delimiters
                                    .lineDelimiter()));
                            pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
                            pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
                            pipeline.addLast("ping", new IdleStateHandler(20, 15, 15, TimeUnit.SECONDS));
                            pipeline.addLast("handler", mNettyClientHandler);
//                            pipeline.addLast(mNettyClientHandler);
                        }
                    });

                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mNettyClientListener != null) {
                                mNettyClientListener.onInited();
                            }
                        }
                    });
                    break;
                }
                case MESSAGE_CONNECT: {
                    try {
                        if (TextUtils.isEmpty(mHost) || mPort == 0) {
                            throw new Exception("Netty host | port is invalid");
                        }
                        if (mChannel == null || !mChannel.isOpen()) {
                            mChannel = mBootstrap
                                    .connect(new InetSocketAddress(mHost, mPort))
                                    .sync()
                                    .channel();
                        }
                    } catch (final Exception e) {
                        e.printStackTrace();
                        mMainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (mNettyClientListener != null) {
                                    mNettyClientListener.onConnectFailed(e);
                                }
                            }
                        });
                    }
                    break;
                }
                case MESSAGE_SEND: {
                    final String sendMsg = msg.getData().getString(ACTION_SEND_MSG);
                    try {
                        if (mChannel != null && mChannel.isActive()) {
                            mChannel.writeAndFlush(constructMessage(sendMsg)).sync();
                            mMainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (mNettyClientListener != null) {
                                        mNettyClientListener.onSent(sendMsg);
                                    }
                                }
                            });
                        } else {
                            throw new Exception("通道为空或未连接服务器");
                        }
                    } catch (final Exception e) {
                        e.printStackTrace();
                        mMainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (mNettyClientListener != null) {
                                    mNettyClientListener.onSendFailed(sendMsg, e);
                                }
                            }
                        });
                    }
                    break;
                }
            }
            return true;
        }
    };

    private NettyClient() {
        init();
    }

    public synchronized static NettyClient getInstance() {
        if (mInstance == null)
            mInstance = new NettyClient();
        return mInstance;
    }

    public void init() {
        mMainHandler = new Handler(Looper.getMainLooper());
        mWorkThread = new HandlerThread(NettyClient.class.getName());
        mWorkThread.start();
        mWorkHandler = new Handler(mWorkThread.getLooper(), mWorkHandlerCallback);
        reInit(0);
    }

    public void reInit(int delay) {
        if (mNettyClientHandler != null)
            mNettyClientHandler.setNettyClientListener(null);
        mNettyClientHandler = new NettyClientHandler();
        //若已有的hearBeat和nettyClientListener再次赋值给nettyClient
        mNettyClientHandler.setHeartBeat(mHeartBeat);
        mNettyClientHandler.setNettyClientListener(mNettyClientListener);
        mWorkHandler.removeMessages(MESSAGE_INIT);
        mWorkHandler.sendEmptyMessageDelayed(MESSAGE_INIT, delay);
    }

    @Override
    public void connect(String host, int port) {
        this.mHost = host;
        this.mPort = port;
        mWorkHandler.removeMessages(MESSAGE_CONNECT);
        mWorkHandler.sendEmptyMessage(MESSAGE_CONNECT);
    }

    public void reConnect(int delay) {
        mWorkHandler.removeMessages(MESSAGE_CONNECT);
        mWorkHandler.sendEmptyMessageDelayed(MESSAGE_CONNECT, delay);
    }

    @Override
    public void setNettyClientListener(NettyClientListener listener) {
        mNettyClientListener = listener;
        if (mNettyClientHandler != null) {
            mNettyClientHandler.setNettyClientListener(listener);
        }
    }

    public void setHeartBeat(String heartBeat) {
        mHeartBeat = heartBeat;
        if (mNettyClientHandler != null) {
            mNettyClientHandler.setHeartBeat(heartBeat);
        }
    }

    @Override
    public void sendMessage(String msg, long delayed) {
        if (TextUtils.isEmpty(msg))
            return;
        Message message = new Message();
        Bundle bundle = new Bundle();
        message.what = MESSAGE_SEND;
        bundle.putString(ACTION_SEND_MSG, msg);
        message.setData(bundle);
        mWorkHandler.sendMessageDelayed(message, delayed);
    }

    public void disconnect() {
        if (mChannel != null)
            mChannel.disconnect();
        mWorkHandler.removeCallbacksAndMessages(null);
    }

    public boolean isConnect() {
        return mChannel != null && mChannel.isOpen();
    }

    private String constructMessage(String msg) {
        String message = msg + "\r\n";
        //与后台协议好，如何设置校验部分，然后和json一起发给服务器
        return message;
    }

//    public ByteBuf getSendByteBuf(String message) {
//        byte[] req = new byte[0];
//        try {
//            req = message.getBytes("UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        ByteBuf pingMessage = Unpooled.buffer();
//        pingMessage.writeBytes(req);
//        return pingMessage;
//    }

//    public String getReadByteBuf(Object msg) {
//        ByteBuf result = (ByteBuf) msg;
//        byte[] result1 = new byte[result.readableBytes()];
//        // msg中存储的是ByteBuf类型的数据，把数据读取到byte[]中
//        result.readBytes(result1);
//        String resultStr = null;
//        try {
//            resultStr = new String(result1, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        // 释放资源，这行很关键
//        result.release();
//        return resultStr;
//    }
}


