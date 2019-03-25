package com.learn.basemc.tcphelpudp.server;

import com.learn.basemc.tcphelpudp.constants.UDPConstants;
import com.learn.basemc.tcphelpudp.utils.ByteUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.UUID;

public class UDPProvider {

    private static Provider PROVIDER_INSTANCE;

    public static void start(int port) {
        stop();

        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn, port);
        provider.start();
        PROVIDER_INSTANCE = provider;
    }

    static void stop() {
        if (PROVIDER_INSTANCE != null) {
            PROVIDER_INSTANCE.exit();
            PROVIDER_INSTANCE = null;
        }
    }

    private static class Provider extends Thread {

        private final byte[] sn;
        private final int port;
        private boolean done = false;
        private DatagramSocket datagramSocket = null;

        // 存储消息的Buffer
        final byte[] buffer = new byte[128];

        public Provider(String sn, int port) {
            this.sn = sn.getBytes(Charset.forName("UTF-8"));
            this.port = port;
        }

        @Override
        public void run() {
            System.out.println("UDPProvider Starter");

            try {
                datagramSocket = new DatagramSocket(UDPConstants.PORT_SERVER);
                // 接收消息的packet
                DatagramPacket receivePack = new DatagramPacket(buffer, buffer.length);

                while (!done) {
                    // 接收
                    datagramSocket.receive(receivePack);

                    String clientIp = receivePack.getAddress().getHostAddress();
                    int clientDataLen = receivePack.getLength();
                    byte[] clientData = receivePack.getData();
                    boolean isValid = clientDataLen >= (UDPConstants.HEADER.length + 2 + 4) && ByteUtils.startsWith(clientData, UDPConstants.HEADER);
                    if (!isValid) {
                        continue;
                    }

                    // 解析命令与回送端口
                    int index = UDPConstants.HEADER.length;
                    short cmd = (short) ((clientData[index++] << 8) | (clientData[index++] & 0xff));
                    int responsePort = (((clientData[index++]) << 24) |
                            ((clientData[index++] & 0xff) << 16) |
                            ((clientData[index++] & 0xff) << 8) |
                            ((clientData[index] & 0xff)));

                    // 判断合法性
                    if (cmd == 1 && responsePort > 0) {
                        // 构建一份回送数据
                        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                        byteBuffer.put(UDPConstants.HEADER);
                        byteBuffer.putShort((short) 2);
                        byteBuffer.putInt(port);
                        byteBuffer.put(sn);
                        int len = byteBuffer.position();
                        // 直接根据发送者构建一份回送信息
                        DatagramPacket responsePacket = new DatagramPacket(buffer,
                                len,
                                receivePack.getAddress(),
                                responsePort);
                        datagramSocket.send(responsePacket);
                        System.out.println("UDPProvider response to:" + clientIp + "\tport:" + responsePort + "\tdataLen:" + len);
                    } else {
                        System.out.println("UDPProvider receive cmd nonsupport; cmd:" + cmd + "\tport:" + port);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void exit() {
            done = true;
            close();
        }

        private void close() {
            if (datagramSocket != null) {
                datagramSocket.close();
                datagramSocket = null;
            }
        }

    }

}
