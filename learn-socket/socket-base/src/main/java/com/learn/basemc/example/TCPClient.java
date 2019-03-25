package com.learn.basemc.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * TCP client
 */
public class TCPClient {

    private static final Integer LOCAL = 20000;
    private static final Integer REMOTE = 20001;

    public static void main(String[] args) throws IOException {

        Socket socket = createSocket();

        initSocket(socket);

        // 连接到本地20001端口 超时时间3s， 超时抛出异常
        socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(), REMOTE), 3000);

        todo(socket);

        socket.close();
    }

    private static void todo(Socket client) throws IOException {

        // 得到socket输出流，并转换成打印流
        OutputStream outputStream = client.getOutputStream();

        // 得到Socket的输入流， 并转换成BufferedReader
        InputStream inputStream = client.getInputStream();

        byte[] buffer = new byte[128];

        byte[] ints = Tool.intToByteArray(2333333);

        outputStream.write(ints);

        int size = inputStream.read(buffer);
        if (size > 0) {
            System.out.println("收到数量: " + size + " " + new String(buffer, 0, size));
        } else {
            System.out.println("没有收到数据: " + size);
        }

        outputStream.close();
        inputStream.close();
    }

    private static void initSocket(Socket socket) throws SocketException {
        // 设置读取超时时间为2s
        socket.setSoTimeout(2000);
        // 是否复用未完全关闭的Socket地址，对于指定bind操作后的套接字有效

        // 是否开启Nagle算法
//        socket.setTcpNoDelay(false);

        // 是否需要在长时间无数据响应时发送确认数据(类似心跳包), 时间大约两小时
        socket.setKeepAlive(true);

        // 对于close关闭操作进行怎样的处理 默认为false，0
        // false, 0: 默认情况，关闭时立即返回，底层系统接管输出流，将缓冲区内的数据发送完成
        // true, 0: 关闭时立即返回，缓冲区数据丢弃，直接发送RST结束命令到对方，并且无需2MSL(数据等待时间)等待
        // true, 200: 关闭时最长阻塞200毫秒, 随后按第二情况处理
        socket.setSoLinger(true, 20);

        // 是否让紧急数据内敛(紧急数据, 可以作为心跳包，但不建议设置为true, 和行为数据混合在一起)
        // 默认false, 紧急数据通过socket.sendUrgentData(1); 发送
        socket.setOOBInline(true);

        // 设置接收发送缓冲器大小 64k
        socket.setReceiveBufferSize(64 * 1024);
        socket.setSendBufferSize(64 * 1024);

        // 设置性能参数： 短连接， 延迟， 宽带的权重
        socket.setPerformancePreferences(1, 1, 0);
    }

    private static Socket createSocket() throws IOException {
        Socket socket = new Socket();
        socket.bind(new InetSocketAddress(Inet4Address.getLocalHost(), LOCAL));
        return socket;
    }


}
