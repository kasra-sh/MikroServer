package ir.kasra_sh.MikroWebServer.IO;

import co.paralleluniverse.fibers.SuspendExecution;
import ir.kasra_sh.MikroWebServer.IO.DynamicFS.DynamicSocketListener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Hashtable;

public class LightWebServer {
    private SocketListener sl;
    private DynamicSocketListener dsl;
    private Hashtable<String,Handler> routes = new Hashtable<>();
    private Hashtable<String,String> filePaths = new Hashtable<>();
    private Hashtable<String,InetSocketAddress> proxies = new Hashtable<>();
    private String ks = null;

    public LightWebServer() {
    }

    public void addContextHandler(String context, Handler h) throws Exception {
        //if (c.isInstance(Handler.class)) {
        h.setContext(context);
        routes.putIfAbsent(context.toLowerCase(), h);
        //}else throw new Exception(c.getName());
    }

    public void addFilePath(String context,String dir){
        filePaths.put(context,dir);
    }

    public void addProxyPath(String context,InetSocketAddress destination){
        proxies.put(context,destination);
    }

    public void addResourceHandler(String context, Handler h) throws Exception {
        h.setContext(context);
        routes.putIfAbsent(context.toLowerCase(), h);
    }

    public void useTLS(String keystore){
        ks = keystore;
    }

    public void start(int port, int parallelism) throws IOException, SuspendExecution {
        if (ks == null) {
            sl = new SocketListener(port, routes, filePaths);
            sl.setWorkers(parallelism);
        }
        else {
            sl = new SocketListener(port, routes, filePaths, ks);
            sl.setWorkers(parallelism);
        }
        sl.start();
    }

    public void startDynamic(int port, int parallelism) throws IOException, SuspendExecution {
        if (ks == null) {
            dsl = new DynamicSocketListener(port, routes, filePaths, proxies);
            dsl.setWorkers(parallelism);
        }
        else {
            dsl = new DynamicSocketListener(port, routes, filePaths, ks);
            dsl.setWorkers(parallelism);
        }
        dsl.start();
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
