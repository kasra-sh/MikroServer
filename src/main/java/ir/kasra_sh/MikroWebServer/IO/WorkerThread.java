package ir.kasra_sh.MikroWebServer.IO;


import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;

public class WorkerThread extends Thread implements SocketReceiver{

    private ArrayBlockingQueue<Socket> abq = new ArrayBlockingQueue<>(1000);

    private int gcc=0;
    private final static int GCCMAX=1024;
    public boolean stopped = false;

    protected WorkerThread(){
        //setDaemon(true);
        setPriority(MAX_PRIORITY);
    }

    @Override
    public void interrupt() {
        abq.clear();
        stopped = true;
        super.interrupt();
    }


    @Override
    public void run() {
        super.run();
        while (true) {
            /*if (!abq.isEmpty())
                try {
                    Strand.sleep(0,1);
                } catch (SuspendExecution suspendExecution) { }
                catch (InterruptedException e) { }*/
            try {
                //Socket sc = abq.take();
                //RequestFiber rf = new RequestFiber(abq.take());
                //rf.start();
                //System.out.println("Fiber !");
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }

    }

    @Override
    public void onReceive(Socket s) {
        try {
            abq.put(s);
            //System.out.println("Put");
            //ServerStats.acceptedSockets+=1;
            //System.out.println("OnReceive");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
