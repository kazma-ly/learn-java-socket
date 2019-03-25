package com.learn.basemc.tcphelpudp.client;

import com.learn.basemc.tcphelpudp.client.bean.ServerInfo;
import com.learn.basemc.tcphelpudp.utils.CloseUtils;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class TCPClient {

    public static void linkWith(ServerInfo serverInfo) throws IOException {
        Socket socket = new Socket();
        // 超时时间
        socket.setSoTimeout(3000);
        // 连接本地端口
        socket.connect(new InetSocketAddress(Inet4Address.getByName(serverInfo.getAddress()), serverInfo.getPort()), 3000);

        System.out.println("已发起服务器连接，并进入后续流程～");
        System.out.println("客户端信息：" + socket.getLocalAddress() + " P:" + socket.getLocalPort());
        System.out.println("服务器信息：" + socket.getInetAddress() + " P:" + socket.getPort());

        ReadHandler readHandler = new ReadHandler(socket.getInputStream());
        try {
            readHandler.start();

            // 发送接收数据
            write(socket);
        } catch (Exception e) {
            System.out.println("异常关闭");
        } finally {
            readHandler.exit();
            // 释放资源
            socket.close();
        }

        System.out.println("客户端已退出～");

    }

    private static void write(Socket client) throws IOException {
        // 构建键盘输入流
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        // 得到Socket输出流，并转换为打印流
        OutputStream outputStream = client.getOutputStream();

        // 资源释放
        try (PrintStream socketPrintStream = new PrintStream(outputStream)) {
            do {
                // 键盘读取一行
                String inputString = input.readLine();
                // 发送到服务器
                socketPrintStream.println(inputString);

                if ("bye".equalsIgnoreCase(inputString)) {
                    break;
                }
            } while (true);
        }
    }

    /**
     * 读取操作类 是一个线程
     */
    static class ReadHandler extends Thread {
        private boolean done = false;
        private final InputStream inputStream;

        public ReadHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {

            try (// 得到输入流
                 BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream))) {
                do {
                    String msg;
                    try {
                        // 从客户端读取一条消息
                        msg = socketInput.readLine();
                        // 捕获超时信息
                    } catch (SocketTimeoutException e) {
                        continue;
                    }
                    if (msg == null) {
                        System.out.println("连接关闭，无法读取数据");
                        break;
                    }
                    // 输出
                    System.out.println(msg);
                } while (!done);
            } catch (IOException e) {
                if (!this.done) {
                    System.out.println("连接异常断开");
                }
            } finally {
                CloseUtils.close(inputStream);
            }
        }

        void exit() {
            done = true;
            CloseUtils.close(inputStream);
        }
    }

}
