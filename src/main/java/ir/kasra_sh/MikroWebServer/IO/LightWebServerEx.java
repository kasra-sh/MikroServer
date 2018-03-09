package ir.kasra_sh.MikroWebServer.IO;

import co.paralleluniverse.fibers.SuspendExecution;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.AbstractMap;
import java.util.Hashtable;

public class LightWebServerEx {
    private SocketListenerEx sl;
    private Hashtable<String,HandlerEx> routes = new Hashtable<>();
    private Hashtable<String,AbstractMap.SimpleEntry<HandlerEx,String>> filePaths = new Hashtable<>();
    private Hashtable<String,InetSocketAddress> proxies = new Hashtable<>();
    private String ks = null;

    public LightWebServerEx() {
    }

    public void addContextHandler(String context, HandlerEx h) throws Exception {
        //if (c.isInstance(Handler.class)) {
        h.setContext(context);
        routes.putIfAbsent(context.toLowerCase(), h);
        //}else throw new Exception(c.getName());
    }

    public void addFileHandler(String context, String dir, HandlerEx handler){
        handler.setContext(context);
        filePaths.putIfAbsent(context.toLowerCase(), new AbstractMap.SimpleEntry(handler,dir));
    }

    public void addProxyPath(String context,InetSocketAddress destination){
        proxies.putIfAbsent(context,destination);
    }

    public void useTLS(String keystore){
        ks = keystore;
    }

    public void start(int port, int parallelism) throws IOException, SuspendExecution {
        if (ks == null) {
            sl = new SocketListenerEx(port, routes, filePaths, proxies);
            sl.setWorkers(parallelism);
        }
        else {
            sl = new SocketListenerEx(port, routes, filePaths, ks);
            sl.setWorkers(parallelism);
        }
        sl.start();
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
