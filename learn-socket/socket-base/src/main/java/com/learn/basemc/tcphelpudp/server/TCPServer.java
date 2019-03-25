package com.learn.basemc.tcphelpudp.server;

import com.learn.basemc.tcphelpudp.server.handle.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TCPServer {

    private final int port;
    private ClientListener clientListener;
    private List<ClientHandler> clientHandlerList = new ArrayList<>();

    public TCPServer(int port) {
        this.port = port;
    }

    public boolean start() {
        try {
            ClientListener listener = new ClientListener(port);
            clientListener = listener;
            listener.start();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void stop() {
        if (clientListener != null) {
            clientListener.exit();
        }
        for (ClientHandler clientHandler : clientHandlerList) {
            clientHandler.exit();
        }
        clientHandlerList.clear();
    }

    public void broadcast(String inputString) {
        for (ClientHandler clientHandler : clientHandlerList) {
            clientHandler.send(inputString);
        }
    }

    private class ClientListener extends Thread {
        private ServerSocket serverSocket;
        private boolean done = false;

        private ClientListener(int port) throws IOException {
            serverSocket = new ServerSocket(port);
            System.out.println("服务器信息：" + serverSocket.getInetAddress() + " P:" + serverSocket.getLocalPort());
        }

        @Override
        public void run() {
            System.out.println("服务器准备就绪");

            try {
                // 等待客户端连接
                do {
                    // 客户端
                    Socket socket = serverSocket.accept();
                    // 客户端构建异步线程
                    ClientHandler clientHandler = new ClientHandler(socket, clientHandler1 -> {
                        clientHandlerList.remove(clientHandler1); // 移除这个客户端
                    });
                    // 读取数据并打印
                    clientHandler.readToPrint();
                    clientHandlerList.add(clientHandler);
                } while (!done);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("客户端处理异常");
            } catch (Exception e1) {
                System.out.println("出现未知异常");
            }
            System.out.println("服务器已经关闭");
        }

        void exit() {
            done = true;
            try {
                serverSocket.close();
            } catch (IOException ignore) {
            }
        }
    }
}
