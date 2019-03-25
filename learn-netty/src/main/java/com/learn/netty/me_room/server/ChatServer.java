package com.learn.netty.me_room.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * ChatServer
 * Created by mac_zly on 2017/5/30.
 */
public class ChatServer {

    private static final int BIZGROUPSIZE = Runtime.getRuntime().availableProcessors() * 2; //默认

    public static void main(String[] args) {
        new ChatServer().startServer(11223);
    }

    private void startServer(int port) {
        /*
         * NioEventLoopGroup实际上就是个线程池,
         * NioEventLoopGroup在后台启动了n个NioEventLoop来处理Channel事件,
         * 每一个NioEventLoop负责处理m个Channel,
         * NioEventLoopGroup从NioEventLoop数组里挨个取出NioEventLoop来处理Channel
         */
        // 我们实现了一个服务端的应用，因此会有2个 NioEventLoopGroup 会被使用。
        // 第一个经常被叫做‘boss’，用来接收进来的连接。第二个经常被叫做‘worker’
        // 一旦‘boss’接收到连接，就会把连接信息注册到‘worker’上
        EventLoopGroup bossGroup = new NioEventLoopGroup(BIZGROUPSIZE);
        EventLoopGroup workerGroup = new NioEventLoopGroup(BIZGROUPSIZE);

        try {
            // ServerBootstrap 是启动 NIO 服务的辅助启动类
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    // 这里我们指定使用NioServerSocketChannel类来举例说明一个新的 Channel 如何接收进来的连接。
                    .channel(NioServerSocketChannel.class)
                    // 这里的事件处理类经常会被用来处理一个最近的已经接收的 Channel
                    .childHandler(new ChatServerInitializer());
                    // 你可以设置这里指定的 Channel 实现的配置参数
                    // 我们正在写一个TCP/IP 的服务端，因此我们被允许设置 socket 的参数选项比如tcpNoDelay 和 keepAlive。
                    //.option(ChannelOption.SO_BACKLOG, 128)
                    // option() 是提供给NioServerSocketChannel用来接收进来的连接。
                    // childOption() 是提供给由父管道ServerChannel接收到的连接，在这个例子中也是 NioServerSocketChannel。
                    //.childOption(ChannelOption.SO_KEEPALIVE, true);

            System.out.println("启动");

            // 绑定端口，开始接收进来的连接
            ChannelFuture channelFuture = serverBootstrap.bind(port).sync();

            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();

            System.out.println("服务器关闭了");
        }
    }

}
