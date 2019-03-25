package com.learn.netty.base_netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

/**
 * NettyClient
 * Created by mac_zly on 2017/5/17.
 */

public class NettyClient {

    public static void main(String[] args) throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    // 现在我们有另外一个处理器插入到 ChannelPipeline 里 TimeDecoder
                    // 在 TimeServerHandler 之前把 TimeEncoder 插入到ChannelPipeline
                    ch.pipeline().addLast(new TimeDecoder(), new TimeClientHandler());
                }
            });

            // 启动客户端
            ChannelFuture f = b.connect("127.0.0.1", 11223).sync();

            // 等待连接关闭
            f.channel().closeFuture().sync();
        } finally {
            // 可以添加listener获得关闭玩的时候
            workerGroup.shutdownGracefully();
        }
    }

    static class TimeClientHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            /*ByteBuf m = (ByteBuf) msg;
            try {
                long currentTimeMillis = (m.readUnsignedInt() - 2208988800L) * 1000L;
                System.out.println(new Date(currentTimeMillis));
                ctx.close();
            } finally {
                m.release();
            }*/
            UnixTime m = (UnixTime) msg;
            System.out.println(m);
            ctx.close();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }

    // ByteToMessageDecoder 是 ChannelInboundHandler 的一个实现类，他可以在处理数据拆分的问题上变得很简单。
    static class TimeDecoder extends ByteToMessageDecoder {
        // 每当有新数据接收的时候，ByteToMessageDecoder 都会调用 decode() 方法来处理内部的那个累积缓冲。
        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
            // Decode() 方法可以决定当累积缓冲里没有足够数据时可以往 out 对象里放任意数据。
            // 当有更多的数据被接收了 ByteToMessageDecoder 会再一次调用 decode() 方法。
            if (in.readableBytes() < 4) {
                return;
            }
            // 如果在 decode() 方法里增加了一个对象到 out 对象里，这意味着解码器解码消息成功
            // ByteToMessageDecoder 将会丢弃在累积缓冲里已经被读过的数据。
            // 请记得你不需要对多条消息调用 decode()，ByteToMessageDecoder 会持续调用 decode() 直到不放任何数据到 out 里。
            out.add(new UnixTime(in.readUnsignedInt()) // in.readBytes(4)
            );
        }
    }
}
