package ir.kasra_sh.MikroServer.Server.Proxy;

import co.paralleluniverse.fibers.Suspendable;
import ir.kasra_sh.MikroServer.HTTPUtils.HTTPConnection;
import ir.kasra_sh.MikroServer.HTTPUtils.KSocket;

import java.net.*;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Suspendable
public class ReverseProxy implements Runnable {

    private HTTPConnection con;
    private byte[] buff = new byte[4096];
    private InetSocketAddress des;
    private int len;
    private HashMap<String,String> overrides = new HashMap<>();

    public ReverseProxy(InetSocketAddress dest, HTTPConnection conn){
        con = conn;
        des = dest;
    }

    public void addOverride(String k, String v) {
        overrides.putIfAbsent(k, v);
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
            KSocket d = new KSocket(new Socket(des.getAddress().getHostAddress(),des.getPort()));
            //System.out.println("KSocket Opened !");
            ////////////////////////
            int len = 0;
            try {
                len = Integer.valueOf(con.getHeader("Content-Length"));
            }catch (Exception e) {
            }
            if (overrides.isEmpty())
                d.writeString(con.getRawHeader().toString());
            else {
                boolean found;
                for (Map.Entry<Object, Object> h:
                        con.getHeaders().entrySet()){
                    found = false;
                    for (Map.Entry<String, String> m:
                            overrides.entrySet()) {
                        if (((String)h.getKey()).equalsIgnoreCase(m.getKey())) {
                            d.writeString(m.getKey()+": "+m.getValue()+"\r\n");
                            found = true;
                            break;
                        }
                    }
                    if (found) {
                        continue;
                    }
                    d.writeString((String)h.getKey() + ": "+(String)h.getValue()+"\r\n");
                }
                d.writeString("\r\n");
            }
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
