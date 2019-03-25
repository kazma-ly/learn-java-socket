package com.learn.chatserver;

// 启动 可以使用telnet连接
public class MainServiceSocket {
    public static void main(String[] args) {
        new ServerLisenterThread().start();
    }
}
