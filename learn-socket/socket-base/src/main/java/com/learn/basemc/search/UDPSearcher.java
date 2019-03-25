package com.learn.basemc.search;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class UDPSearcher {

    private static final int LISTEN_PORT = 30000;

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("UDPSearcher started.");

        Listener listener = listen();

        sendBroadcast();

        System.in.read();
        List<Device> devices = listener.getDevicesAndClose();

        for (Device device : devices) {
            System.out.println(device);
        }

        // 完成
        System.out.println("UDPSearcher end.");
    }

    private static Listener listen() throws InterruptedException {
        System.out.println("UDPSearch listener started");
        CountDownLatch countDownLatch = new CountDownLatch(1);

        Listener listener = new Listener(LISTEN_PORT, countDownLatch);
        listener.start();

        countDownLatch.await();
        return listener;
    }

    private static void sendBroadcast() throws IOException {
        System.out.println("UDPSearcher sendBroadcast started.");

        // 作为搜索方，让系统自动分配端口
        DatagramSocket datagramSocket = new DatagramSocket();

        // 构建一份回送数据
        String reqData = MessageCreator.buildWithPort(LISTEN_PORT);
        byte[] reqDataBytes = reqData.getBytes();

        // 发送
        DatagramPacket reqPack = new DatagramPacket(reqDataBytes, reqDataBytes.length);
        reqPack.setAddress(InetAddress.getByName("255.255.255.255")); // 广播地址
        reqPack.setPort(20000);

        // 发送信息
        datagramSocket.send(reqPack);

        datagramSocket.close();

        System.out.println("UDPSearcher sendBroadcast close.");
    }

    private static class Device {
        private int port;
        private String ip;
        private String sn;

        public Device(int port, String ip, String sn) {
            this.port = port;
            this.ip = ip;
            this.sn = sn;
        }

        public int getPort() {
            return port;
        }

        public String getIp() {
            return ip;
        }

        public String getSn() {
            return sn;
        }

        @Override
        public String toString() {
            return "Device{" +
                    "port=" + port +
                    ", ip='" + ip + '\'' +
                    ", sn='" + sn + '\'' +
                    '}';
        }
    }

    private static class Listener extends Thread {

        private final int listenPort;
        private final CountDownLatch countDownLatch;
        private final List<Device> devices = new ArrayList<>();
        private boolean done = false;
        private DatagramSocket datagramSocket;

        public Listener(int listenPort, CountDownLatch countDownLatch) {
            this.listenPort = listenPort;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            // 已启动
            countDownLatch.countDown();

            try {
                datagramSocket = new DatagramSocket(LISTEN_PORT);
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

                    String sn = MessageCreator.parseSN(data);
                    if (sn != null) {
                        Device device = new Device(port, ip, sn);
                        devices.add(device);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                close();
            }

            System.out.println("UDPSearch listener finish");
        }

        private void close() {
            if (datagramSocket != null) {
                datagramSocket.close();
                datagramSocket = null;
            }
        }

        List<Device> getDevicesAndClose() {
            done = true;
            close();
            return devices;
        }

    }

}
