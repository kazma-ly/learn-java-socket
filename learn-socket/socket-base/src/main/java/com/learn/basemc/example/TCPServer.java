package com.learn.basemc.example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;

public class TCPServer {

    private static final Integer LOCAL = 20000;
    private static final Integer SERVER = 20001;

    public static void main(String[] args) throws IOException {

        ServerSocket serverSocket = createServerSocket();

        initServerSocket(serverSocket);

        // 绑定ip端口，并且最大等待数未50
        serverSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), SERVER), 50);

        while (true) {
            // 得到客户端
            Socket client = serverSocket.accept();
            ClientHandle clientHandle = new ClientHandle(client);
            clientHandle.start();
        }
    }

    private static void initServerSocket(ServerSocket serverSocket) throws SocketException {
        // 是否服用未完全关闭的地址端口
        serverSocket.setReuseAddress(true);

        // 等效Socket#setReceiveBufferSize
        serverSocket.setReceiveBufferSize(64 * 1024 * 1024);

        // 设置serverSocket#accept超时时间
        // serverSocket.setSoTimeout(2000);

        // 设置性能参数： 短连接， 延迟， 宽带的权重
        serverSocket.setPerformancePreferences(1, 1, 0);
    }

    private static ServerSocket createServerSocket() throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        return serverSocket;
    }

    private static class ClientHandle extends Thread {

        private Socket socket;

        public ClientHandle(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

            byte[] buffer = new byte[128];

            // 得到套接字流
            try (OutputStream outputStream = socket.getOutputStream();
                 InputStream inputStream = socket.getInputStream()) {

                int size = inputStream.read(buffer);
                if (size > 0) {
                    int val = Tool.byteArrayToInt(buffer);
                    System.out.println("收到数量: " + size + " " + val);
                    // echo
                    outputStream.write(buffer, 0, size);
                } else {
                    System.out.println("没有收到数据: " + size);
                    // echo
                    outputStream.write(new byte[]{0});
                }
            } catch (IOException e) {
                System.out.println("异常断开");
            }

        }
    }

}
