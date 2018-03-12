package ir.kasra_sh.MikroServer.Server;

import com.google.common.util.concurrent.AtomicDouble;

import java.util.concurrent.atomic.AtomicInteger;

public final class ServerStats {
    public static AtomicInteger activeSockets = new AtomicInteger();
    public static AtomicInteger servedSockets = new AtomicInteger();
    public static AtomicDouble  meanProcess   = new AtomicDouble();
    public static AtomicInteger proc = new AtomicInteger(0);

    public static synchronized void incActive(){
        activeSockets.set(activeSockets.get()+1);
    }
    public static synchronized void decActive(){
        activeSockets.set(activeSockets.get()-1);
    }
    public static synchronized void incServed(){
        servedSockets.set(servedSockets.get()+1);
    }
    public static synchronized void decServed(){
        servedSockets.set(servedSockets.get()-1);
    }
    public static synchronized void addProc(double p) {
        int i = proc.incrementAndGet();
        meanProcess.set(((meanProcess.get()*(i-1))+p)/i);
    }

    public static synchronized double getMeanProc(){
        return meanProcess.get();
    }

}
