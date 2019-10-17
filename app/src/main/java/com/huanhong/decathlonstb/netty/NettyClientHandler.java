package com.huanhong.decathlonstb.netty;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import java.io.UnsupportedEncodingException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

@ChannelHandler.Sharable
public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    private INettyClient.NettyClientListener mNettyClientListener;
    private Handler mMainHandler;
    private String mHeartBeat;
    private int mIdleCount;

    public NettyClientHandler() {
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    public void setHeartBeat(String heartBeat) {
        mHeartBeat = heartBeat;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //channelActive()方法将会在连接被建立并且准备进行通信时被调用。
        super.channelActive(ctx);
        mIdleCount = 0;

        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mNettyClientListener != null) {
                    mNettyClientListener.onConnected();
                }
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mNettyClientListener != null) {
                    mNettyClientListener.onDisconnected();
                }
            }
        });
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        mIdleCount = 0;

        final String data = msg.toString();
//        final String data = getReadByteBuf(msg);
        //verify(String body)方法对服务器返回的数据进行校验，并取出数据部分。
        //具体校验的方法需要与后台同事进行协议。
        if (!TextUtils.isEmpty(data)) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mNettyClientListener != null) {
                        mNettyClientListener.onDataReceive(data);
                    }
                }
            });
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, final Throwable cause)
            throws Exception {
        //exceptionCaught()事件处理方法是当出现Throwable对象才会被调用，
        //即当Netty由于IO错误或者处理器在处理事件时抛出的异常时。
        //在大部分情况下，捕获的异常应该被记录下来并且把关联的channel给关闭掉。
        super.exceptionCaught(ctx, cause);
        ctx.close();

        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mNettyClientListener != null) {
                    mNettyClientListener.onError(new Exception(cause));
                }
            }
        });
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
//                if (mIdleCount >= 3) {
//                    ctx.close();
//                    mIdleCount = 0;
//                    Log.e(getClass().getSimpleName(), this.toString() + ":心跳中断,关闭连接");
//                } else {
//                    Log.e(getClass().getSimpleName(), this.toString() + ":心跳中断：" + mIdleCount);
//                    mIdleCount++;
//                }
            } else if (event.state() == IdleState.WRITER_IDLE) {
                Log.e("---","---"+event.state());
                ctx.writeAndFlush(mHeartBeat + "\n");
            }
        }
    }

    public String getReadByteBuf(Object msg) {
        ByteBuf result = (ByteBuf) msg;
        byte[] result1 = new byte[result.readableBytes()];
        // msg中存储的是ByteBuf类型的数据，把数据读取到byte[]中
        result.readBytes(result1);
        String resultStr = null;
        try {
            resultStr = new String(result1, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // 释放资源，这行很关键
        result.release();
        return resultStr;
    }

    public INettyClient.NettyClientListener getNettyClientListener() {
        return mNettyClientListener;
    }

    public void setNettyClientListener(INettyClient.NettyClientListener nettyClientListener) {
        mNettyClientListener = nettyClientListener;
    }
}