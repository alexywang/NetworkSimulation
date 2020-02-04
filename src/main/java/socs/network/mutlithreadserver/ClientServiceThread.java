package socs.network.mutlithreadserver;
import socs.network.message.SOSPFPacket;
import socs.network.node.Router;
import socs.network.node.RouterDescription;

import java.net.*;
import java.io.*;

public class ClientServiceThread extends Thread {
    Socket clientSocket;
    RouterDescription clientDesc;
    boolean runThread = true;

    Router myRouter;
    RouterDescription theirRouter;

    ObjectOutputStream out;
    ObjectInputStream in;

    public ClientServiceThread(){
        super();
    }

    public ClientServiceThread(Socket client, Router myRouter){
        clientSocket = client;
        this.myRouter = myRouter;

        try{
            out = new ObjectOutputStream(clientSocket.getOutputStream());
            in = new ObjectInputStream(clientSocket.getInputStream());
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    // Method called when thread is started
    public void run() {

        try{
            // Continuously listen for client messages
            while(runThread){
                // Wait for messages then get router to handle them.
                SOSPFPacket packet = (SOSPFPacket) in.readObject();
                myRouter.processPacket(packet, clientSocket);
            }

        }catch(Exception e){
            e.printStackTrace();
        }finally{ // Clean up buffers and end connections
            try{
                in.close();
                out.close();
                clientSocket.close();
            }catch(IOException e){
                e.printStackTrace();
            }
        }

    }






}

