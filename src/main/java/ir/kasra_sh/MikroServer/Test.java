package ir.kasra_sh.MikroServer;

import ir.kasra_sh.MikroServer.Server.Mikro;

import java.net.InetSocketAddress;
import java.util.HashMap;

public class Test {
    public static void main(String ... args) {
        new Test(args);
    }

    public Test(String ... args) {
        Mikro mikro = new Mikro();
        HashMap<String, String> ovr = new HashMap<>();
        ovr.putIfAbsent("Server", "MikroServer");
        try {
            mikro.addHandler(MultiHandler.class);
            mikro.addProxyPath("/tes*", new InetSocketAddress("localhost", 8080), ovr);
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            mikro.start(8000, 5, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
