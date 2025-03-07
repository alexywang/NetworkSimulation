package socs.network.node;

import java.io.*;
import java.net.Socket;

public class Link {

  RouterDescription router1;
  RouterDescription router2;

  Socket socket;
  ObjectOutputStream out;
  ObjectInputStream in;
  short weight;


  public Link(RouterDescription r1, RouterDescription r2, Socket r2Socket, short weight) throws Exception {
    router1 = r1;
    router2 = r2;

    socket = r2Socket;
    out = new ObjectOutputStream(socket.getOutputStream());
    in = new ObjectInputStream(socket.getInputStream());
    this.weight = weight;

  }

  public String getLinkIP(){
    return router2.simulatedIPAddress;
  }

  public void setLinkStatus(RouterStatus status){
    router2.status = status;
  }



}
