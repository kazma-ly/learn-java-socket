package com.learn.basemc.basetcp;

import java.io.*;
import java.net.*;

public class Client {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket();
        // 超时时间
        socket.setSoTimeout(3000);
        // 连接到本地 端口是2000，连接超时时间3000ms
        socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(), 2000), 3000);

        //
        System.out.println("已发起服务器连接,并进入后续流程~");
        System.out.println("客户端信息" + socket.getLocalAddress() + " P:" + socket.getLocalPort());
        System.out.println("服务器信息: " + socket.getInetAddress() + " P:" + socket.getPort());

        try {
            // 发送数据
            todo(socket);
        } catch (Exception e) {
            System.out.println("异常关闭");
        }

        socket.close();
        System.out.println("客户端已退出");
    }

    private static void todo(Socket client) throws IOException {
        // 构建键盘输入流
        InputStream inputStream = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));

        // 得到socket输出流， 并转换为打印流
        OutputStream outputStream = client.getOutputStream();
        PrintStream printStream = new PrintStream(outputStream);

        // 得到输入流
        InputStream clientInputStream = client.getInputStream();
        BufferedReader socketBufferReader = new BufferedReader(new InputStreamReader(clientInputStream));

        boolean flag = true;
        do {
            String str = input.readLine();
            printStream.println(str);

            // 从服务器读取一行
            String echo = socketBufferReader.readLine();
            if ("bye".equalsIgnoreCase(echo)) {
                flag = false;
            } else {
                System.out.println(echo);
            }
        } while (flag);

        printStream.close();
        socketBufferReader.close();
    }

}
