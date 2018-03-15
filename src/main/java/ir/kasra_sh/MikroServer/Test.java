package ir.kasra_sh.MikroServer;

import co.paralleluniverse.fibers.SuspendExecution;
import ir.kasra_sh.MikroServer.Server.Mikro;

import java.io.IOException;
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
        mikro.addProxyPath("/tes*", new InetSocketAddress("localhost", 8080), ovr);

        try {
            mikro.addContextHandler("/test",new MultiHandler());
            mikro.start(8000, 5, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
