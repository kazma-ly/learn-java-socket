package com.learn.chatserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/*
  监听连接线程
 */
public class ServerLisenterThread extends Thread {

    @Override
    public void run() {
        try {
            //默认监听接口11223
            int port = 11223;
            ServerSocket serverSocket = new ServerSocket(port);
            //无线循环开启监听线程
            while (true) {
                try {
                    //开始监听监听,阻塞线程
                    Socket socket = serverSocket.accept();

                    System.out.println("***有客户端连接到了本机的" + port + "端口***");
                    //将socket传给新的线程(每一个客户端对应一个线程)
                    ChatSocketThread chatSocketThread = new ChatSocketThread(socket);
                    //开启一个新的聊天线程
                    chatSocketThread.start();

                    ChatManager.getChatManager().send(chatSocketThread, "Welcome A New Friend \n");

                    socket.getOutputStream().write("Welcome Friend \n".getBytes("UTF-8"));

                    //把新创建的聊天线程给与ChatManager聊天管理器
                    ChatManager.getChatManager().add(chatSocketThread);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
