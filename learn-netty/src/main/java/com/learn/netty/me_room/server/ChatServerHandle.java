package com.learn.netty.me_room.server;

import framework.netty.me_room.empty.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

/**
 * ChatServerHandle
 * Created by mac_zly on 2017/5/30.
 */

public class ChatServerHandle extends SimpleChannelInboundHandler<Message> {

    private static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    // 每当从服务端收到新的客户端连接时
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        /*for (Channel channel : channels) {
            channel.writeAndFlush("[SERVER] - " + incoming.remoteAddress() + " 加入\n");
        }*/
        channels.add(ctx.channel());
    }

    // 每当从服务端收到客户端断开时
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel incoming = ctx.channel();
        /*for (Channel channel : channels) {
            channel.writeAndFlush("[SERVER] - " + incoming.remoteAddress() + " 离开\n");
        }*/
        channels.remove(ctx.channel());
    }

    // 接收到数据
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        Channel incoming = ctx.channel();
        for (Channel channel : channels) {
            if (incoming == channel) {
                System.out.println("消息: " + msg);
            } else {
                System.out.println("消息发给别人: " + msg);
                channel.writeAndFlush(msg);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
    }
}
