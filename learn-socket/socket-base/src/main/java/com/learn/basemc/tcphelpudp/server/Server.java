package com.learn.basemc.tcphelpudp.server;

import com.learn.basemc.tcphelpudp.constants.TCPConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Server {

    public static void main(String[] args) throws IOException {
        TCPServer tcpServer = new TCPServer(TCPConstants.PORT_SERVER);
        boolean success = tcpServer.start();

        if (!success) {
            System.out.println("Start TCP server failed!");
            return;
        }

        UDPProvider.start(TCPConstants.PORT_SERVER);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String inputString;
        do {
            inputString = bufferedReader.readLine();
            tcpServer.broadcast(inputString);
        } while (!"bye".equalsIgnoreCase(inputString));

        UDPProvider.stop();
        tcpServer.stop();
    }

}
