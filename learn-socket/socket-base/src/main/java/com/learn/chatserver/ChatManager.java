package com.learn.chatserver;

import java.util.Vector;

public class ChatManager {

    private ChatManager() {
    }

    private static final ChatManager chatManager = new ChatManager();

    // 返回消息管理器 单利化
    public static ChatManager getChatManager() {
        return chatManager;
    }

    Vector<ChatSocketThread> vector = new Vector<>();

    // 把每个客户端加进来 进行管理
    public void add(ChatSocketThread chatSocketThread) {
        vector.add(chatSocketThread);
    }

    // 自己写的取消连接客户端
    public void remove(ChatSocketThread chatSocketThread) {
        vector.remove(chatSocketThread);
    }

    public void send(ChatSocketThread chatSocketThread, String sendMessage) {
        // 对所有用户发送,就遍历一遍
        for (int i = 0; i < vector.size(); i++) {
            //遍历到的每个用户
            ChatSocketThread cs = vector.get(i);
            //如果不是当前用户就输出,因为自己的话就不用接受自己的消息了
            if (!chatSocketThread.equals(cs)) {

                //然后如果不是自己 就可以向别人发送数据了
                cs.out(sendMessage);
            }
        }
    }

}
