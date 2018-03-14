package ir.kasra_sh.MikroServer.Server.Proxy;

import co.paralleluniverse.fibers.Suspendable;
import ir.kasra_sh.MikroServer.HTTPUtils.HTTPConnection;
import ir.kasra_sh.MikroServer.HTTPUtils.KSocket;
import ir.kasra_sh.MikroServer.HTTPUtils.RequestParser;

import java.net.*;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Suspendable
public class ReverseProxy implements Runnable {

    private HTTPConnection user;
    private byte[] buff = new byte[4096];
    private InetSocketAddress des;
    private int len;
    private HashMap<String,String> overrides = new HashMap<>();

    public ReverseProxy(InetSocketAddress dest, HTTPConnection conn){
        user = conn;
        des = dest;
    }

    public void setOverrides(HashMap<String, String> o) {
        overrides = o;
    }
    @Suspendable
    @Override
    public void run() {
        try {
            StringBuilder p = new StringBuilder();
            p.append(user.kSocket().getSocket().getInetAddress()).append(" - ");
            p.append(Date.from(Instant.now()));
            p.append(" - Routed to ").append(des.getAddress().getHostAddress()).append(":").append(des.getPort());
            long st = System.currentTimeMillis();
            //System.out.println("Routing "+sock.getLocalAddress()+" to "+des.getHostName()+":"+des.getPort());
            //System.out.println(new String(dt,0,len));
            //System.out.println("to "+ des.getAddress().getHostAddress() + ":" + des.getPort());
            KSocket svr = new KSocket(new Socket(des.getAddress().getHostAddress(),des.getPort()));
            //System.out.println("KSocket Opened !");
            ////////////////////////
            int len = 0;
            try {
                len = Integer.valueOf(user.getHeader("Content-Length"));
            }catch (Exception e) {
            }
            svr.writeString(user.getRawHeader().toString());
            svr.flush();
            int read=0;
            while (read<len) {
                int l = user.kSocket().readBytes(buff);
                svr.writeBytes(buff,0,l);
                read += l;
                svr.flush();
            }
            RequestParser rp = new RequestParser(svr);
            rp.parseResponseHeader();
            HTTPConnection svrConn = rp.getHTTPConnection();
            if (overrides.isEmpty()) {
                user.kSocket().writeString(svrConn.getRawHeader().toString());
            }
            else {
                String resp_status = rp.getStatus();
                //System.out.println("Status : "+resp_status);
                user.kSocket().writeString(resp_status);
                for (Map.Entry<String, String> m:
                     overrides.entrySet()) {
                    svrConn.getHeaders().replace(m.getKey(), m.getValue());
                }
                for (Map.Entry<Object, Object> x :
                        svrConn.getHeaders().entrySet()) {
                    user.kSocket().writeString(x.getKey() + ": "+x.getValue()+"\r\n");
                }
                user.kSocket().writeString("\r\n");
            }
            while (true) {
                int l = svr.readBytes(buff);
                if (l<0) break;
                user.kSocket().writeBytes(buff,0,l);
                user.kSocket().flush();
            }
            user.kSocket().close();
            svr.close();
            ////////////////////////
            p.append(" - ").append(System.currentTimeMillis()-st).append("ms");
            System.out.println(p);
            //System.out.println("Route Done!");


        } catch (Exception e){
            //e.printStackTrace();
            user.writer.finish();
        } finally {
            user.writer.finish();
        }

    }
}
