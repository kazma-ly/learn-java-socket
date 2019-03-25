package com.learn.chatserver;

import java.io.*;
import java.net.Socket;

/**
 * 聊天线程
 */
public class ChatSocketThread extends Thread {
    private Socket socket;

    public ChatSocketThread(Socket socket) {
        this.socket = socket;
    }

    //循环读取客户端的操作,将读取到的内容发送到所有的客户端中
    @Override
    public void run() {
        try {
            //从socket获得输入流
            InputStream inputStream = socket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            //客户端发送给服务器的数据
            while ((line = bufferedReader.readLine()) != null) {
                if ("exit".equals(line) || "bye".equals(line)) {
                    socket.close();
                    ChatManager.getChatManager().remove(this);
                    return;
                }
                //System.out.println("line -> " + line);

                if (!"".equals(line) || !line.isEmpty()) {
                    //发给所有人 使用ChatManager的单利
                    ChatManager.getChatManager().send(this, line);
                }
            }
            //
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
        } catch (IOException e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    // 发送数据
    public void out(String string) {
        try {
            String tempStr = string + "\n";
            //数据流写出
            socket.getOutputStream().write(tempStr.getBytes("UTF-8"));
        } catch (IOException e) {
            ChatManager.getChatManager().remove(this);
            System.out.println(e.getLocalizedMessage());
        }
    }
}
