package com.server.handle;

import com.lib.clink.utils.CloseUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {
    private final SocketChannel socketChannel;
    private final ClientReadHandler readHandler;
    private final ClientWriteHandler writeHandler;
    private final ClientHandleCallback clientHandleCallback;
    private final String clientInfo;

    public ClientHandler(SocketChannel socketChannel, ClientHandleCallback clientHandleCallback) throws IOException {
        this.socketChannel = socketChannel;
        // 设置非阻塞模式
        socketChannel.configureBlocking(false);

        Selector readSelector = Selector.open();
        socketChannel.register(readSelector, SelectionKey.OP_READ);
        this.readHandler = new ClientReadHandler(readSelector);

        Selector writeSelector = Selector.open();
        socketChannel.register(writeSelector, SelectionKey.OP_WRITE);
        this.writeHandler = new ClientWriteHandler(writeSelector);

        this.clientHandleCallback = clientHandleCallback;
        this.clientInfo = socketChannel.getRemoteAddress().toString();
        System.out.println("新客户端连接：" + clientInfo);
    }

    public String getClientInfo() {
        return clientInfo;
    }

    public void exit() {
        readHandler.exit();
        writeHandler.exit();

        CloseUtils.close(socketChannel);
        System.out.println("客户端已退出：" + clientInfo);
    }

    public void send(String str) {
        writeHandler.send(str);
    }

    public void readToPrint() {
        readHandler.start();
    }

    private void exitBySelf() {
        exit();
        clientHandleCallback.onSelfClosed(this);
    }

    public interface ClientHandleCallback {
        // 收到消息时通知
        void onSelfClosed(ClientHandler handler);

        // 收到消息的回调
        void onMessageArrived(ClientHandler handler, String msg);
    }

    class ClientReadHandler extends Thread {
        private boolean done = false;
        private final Selector selector;
        private final ByteBuffer byteBuffer;

        ClientReadHandler(Selector selector) {
            this.selector = selector;
            this.byteBuffer = ByteBuffer.allocate(256);
        }

        @Override
        public void run() {
            super.run();
            try {
                do {
                    // 客户端拿到一条数据
                    if (selector.select() == 0) {
                        if (done) {
                            break;
                        }
                        continue;
                    }

                    Iterator<SelectionKey> iterator = selector.keys().iterator();
                    while (iterator.hasNext()) {
                        if (done) {
                            break;
                        }
                        SelectionKey key = iterator.next();
                        // iterator.remove();
                        if (key.isReadable()) {
                            SocketChannel clientChanl = (SocketChannel) key.channel();
                            // 清空buffer
                            byteBuffer.clear();
                            int readSize = clientChanl.read(byteBuffer);
                            if (readSize > 0) {
                                // 丢弃换行符
                                String res = new String(byteBuffer.array(), 0, byteBuffer.position() - 1);

                                // 收到消息回调
                                clientHandleCallback.onMessageArrived(ClientHandler.this, res);
                            } else {
                                System.out.println("客户端已无法读取数据！");
                                // 退出当前客户端
                                ClientHandler.this.exitBySelf();
                                break;
                            }
                        }
                    }
                } while (!done);
            } catch (Exception e) {
                e.printStackTrace();
                if (!done) {
                    System.out.println("连接异常断开");
                    ClientHandler.this.exitBySelf();
                }
            } finally {
                // 连接关闭
                CloseUtils.close(selector);
            }
        }

        void exit() {
            done = true;
            selector.wakeup();
            CloseUtils.close(selector);
        }
    }

    class ClientWriteHandler {
        private boolean done = false;
        private final Selector selector;
        private final ByteBuffer byteBuffer;
        private final ExecutorService executorService;

        ClientWriteHandler(Selector selector) {
            this.selector = selector;
            this.byteBuffer = ByteBuffer.allocate(256);
            this.executorService = Executors.newSingleThreadExecutor();
        }

        void exit() {
            done = true;
            CloseUtils.close(selector);
            executorService.shutdownNow();
        }

        void send(String str) {
            executorService.execute(new WriteRunnable(str));
        }

        class WriteRunnable implements Runnable {
            private final String msg;

            WriteRunnable(String msg) {
                this.msg = msg + "\n";
            }

            @Override
            public void run() {
                if (ClientWriteHandler.this.done) {
                    return;
                }

                byteBuffer.clear();
                byteBuffer.put(msg.getBytes(Charset.forName("UTF-8")));

                // 反转操作, 重头开始，重新读取, 重置指针
                byteBuffer.flip();

                // 判断是否还有数据
                while (!done && byteBuffer.hasRemaining()) {

                    try {
                        int len = socketChannel.write(byteBuffer);
                        // == 0是合法的
                        if (len < 0) {
                            System.out.println("客户端已无法发送数据！");
                            ClientHandler.this.exitBySelf();
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
