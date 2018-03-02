package ir.kasra_sh.Defaults;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import ir.kasra_sh.MikroWebServer.HTTPUtils.HTTPConnection;
import ir.kasra_sh.MikroWebServer.HTTPUtils.ResponseCode;
import ir.kasra_sh.MikroWebServer.IO.Handler;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public class ProxyHandler extends Handler {

    private static ArrayList<ProxyRoute> plist = new ArrayList<>();
    private final String pattern = "[_\\.\\?\\&\\w\\d\\/]*";


    public ProxyHandler(){

    }

    public ProxyHandler(ArrayList<ProxyRoute> list){
        plist = list;
    }

    public void addProxyRoute(String pathPattern, InetSocketAddress addr){
        plist.add(new ProxyRoute(pathPattern,addr));
    }

    @Override
    public int handle(HTTPConnection conn) {
        String r = conn.getRoute();
        for (ProxyRoute pr:
             plist) {
            if (r.matches(pr.getPath()+pattern)) {
                String p = r.replaceAll(pr.getPath(),"");

                conn.setRoute(p);
                new Proxifier(conn, pr.getAddr()).start();
                /*
                try {
                    f.get();
                    System.out.println("\n\nProxify Finished\n\n");
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }*/
                //conn.writer.finish();
                return 0;
            }
        }
        return ResponseCode.BAD_GATEWAY;
    }

}
