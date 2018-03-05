package ir.kasra_sh.MikroWebServer.IO.Proxy;

import co.paralleluniverse.fibers.Suspendable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.time.Instant;
import java.util.Date;

@Suspendable
public class APIReverseProxy implements Runnable {

    private Socket sock;
    private byte[] dt;
    private InetSocketAddress des;
    private int len;

    public APIReverseProxy(InetSocketAddress dest, byte[] readData, int readLen, Socket socket){
        sock = socket;
        des = dest;
        dt = readData;
        this.len = readLen;
    }

    @Suspendable
    @Override
    public void run() {
        try {
            StringBuilder p = new StringBuilder();
            p.append(sock.getInetAddress()).append(" - ");
            p.append(Date.from(Instant.now()));
            p.append(" - Routed to ").append(des.getAddress().getHostAddress()).append(":").append(des.getPort());
            long st = System.currentTimeMillis();
            //System.out.println("Routing "+sock.getLocalAddress()+" to "+des.getHostName()+":"+des.getPort());
            //System.out.println(new String(dt,0,len));
            //System.out.println("to "+ des.getAddress().getHostAddress() + ":" + des.getPort());
            Socket d = new Socket(des.getAddress().getHostAddress(),des.getPort());
            //System.out.println("Socket Opened !");
            InputStream dis = d.getInputStream();
            OutputStream dos = d.getOutputStream();
            InputStream sis = sock.getInputStream();
            OutputStream sos = sock.getOutputStream();

            // Source to Dest
            boolean sfin=false;
            while (true){
                int l = 0;
                //System.out.println("len before : "+len);
                if ((len >= dt.length) && (!sfin)) {
                    try {
                        l = sis.read(dt, len, dt.length-len);
                    } catch (Exception xx){
                        xx.printStackTrace();
                        sfin = true;
                    }

                    if (l<=0)
                        sfin = true;
                    else
                        len+=l;
                } else sfin = true;

                if (len>0) {
                    //System.out.println("len after : "+len);
                    //System.out.println(new String(dt,0,len));
                    dos.write(dt,0, len);
                    len = 0;
                    //System.out.println("Wrote to Dest");
                    if (sfin) {
                        dos.flush();
                        d.shutdownOutput();
                        sock.shutdownInput();
                        break;
                    }
                }
            }

            // Dest to Source
            sfin = false;
            while (true) {
                int l = 0;
                if (dt.length>l && !d.isInputShutdown()) {
                    try {
                        l = dis.read(dt);
                        if (d.isInputShutdown()) {
                            sfin = true;
                        }
                    }catch (Exception xx){
                        xx.printStackTrace();
                        sfin = true;
                    }
                    if (l<=0) {
                        d.shutdownInput();
                        sfin = true;
                    }

                }
                if (l>0) {
                    try {
                        sos.write(dt,0,l);
                        sos.flush();
                        l = 0;
                    } catch (Exception xxx){
                        break;
                    }
                }
                if (sfin) {
                    break;
                }
            }
            p.append(" - ").append(System.currentTimeMillis()-st).append("ms");
            System.out.println(p);
            //System.out.println("Route Done!");


        } catch (Exception e){
            //e.printStackTrace();
            try { sock.close(); } catch (IOException e1) {}
        } finally {
            try {
                sock.close();
            } catch (IOException e) {
            }
        }

    }
}
