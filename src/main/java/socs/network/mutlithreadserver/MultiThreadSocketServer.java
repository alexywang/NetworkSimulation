package socs.network.mutlithreadserver;
import socs.network.node.Link;
import socs.network.node.Router;
import socs.network.node.RouterDescription;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.*;

public class MultiThreadSocketServer extends Thread{

    ServerSocket server;
    boolean serverOn = true;
    short port;

    Router myRouter; // Keep reference to router to send information about messages
    HashSet<ClientServiceThread> threads;


    public MultiThreadSocketServer(short port, Router router){
        super();
        this.port = port;
        myRouter = router;
        threads = new HashSet<ClientServiceThread>();
    }

    public void run(){
        try{
            server = new ServerSocket(port);
            server.setReuseAddress(true);

        }catch(IOException e){
            System.out.println("Couldn't create server socket. Quitting.");
            System.exit(-1);
        }

        System.err.println("Server Socket listening on port " + server.getLocalPort());
        System.err.println("My simulated ip: " + myRouter.getRouterDescription().simulatedIPAddress);

        // Wait for connections
        while(serverOn){
            // Accept incoming connections
            try{

                Socket clientSocket = server.accept();

                // If there are too many connections close the new connection
                if(myRouter.getNumLinks() >= myRouter.getMaxLinks()){
                    System.out.println("Maximum neighbours exceeded, closing connection");
                    clientSocket.close();
                    continue;
                }
                try{
                    // Spawn new service thread
                    System.err.println("Client attempting to attach");
                    ClientServiceThread clientThread = new ClientServiceThread(clientSocket, myRouter);
                    threads.add(clientThread);
                    clientThread.start();
                    System.err.println("A client has attached");


                }catch (Exception e){
                    e.printStackTrace();
                }



            }catch(IOException e){
                System.out.println("Exception occurred when accepting connection. Stack Trace: ");
                e.printStackTrace();
            }
        }
    }

}
