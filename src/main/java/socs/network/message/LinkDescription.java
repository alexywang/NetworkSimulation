package socs.network.message;

import java.io.Serializable;

public class LinkDescription implements Serializable {
  public String linkID;
  public int portNum;
  public int tosMetrics;

  public LinkDescription(){

  }

  public LinkDescription(String neighbourIP, int portNum, int distance){
    this.linkID = neighbourIP;
    this.portNum = portNum;
    this.tosMetrics = distance;
  }

  public String toString() {
    return linkID + ","  + portNum + "," + tosMetrics;
  }
}
