package ir.kasra_sh.MikroWebServer.IO;


import co.paralleluniverse.common.monitoring.MonitorType;
import co.paralleluniverse.fibers.FiberForkJoinScheduler;
import ir.kasra_sh.MikroWebServer.HTTPUtils.SocketIO;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.swing.text.html.parser.Entity;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.security.*;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.*;

public class SocketListenerEx extends Thread{
    private ServerSocket serverSocket;
    private SocketReceiver[] socketReceivers;
    private SocketIO socket;
    //private WorkerThread[] workerThreads;
    private int workers=4;
    private Set<Map.Entry<String,HandlerEx>> routes;
    private Set<Map.Entry<String,AbstractMap.SimpleEntry<HandlerEx,String>>> files;
    private Set<Map.Entry<String, InetSocketAddress>> proxies;

    private boolean stop = false;

    protected SocketListenerEx(int port,
                               Hashtable<String,HandlerEx> routes,
                               Hashtable<String,AbstractMap.SimpleEntry<HandlerEx,String>> files,
                               Hashtable<String, InetSocketAddress> proxies) {
        stop = false;
        this.routes = routes.entrySet();
        this.files = files.entrySet();
        this.proxies = proxies.entrySet();
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected SocketListenerEx(int port, Hashtable<String, HandlerEx> routes, Hashtable<String,AbstractMap.SimpleEntry<HandlerEx, String>> files, String jkeystore) throws IOException {
        stop = false;
        this.routes = routes.entrySet();
        this.files = files.entrySet();
        serverSocket = makeSSLServerSocket(port,jkeystore,"PASSWORD");
        if (serverSocket == null){
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(port));
        }
    }

    private ServerSocket makeSSLServerSocket(int port, String jkeystore, String psw){
        //String psw = "1kaskaskas";
        try {
            ServerSocket serverSocket;
            KeyStore keystore = null;
            FileInputStream fis = null;
            try {
                keystore = KeyStore.getInstance("JKS");
                fis = new FileInputStream(jkeystore);
                keystore.load(fis, psw.toCharArray());
            } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
                keystore = null;
                return null;

            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) { }
                    fis = null;
                }
            }
            SSLContext sslContext = null;
            KeyManagerFactory kmf = null;
            if (keystore == null) {
                // throw exception
                return null;
            }
            try {
                kmf = KeyManagerFactory.getInstance("PKIX");
                kmf.init(keystore, psw.toCharArray());
            } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
                //e.printStackTrace();
                kmf = null;
                return null;
                // throw exception
            }
            try {
                sslContext = SSLContext.getInstance("TLSv1.2");
                //System.out.println(kmf==null); // prints false
                sslContext.init(kmf.getKeyManagers(), null, null);
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                // throw exception
            }

            serverSocket = sslContext.getServerSocketFactory().createServerSocket(port);
            ((SSLServerSocket)serverSocket).accept();
            ((SSLServerSocket) serverSocket).setEnabledProtocols(new String[]{"TLSv1.2"});
            for (String s: ((SSLServerSocket)serverSocket).getEnabledProtocols()) {
                System.out.println(s);
            }
            //serverSocket.addTrustMaterial()
            return serverSocket;

            /*for (String s: ((SSLServerSocket)serverSocket).getEnabledCipherSuites()) {
                System.out.println(s);
            }*/
        }catch (Exception e){
            return null;
        }

    }

    public void tryStop(){
        stop = true;
        try {
            serverSocket.close();
        } catch (IOException e) {
        }
    }

    @Override
    public void run() {
        long ctime, ptime = 0;
        int csv, psv=0;

        FiberForkJoinScheduler fes =
                new FiberForkJoinScheduler("sldef"+new Random(Date.from(Instant.now()).getTime()).nextLong(),workers,null, MonitorType.JMX, false);


        //FiberExecutorScheduler fes = new FiberExecutorScheduler("Sch", Executors.newWorkStealingPool(1));

        while (true) {
            try {
                if (stop) {
                    System.out.println("\n(Info)-> Stopping Server ...");
                    serverSocket.close();
                    System.out.println("\n(Info)-> Server Stopped !");
                    return;
                }

                socket = null;
                socket = new SocketIO(serverSocket.accept());

                fes.getForkJoinPool().execute(new RouterFiberEx(socket,routes, files, proxies));

            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }

    public int getWorkers() {
        return workers;
    }

    public void setWorkers(int workers) {
        this.workers = workers;
    }
}
