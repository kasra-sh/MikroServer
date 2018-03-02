package ir.kasra_sh.MikroWebServer.IO.DynamicFS;


import co.paralleluniverse.common.monitoring.MonitorType;
import co.paralleluniverse.fibers.FiberForkJoinScheduler;
import ir.kasra_sh.MikroWebServer.IO.Handler;
import ir.kasra_sh.MikroWebServer.IO.ServerStats;
import ir.kasra_sh.MikroWebServer.IO.SocketReceiver;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class DynamicSocketListener extends Thread{
    private ServerSocket serverSocket;
    private SocketReceiver[] socketReceivers;
    private Socket socket;
    //private WorkerThread[] workerThreads;
    private int workers=4;
    private long i = 0;
    private int cnt=0;
    private FiberForkJoinScheduler f;

    private Set<Map.Entry<String,Handler>> routes;
    private Set<Map.Entry<String,InetSocketAddress>> proxies;
    private Set<Map.Entry<String,String>> files;

    private boolean stop = false;

    public DynamicSocketListener(int port, Hashtable<String,Handler> routes, Hashtable<String,String> files) {
        stop = false;
        this.routes = routes.entrySet();
        this.files = files.entrySet();
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DynamicSocketListener(int port, Hashtable<String,Handler> routes, Hashtable<String,String> files, Hashtable<String,InetSocketAddress> proxies) {
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

    public DynamicSocketListener(int port, Hashtable<String, Handler> routes, Hashtable<String,String> files, String jkeystore) throws IOException {
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

        FiberForkJoinScheduler fes =
                new FiberForkJoinScheduler("dsldef"+ new Random(System.nanoTime()).nextLong(),workers,null, MonitorType.JMX, false);

        while (true) {
            try {
                if (stop) {
                    System.out.println("\n(Info)-> Stopping Server ...");
                    serverSocket.close();
                    System.out.println("\n(Info)-> Server Stopped !");
                    return;
                }

                /*if (ServerStats.activeSockets.get()>100){
                    System.gc();
                    System.out.println("GC");
                }*/
                socket = null;
                socket = serverSocket.accept();
                System.out.println("Accepted "+socket.getInetAddress());
                fes.getForkJoinPool().execute(new DynamicRequestFiber(socket,routes, files, proxies, fes, null));

            } catch (IOException e) {
                e.printStackTrace();
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
