package socs.network.message;

import socs.network.node.Link;
import socs.network.node.RouterDescription;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

public class SOSPFPacket implements Serializable {

  public SOSPFPacket(){

  }

  public SOSPFPacket(RouterDescription source, RouterDescription dest, short type){
    srcProcessIP = source.processIPAddress;
    srcProcessPort = source.processPortNumber;
    srcIP = source.simulatedIPAddress;
    dstIP = dest.simulatedIPAddress;
    sospfType = type;
    //Todo: what do i fill in for routerID and neighborID?
  }

  //for inter-process communication
  public String srcProcessIP;
  public short srcProcessPort;

  //simulated IP address
  public String srcIP;
  public String dstIP;

  //common header
  public short sospfType; //0 - HELLO, 1 - LinkState Update
  public String routerID;

  //used by HELLO message to identify the sender of the message
  //e.g. when router A sends HELLO to its neighbor, it has to fill this field with its own
  //simulated IP address
  public String neighborID; //neighbor's simulated IP address

  //used by LSAUPDATE
  public Vector<LSA> lsaArray = null;

  public void printValues(){
    StringBuilder result = new StringBuilder();
    String newLine = System.getProperty("line.separator");

    result.append( this.getClass().getName() );
    result.append( " Object {" );
    result.append(newLine);

    //determine fields declared in this class only (no fields of superclass)
    Field[] fields = this.getClass().getDeclaredFields();

    //print field names paired with their values
    for ( Field field : fields  ) {
      result.append("  ");
      try {
        result.append( field.getName() );
        result.append(": ");
        //requires access to private field:
        result.append( field.get(this) );
      } catch ( IllegalAccessException ex ) {
        System.out.println(ex);
      }
      result.append(newLine);
    }
    result.append("}");

    System.out.println(result.toString());
  }
}
