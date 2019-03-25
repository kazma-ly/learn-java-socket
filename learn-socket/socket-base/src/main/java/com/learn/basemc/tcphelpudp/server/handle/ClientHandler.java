package com.learn.basemc.tcphelpudp.server.handle;

import com.learn.basemc.tcphelpudp.utils.CloseUtils;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 处理客户端
 *
 * @author zly
 */
public class ClientHandler {

    private final Socket socket;
    private final ClientReadHandler clientReadHandler;
    private final ClientWriteHandler clientWriteHandler;
    private final CloseNotify closeNotify;

    public ClientHandler(Socket socket, CloseNotify closeNotify) throws IOException {
        this.socket = socket;
        this.clientReadHandler = new ClientReadHandler(socket.getInputStream());
        this.clientWriteHandler = new ClientWriteHandler(socket.getOutputStream());
        this.closeNotify = closeNotify;
    }

    public void exit() {
        clientReadHandler.exit();
        clientWriteHandler.exit();
        CloseUtils.close(socket);
        System.out.println("客户端已退出：" + socket.getInetAddress() + " P:" + socket.getPort());
    }

    private void exitBySelf() {
        exit();
        this.closeNotify.onSelfClosed(this);
    }

    public void send(String inputString) {
        clientWriteHandler.send(inputString);
    }

    public void readToPrint() {
        clientReadHandler.start();
    }

    /**
     * 读取操作类 是一个线程
     */
    class ClientReadHandler extends Thread {
        private boolean done = false;
        private final InputStream inputStream;

        public ClientReadHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {

            try (// 得到输入流
                 BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream))) {
                do {
                    // 从客户端读取一条消息
                    String msg = socketInput.readLine();
                    if (msg == null) {
                        System.out.println("客户端读取数据失败, 目测已经失联");
                        ClientHandler.this.exitBySelf();
                    } else {
                        System.out.println(msg);
                    }
                } while (!done);
            } catch (IOException e) {
                if (!this.done) {
                    System.out.println("连接异常断开");
                    ClientHandler.this.exitBySelf();
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

    /**
     * 消息发送
     */
    private class ClientWriteHandler {

        private boolean done = false;
        private final PrintStream printStream;
        private final Executor executor;

        public ClientWriteHandler(OutputStream outputStream) {
            this.printStream = new PrintStream(outputStream);
            this.executor = Executors.newSingleThreadExecutor();
        }

        public void exit() {
            done = true;
            CloseUtils.close(printStream);
        }

        public void send(String inputString) {
            executor.execute(new WriteRunable(inputString));
        }

        private class WriteRunable implements Runnable {

            private final String msg;

            public WriteRunable(String msg) {
                this.msg = msg;
            }

            @Override
            public void run() {
                try {
                    if (ClientWriteHandler.this.done) {
                        return;
                    }
                    ClientWriteHandler.this.printStream.println(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    public interface CloseNotify {
        void onSelfClosed(ClientHandler clientHandler);
    }

}
