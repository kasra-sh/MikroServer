package ir.kasra_sh.MikroServer.Server;

import co.paralleluniverse.fibers.SuspendExecution;
import ir.kasra_sh.MikroServer.Server.Annotations.Route;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Time;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.AbstractMap.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.*;

public class Mikro {
    private SocketListener sl;
    private Hashtable<String,Class<? extends Handler>> routes = new Hashtable<>();
    private Hashtable<String,SimpleEntry<Class<? extends Handler>,String>> filePaths = new Hashtable<>();
    private Hashtable<String,InetSocketAddress> proxies = new Hashtable<>();
    private HashMap<String, HashMap<String, String>> overrides = new HashMap<>();
    private String ks = null;

    public Mikro() {
    }

    public void addHandler(Class<? extends Handler> h) {
        //if (c.isInstance(Handler.class)) {
        String con = h.getAnnotation(Route.class).value();
        if (con.equals("")) {
            System.out.println("Warning : couldn't add handler("+h.getName()+"); no @Route specified !");
            return;
        }

        try {
            h.newInstance().setContext(con);
            routes.putIfAbsent(con.toLowerCase(), h);
            System.out.println("Path ("+con+") routed to "+h.getName());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        //}else throw new Exception(c.getName());
    }

    public void addFileHandler(String dir, Class<? extends Handler> h){
        String con = h.getAnnotation(Route.class).value();
        if (con.equals("")) {
            System.out.println("Warning : couldn't add handler("+h.getName()+"); no @Route specified !");
            return;
        }
        try {
            h.newInstance().setContext(con);
            filePaths.putIfAbsent(con.toLowerCase(), new SimpleEntry(h, dir));
            System.out.println("Path ("+con+") routed to "+h.getName()+" as resource path");
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void addProxyPath(String context, InetSocketAddress destination, HashMap<String, String> ovr){
        proxies.putIfAbsent(context,destination);
        overrides.putIfAbsent(context,ovr);
    }

    public void useTLS(String keystore){
        ks = keystore;
    }

    public void start(int port, int parallelism, boolean useFibers) throws IOException, SuspendExecution {
        if (ks == null) {
            sl = new SocketListener(port, routes, filePaths, proxies, overrides);
            sl.setWorkers(parallelism);
        }
        else {
            sl = new SocketListener(port, routes, filePaths, proxies, overrides, ks);
            sl.setWorkers(parallelism);
        }
        sl.setUseFibers(useFibers);
        sl.start();
        System.out.println("Started @("+ Time.from(Instant.now())+ ") on Port: "+port + " ...");
    }

    public void stop(){
        sl.tryStop();
        int c = 0;
        while (sl.isAlive()) {
            c++;
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                break;
            }
            if (c == 2000) {
                try {
                    finalize();
                } catch (Throwable throwable) {  }
                return;
            }
        }
    }

}
