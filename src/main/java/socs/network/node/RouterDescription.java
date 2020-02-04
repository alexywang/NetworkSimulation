package socs.network.node;

public class RouterDescription {
  //used to socket communication
  public String processIPAddress;
  public short processPortNumber;
  //used to identify the router in the simulated network space
  public String simulatedIPAddress;
  //status of the router
  public RouterStatus status;

  public RouterDescription(){

  }

  public RouterDescription(String ip, short port, String sim, RouterStatus s){
    processIPAddress = ip;
    processPortNumber = port;
    simulatedIPAddress = sim;
    status = s;
  }

  // Message format
  public String toString(){
    String description = processIPAddress + " " + processPortNumber + " " + simulatedIPAddress + " " + status.name();
    return description;
  }

  // Parse router description from string format
  public static RouterDescription parseDescription(String description){
    // Returns array in order of [processIPAddress, processPortNumber, simulatedIPAddress, status]
    String[] tokens = description.split(" ");
    RouterDescription desc = new RouterDescription(tokens[0], Short.valueOf(tokens[1]), tokens[2], RouterStatus.valueOf(tokens[3]));
    return desc;
  }

}
