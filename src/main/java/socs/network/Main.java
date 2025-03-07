package socs.network;

import socs.network.node.Router;
import socs.network.util.Configuration;

import java.util.Random;


public class Main {

  public static void main(String[] args) {

//    if (args.length != 1) {
//      System.out.println("usage: program conf_path");
//      System.exit(1);
//    }
//
//    Router r = new Router(new Configuration(args[0]));
//    r.terminal();

    // Temporary code for testing in IDE
    //System.err.println("Starting Router");
    Random rand = new Random();
    Router r = new Router(new Configuration("conf/router"+(1+rand.nextInt(6))+".conf"));
    System.err.println(r.getRouterDescription().simulatedIPAddress + ":"+r.getRouterDescription().processPortNumber);
    r.terminal();
  }
}
