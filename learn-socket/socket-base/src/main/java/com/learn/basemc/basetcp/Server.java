package com.learn.basemc.basetcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(2000);

        System.out.println("服务器准备就绪");
        System.out.println("服务器信息" + serverSocket.getLocalSocketAddress() + " P:" + serverSocket.getLocalPort());

        // 等待客户端连接
        for (; ; ) {
            Socket socket = serverSocket.accept();
            // 客户端构建异步线程
            ClassHandler classHandler = new ClassHandler(socket);
            classHandler.start();
        }
    }


    // 客户端消息处理
    private static class ClassHandler extends Thread {

        private Socket socket;
        private boolean flag = true;

        public ClassHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            super.run();
            System.out.println("新客户端连接: " + socket.getInetAddress() + " P:" + socket.getPort());

            try {
                // 用于数据输出
                PrintStream socketOutput = new PrintStream(socket.getOutputStream());
                // 输入流 接收客户端数据
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                do {
                    // 从客户端拿到一条数据
                    String str = socketInput.readLine();
                    if ("bye".equalsIgnoreCase(str)) {
                        flag = false;
                        // 回送bye
                        socketOutput.println("bye");
                    } else {
                        System.out.println(str);
                        socketOutput.println("回送: " + str.length());
                    }
                } while (flag);
                socketInput.close();
                socketOutput.close();
            } catch (Exception e) {
                System.out.println("连接异常断开");
            } finally {
                // 连接关闭
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("客户端已经推出" + socket.getInetAddress() + " P:" + socket.getPort());
                }
            }
        }

    }

}
