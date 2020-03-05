package socs.network.node;

import socs.network.message.LSA;
import socs.network.message.LinkDescription;
import socs.network.message.SOSPFPacket;
import socs.network.mutlithreadserver.MultiThreadSocketServer;
import socs.network.util.Configuration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.*;


public class Router {

  protected LinkStateDatabase lsd;

  int lastSeqNumber = 0;

  RouterDescription rd = new RouterDescription();

  //assuming that all routers are with 4 ports
  Link[] ports = new Link[4];

  // Server
  MultiThreadSocketServer server;


  public Router(Configuration config) {
    rd.simulatedIPAddress = config.getString("socs.network.router.ip");
    rd.processIPAddress = config.getString("socs.network.router.host");
    rd.processPortNumber = config.getShort("socs.network.router.port");

    lsd = new LinkStateDatabase(rd);

    // Temporary random port
    Random r = new Random();
    // Initialize my server
    server = new MultiThreadSocketServer(rd.processPortNumber, this);
    server.start();
  }

  /**
   * output the shortest path to the given destination ip
   * <p/>
   * format: source ip address  -> ip address -> ... -> destination ip
   *
   * @param destinationIP the ip adderss of the destination simulated router
   */
  private void processDetect(String destinationIP) {

  }

  /**
   * disconnect with the router identified by the given destination ip address
   * Notice: this command should trigger the synchronization of database
   *
   * @param portNumber the port number which the link attaches at
   */
  private void processDisconnect(short portNumber) {

  }

  /**
   * attach the link to the remote router, which is identified by the given simulated ip;
   * to establish the connection via socket, you need to indentify the process IP and process Port;
   * additionally, weight is the cost to transmitting data through the link
   * <p/>
   * NOTE: this command should not trigger link database synchronization
   */
   private void processAttach(String processIP, short processPort,
                             String simulatedIP, short weight) {
     // Check if already previously attached/connected
     if (hasLink(simulatedIP)) {
       System.out.println("Already attached to " + simulatedIP);
       return;
     }

     // Prevent attaching to self
     if(simulatedIP.equals(rd.simulatedIPAddress)){
       System.out.println("Cannot attach to self.");
       return;
     }

     // Create new socket and attach to the ServerSocket of the desired Router
     Socket socket;

    try{
       socket = new Socket(processIP, (int) processPort);
       //System.err.println("Attached.");
     }catch(Exception e){
       System.out.println("Failed to attach to " + simulatedIP);
       return;
     }

     // Map this socket to the simulated ip address and create link
     RouterDescription newNeighbourRd = new RouterDescription(processIP, processPort, simulatedIP, RouterStatus.INIT);
     try{
       addLink(new Link(rd, newNeighbourRd, socket, weight));
     }catch(Exception e){
       System.out.println("Reached maximum links.");
       return;
     }

  }

  /**
   * broadcast Hello to neighbors
   */
  private void processStart() {
    for(int i = 0; i < ports.length; i++){
      if(ports[i] != null){
        SOSPFPacket packet = new SOSPFPacket(ports[i].router1, ports[i].router2, (short)0);
        packet.weight = ports[i].weight;
        try{
          ports[i].out.writeObject(packet);
          ports[i].out.flush();
        }catch(IOException e){
          System.out.println("Failed to send HELLO to " + packet.dstIP);
        }
      }
    }



  }

  /**
   * attach the link to the remote router, which is identified by the given simulated ip;
   * to establish the connection via socket, you need to indentify the process IP and process Port;
   * additionally, weight is the cost to transmitting data through the link
   * <p/>
   * This command does trigger the link database synchronization
   */
  private void processConnect(String processIP, short processPort,
                              String simulatedIP, short weight) {

  }

  /**
   * output the neighbors of the routers
   */
  private void processNeighbors() {
    for(int i = 0; i < ports.length; i++){
      if(ports[i] != null && ports[i].router2.status == RouterStatus.TWO_WAY){
        System.out.println(ports[i].getLinkIP() + " " + ports[i].weight);
      }
    }
  }

  /**
   * disconnect with all neighbors and quit the program
   */
  private void processQuit() {

  }

  //---------------
  // Helper Methods
  //---------------
  public RouterDescription getRouterDescription(){
    return rd;
  }

  public void addLink(Link link) throws Exception{
    for(int i = 0; i < ports.length; i++){
      if(ports[i] == null){
        ports[i] = link;
        lsd.addMyLink(link, i);
        return;
      }
    }
    // If no free ports throw Exception
    throw new Exception("No ports available for new link.");
  }

  public Link getLink(String simulatedIP){
    for(int i = 0; i < ports.length; i++){
      if(ports[i] != null && ports[i].router2.simulatedIPAddress.equals(simulatedIP)){
        return ports[i];
      }
    }
    return null;
  }

  public int getNumLinks(){
    int links = 0;
    for(int i = 0; i < ports.length; i++){
      if(ports[i] != null){
        links ++;
      }
    }
    return links;
  }

  // Check if a link already exists with the given simulated ip
  public boolean hasLink(String simulatedIP){
    for(int i = 0; i < ports.length; i++){
      if(ports[i] != null && ports[i].router2.simulatedIPAddress.equals(simulatedIP)){
        return true;
      }
    }
    return false;
  }

  public int getMaxLinks(){
    return ports.length;
  }

  //-----------
  // IO Methods
  //-----------

  // When a client service thread receives a message, this method is called
  public void processPacket(SOSPFPacket packet, Socket sender){
    if(packet.sospfType == 0){ // HELLO
      handleHello(packet, sender);
    }

    if(packet.sospfType == 1){ // Handle Link State Update
      handleLSA(packet, sender);
    }
  }

  // Deal with incoming hello packet
  private void handleHello(SOSPFPacket packet, Socket sender){
    Link link = getLink(packet.srcIP);

    if(link != null && link.router2.status == RouterStatus.INIT){ // Link is currently at INIT, set status as TWO-WAY
      System.out.println("received HELLO from " + packet.srcIP);
      link.setLinkStatus(RouterStatus.TWO_WAY);
      System.out.println("set " + packet.srcIP + " state to TWO_WAY");
      sendHello(packet.srcIP);

      try{
        Thread.sleep(250);
        System.err.println("Forwarding LSA to neighbours");
        forwardLSA(generateLSA(), rd.simulatedIPAddress);
      }catch(Exception e){

      }
    }else if(link == null){ // No link yet, attach to the sender and send hello back
      System.out.println("received HELLO from " + packet.srcIP);
      System.out.println(packet.srcProcessIP + " " + packet.srcProcessPort);
      processAttach(packet.srcProcessIP, packet.srcProcessPort, packet.srcIP, packet.weight);
      System.out.println("set " + packet.srcIP + " state to INIT");
      sendHello(packet.srcIP);
    }
  }

  // Send hello to a single router
  public void sendHello(String simulatedIP){
    Link link = getLink(simulatedIP);
    SOSPFPacket packet = new SOSPFPacket(link.router1, link.router2, (short)0);
    try{
      link.out.writeObject(packet);
      link.out.flush();

    }catch(IOException e){
      System.out.println("Failed to send HELLO to "+ packet.dstIP);
      e.printStackTrace();
    }
  }

  // Return an LSA with information about all my neighbours
  private LSA generateLSA(){
    LSA myLSA = new LSA(this.rd.simulatedIPAddress, lastSeqNumber);
    lastSeqNumber++;

    // Generate neighbour descriptions and prepare LSA
    for(int i = 0; i < ports.length; i++){
      if(ports[i] != null){
        System.err.println(ports[i].getLinkIP());
        LinkDescription desc = new LinkDescription(ports[i].router1.simulatedIPAddress, ports[i].getLinkIP(), i, ports[i].weight);
        myLSA.links.add(desc);
      }
    }

    return myLSA;
  }

  // Forward an LSA packet to all neighbours
  private void forwardLSA(LSA lsa, String sender){

    for(int i = 0; i < ports.length; i++){
      if(ports[i] != null){
        Link link = ports[i];
        // Don't send if the neighbour sent it to  you in the first place
        if(link.router2.simulatedIPAddress.equals(sender)){
          continue;
        }

        SOSPFPacket LSAPacket = new SOSPFPacket(link.router1, link.router2, lsa);

        // Attempt send.
        try{
          link.out.writeObject(LSAPacket);
          link.out.flush();
        }catch(IOException e){
          System.out.println("Failed to send LSA update to " + ports[i].router2.simulatedIPAddress);
          e.printStackTrace();
        }

      }
    }
  }


  // Handle a LSA incoming packet
  private void handleLSA(SOSPFPacket packet, Socket sender){
    LSA receivedLSA = packet.lsaArray.get(0);
    Link link = getLink(packet.srcIP);
    if(link != null && link.router2.status == RouterStatus.TWO_WAY){
      System.err.println("Receiving LSA Update from " + packet.srcIP);
      lsd.addLSA(receivedLSA);
      System.out.println(lsd);
      forwardLSA(receivedLSA, packet.srcIP);
    }
  }


  public void terminal() {
    try {
      InputStreamReader isReader = new InputStreamReader(System.in);
      BufferedReader br = new BufferedReader(isReader);
      System.out.print(">> ");
      String command = br.readLine();
      while (true) {
        if (command.startsWith("detect ")) {
          String[] cmdLine = command.split(" ");
          processDetect(cmdLine[1]);
        } else if (command.startsWith("disconnect ")) {
          String[] cmdLine = command.split(" ");
          processDisconnect(Short.parseShort(cmdLine[1]));
        } else if (command.startsWith("quit")) {
          processQuit();
        } else if (command.startsWith("attach ")) {
          String[] cmdLine = command.split(" ");
          processAttach(cmdLine[1], Short.parseShort(cmdLine[2]),
                  cmdLine[3], Short.parseShort(cmdLine[4]));
        } else if (command.equals("start")) {
          processStart();
        } else if (command.startsWith("connect ")) {
          String[] cmdLine = command.split(" ");
          processConnect(cmdLine[1], Short.parseShort(cmdLine[2]),
                  cmdLine[3], Short.parseShort(cmdLine[4]));
        } else if (command.equals("neighbors")) {
          //output neighbors
          processNeighbors();
        } else {
          //invalid command
          break;
        }
        System.out.print(">> ");
        command = br.readLine();
      }
      isReader.close();
      br.close();
    } catch (Exception e) {
      e.printStackTrace();
      // After dealing with exception keep terminal active
      terminal();
    }
  }

}
