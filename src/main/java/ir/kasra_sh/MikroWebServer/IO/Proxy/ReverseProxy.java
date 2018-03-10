package ir.kasra_sh.MikroWebServer.IO.Proxy;

import co.paralleluniverse.fibers.Suspendable;
import ir.kasra_sh.HTTPUtils.HTTPConnection;
import ir.kasra_sh.HTTPUtils.SocketIO;

import java.net.*;
import java.time.Instant;
import java.util.Date;

@Suspendable
public class ReverseProxy implements Runnable {

    private HTTPConnection con;
    private byte[] buff = new byte[4096];
    private InetSocketAddress des;
    private int len;

    public ReverseProxy(InetSocketAddress dest, HTTPConnection conn){
        con = conn;
        des = dest;
    }

    @Suspendable
    @Override
    public void run() {
        try {
            StringBuilder p = new StringBuilder();
            p.append(con.socketIO().getSocket().getInetAddress()).append(" - ");
            p.append(Date.from(Instant.now()));
            p.append(" - Routed to ").append(des.getAddress().getHostAddress()).append(":").append(des.getPort());
            long st = System.currentTimeMillis();
            //System.out.println("Routing "+sock.getLocalAddress()+" to "+des.getHostName()+":"+des.getPort());
            //System.out.println(new String(dt,0,len));
            //System.out.println("to "+ des.getAddress().getHostAddress() + ":" + des.getPort());
            SocketIO d = new SocketIO(new Socket(des.getAddress().getHostAddress(),des.getPort()));
            //System.out.println("SocketIO Opened !");
            ////////////////////////
            int len = 0;
            try {
                len = Integer.valueOf(con.getHeader("Content-Length"));
            }catch (Exception e) {
            }
            d.writeString(con.getRawHeader().toString());
            d.flush();
            int read=0;
            while (read<len) {
                int l = con.socketIO().readBytes(buff);
                d.writeBytes(buff,0,l);
                read += l;
                d.flush();
            }

            while (true) {
                int l = d.readBytes(buff);
                if (l<0) break;
                con.socketIO().writeBytes(buff,0,l);
                con.socketIO().flush();
            }
            con.socketIO().close();
            d.close();
            ////////////////////////
            p.append(" - ").append(System.currentTimeMillis()-st).append("ms");
            System.out.println(p);
            //System.out.println("Route Done!");


        } catch (Exception e){
            e.printStackTrace();
            con.writer.finish();
        } finally {
            con.writer.finish();
        }

    }
}
