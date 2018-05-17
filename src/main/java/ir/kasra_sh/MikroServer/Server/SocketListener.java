package ir.kasra_sh.MikroServer.Server;


import co.paralleluniverse.common.monitoring.MonitorType;
import co.paralleluniverse.fibers.FiberForkJoinScheduler;
import ir.kasra_sh.MikroServer.Utils.KSocket;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.security.*;
import java.security.cert.CertificateException;
import java.sql.Time;
import java.time.Instant;
import java.util.*;
import java.util.AbstractMap.*;
import java.util.Map.*;
import java.util.concurrent.ForkJoinPool;

public class SocketListener extends Thread{
    private ServerSocket serverSocket;
    private boolean tls = false;
    private KSocket socket;
    private boolean useFibers = true;
    //private WorkerThread[] workerThreads;
    private int workers=4;
    private Set<Entry<String, Class<? extends HTTPHandler>>> routes;
    private Set<Entry<String, SimpleEntry<Class<? extends HTTPHandler>,String>>> files;
    private Set<Entry<String, InetSocketAddress>> proxies;
    private HashMap<String, HashMap<String, String>> overrides;

    private boolean stop = false;

    protected SocketListener(int port,
                             Hashtable<String,Class<? extends HTTPHandler>> routes,
                             Hashtable<String, SimpleEntry<Class<? extends HTTPHandler>,String>> files,
                             Hashtable<String, InetSocketAddress> proxies,
                             HashMap<String, HashMap<String, String>> overrides) {
        stop = false;
        this.routes = routes.entrySet();
        this.files = files.entrySet();
        this.proxies = proxies.entrySet();
        this.overrides = overrides;
        try {
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected SocketListener(int port, Hashtable<String, Class<? extends HTTPHandler>> routes,
                             Hashtable<String, SimpleEntry<Class<? extends HTTPHandler>, String>> files,
                             Hashtable<String, InetSocketAddress> proxies,
                             HashMap<String, HashMap<String, String>> overrides,
                             String jkeystore) throws IOException {
        stop = false;
        this.routes = routes.entrySet();
        this.files = files.entrySet();
        this.proxies = proxies.entrySet();
        this.overrides = overrides;
        serverSocket = makeSSLServerSocket(port,jkeystore,"1kaskaskas");
        if (serverSocket == null){
            serverSocket = new ServerSocket();
            serverSocket.bind(new InetSocketAddress(port));
        }
    }

    protected void setUseFibers(boolean uf) {
        useFibers = uf;
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
            //((SSLServerSocket)serverSocket).accept();
            ((SSLServerSocket) serverSocket).setEnabledProtocols(new String[]{"TLSv1.2"});
            for (String s: ((SSLServerSocket)serverSocket).getEnabledProtocols()) {
                System.out.println(s);
            }
            //serverSocket.addTrustMaterial()
            System.out.println("TLS Enabled !");
            tls = true;
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

        FiberForkJoinScheduler fes = null;

        ForkJoinPool forkJoinPool= null;
        if (!useFibers) {
            forkJoinPool = new ForkJoinPool(workers);
        } else {
            fes = new FiberForkJoinScheduler("sldef"+new Random(Time.from(Instant.now()).getTime()).nextLong(),workers,null, MonitorType.JMX, false);
        }

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
                if (tls) {
                    socket = new KSocket(((SSLServerSocket)serverSocket).accept());
                } else
                socket = new KSocket(serverSocket.accept());

                if (useFibers) {
                    fes.getForkJoinPool().execute(new RouterFiber(socket,routes, files, proxies, overrides));
                } else {
                    forkJoinPool.execute(new RouterFiber(socket, routes, files, proxies, overrides));
                }


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
