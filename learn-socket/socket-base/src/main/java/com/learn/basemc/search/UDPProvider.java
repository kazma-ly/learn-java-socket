package com.learn.basemc.search;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.UUID;

/**
 * UDP 提供者 用于提供服务, 利用了UDP的广播
 */
public class UDPProvider {

    public static void main(String[] args) throws IOException {
        // 唯一标识
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn);
        provider.start();

        // 读取任意字符退出
        System.in.read();
        provider.exit();
    }

    private static class Provider extends Thread {

        private final String sn;
        private boolean done = false;
        private DatagramSocket datagramSocket;

        public Provider(String sn) {
            this.sn = sn;
        }

        @Override
        public void run() {
            System.out.println("UDPProvider Started.");
            try {
                // 监听2000 port
                datagramSocket = new DatagramSocket(20001);
                while (!done) {
                    // 构建接收实体
                    final byte[] buf = new byte[512];
                    DatagramPacket receivePack = new DatagramPacket(buf, buf.length);

                    // 接收
                    datagramSocket.receive(receivePack);

                    // 打印接收到的信息
                    String ip = receivePack.getAddress().getHostAddress();
                    int port = receivePack.getPort();
                    int dataLen = receivePack.getLength();
                    String data = new String(receivePack.getData(), 0, dataLen);
                    System.out.println("UDPProvider reveive from ip:" + ip + ":" + port + " {" + data + "}");

                    int responsePort = MessageCreator.parsePort(data);

                    if (responsePort != -1) {
                        // 回送数据
                        String responseData = MessageCreator.buildWithSN(sn);
                        byte[] responseDataBytes = responseData.getBytes();
                        DatagramPacket responsePacket = new DatagramPacket(responseDataBytes, responseDataBytes.length, receivePack.getAddress(), responsePort);
                        datagramSocket.send(responsePacket);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close();
            }
            System.out.println("UDPProider end.");
        }

        private void close() {
            if (datagramSocket != null) {
                datagramSocket.close();
                datagramSocket = null;
            }
        }

        void exit() {
            this.done = true;
            close();
        }

    }

}
