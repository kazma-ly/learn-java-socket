package com.learn.netty.base_netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.ReferenceCountUtil;

/**
 * NettyServer
 * Created by mac_zly on 2017/5/11.
 */

public class NettyServer {

    private int port;

    public NettyServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        NettyServer nettyServer = new NettyServer(11223);
        nettyServer.run();
    }

    public void run() throws Exception {
        // NioEventLoopGroup 是用来处理I/O操作的多线程事件循环器，Netty 提供了许多不同的 EventLoopGroup 的实现用来处理不同的传输
        /*
        在这个例子中我们实现了一个服务端的应用,因此会有2个 NioEventLoopGroup 会被使用。
        第一个经常被叫做‘boss’，用来接收进来的连接。
        第二个经常被叫做‘worker’，用来处理已经被接收的连接，一旦‘boss’接收到连接，就会把连接信息注册到‘worker’上
         */
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            // ServerBootstrap 是一个启动 NIO 服务的辅助启动类。
            // 你可以在这个服务中直接使用 Channel，但是这会是一个复杂的处理过程，在很多情况下你并不需要这样做。
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    // 这里我们指定使用 NioServerSocketChannel 类来举例说明一个新的 Channel 如何接收进来的连接。
                    .channel(NioServerSocketChannel.class)
                    /*
                    这里的事件处理类经常会被用来处理一个最近的已经接收的 Channel。
                    ChannelInitializer 是一个特殊的处理类，他的目的是帮助使用者配置一个新的 Channel。
                    也许你想通过增加一些处理类比如DiscardServerHandler 来配置一个新的 Channel 或者其对应的ChannelPipeline 来实现你的网络程序。
                    当你的程序变的复杂时，可能你会增加更多的处理类到 pipline 上，然后提取这些匿名类到最顶层的类上。
                     */
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            // 在 TimeServerHandler 之前把 TimeEncoder 插入到ChannelPipeline
                            ch.pipeline().addLast(new TimeEncoder(), new DiscardServerHandler());
                        }
                    })
                    // 你可以设置这里指定的 Channel 实现的配置参数。
                    // 我们正在写一个TCP/IP 的服务端，因此我们被允许设置 socket 的参数选项比如tcpNoDelay 和 keepAlive
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // 绑定端口
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();
            // 等待服务器  socket 关闭 。
            // 在这个例子中，这不会发生，但你可以优雅地关闭你的服务器。
            channelFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    class DiscardServerHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            ByteBuf in = (ByteBuf) msg;
            try {
                // 这个低效的循环事实上可以简化为:System.out.println(in.toString(io.base_netty.util.CharsetUtil.US_ASCII))
                while (in.isReadable()) {
                    System.out.print((char) in.readByte());
                    System.out.flush();
                }
                // 写回去
                ctx.writeAndFlush(msg);
                // ctx.write(Object) 方法不会使消息写入到通道上，他被缓冲在了内部
                //ctx.flush();
            } finally {
                // 或者，你可以在这里调用 in.release()。
                ReferenceCountUtil.release(msg);
                //in.release();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            // 当出现异常就关闭连接
            cause.printStackTrace();
            ctx.close();
        }

        // channelActive() 方法将会在连接被建立并且准备进行通信时被调用,
        // 因此让我们在这个方法里完成一个代表当前时间的32位整数消息的构建工作。
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            /*// 为了发送一个新的消息，我们需要分配一个包含这个消息的新的缓冲。
            // 因为我们需要写入一个32位的整数，因此我们需要一个至少有4个字节的 ByteBuf
            final ByteBuf time = ctx.alloc().buffer(4);
            time.writeInt((int) (System.currentTimeMillis() / 1000L + 2208988800L));
            // ByteBuf 之所以没有这个方法因为有两个指针，一个对应读操作一个对应写操作
            // 所以不用想NIO那样进行翻转操作 flip()
            final ChannelFuture f = ctx.writeAndFlush(time);*/

            // 当一个写请求已经完成是如何通知到我们？这个只需要简单地在返回的 ChannelFuture 上增加一个ChannelFutureListener
            /*f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    assert f == future;
                    ctx.close();
                }
            });*/

            ChannelFuture f = ctx.writeAndFlush(new UnixTime());
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * 第一，通过 ChannelPromise，当编码后的数据被写到了通道上 Netty 可以通过这个对象标记是成功还是失败。
     * 第二， 我们不需要调用 cxt.flush()。
     * 因为处理器已经单独分离出了一个方法 void flush(ChannelHandlerContext cxt)
     * 如果像自己实现 flush() 方法内容可以自行覆盖这个方法。
     */
    public class TimeEncoder1 extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
            UnixTime m = (UnixTime) msg;
            ByteBuf encoded = ctx.alloc().buffer(4);
            encoded.writeInt((int) m.value());
            ctx.write(encoded, promise);
        }
    }

    // 进一步简化使用
    public class TimeEncoder extends MessageToByteEncoder<UnixTime> {
        @Override
        protected void encode(ChannelHandlerContext ctx, UnixTime msg, ByteBuf out) {
            out.writeInt((int) msg.value());
        }
    }
}