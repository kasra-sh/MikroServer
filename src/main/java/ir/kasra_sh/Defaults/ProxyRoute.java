package ir.kasra_sh.Defaults;

import java.net.InetSocketAddress;
import java.util.List;

public class ProxyRoute {

    private String path;
    private InetSocketAddress addr;

    public ProxyRoute(String path, InetSocketAddress addr){
        this.path = path;
        this.addr = addr;
    }

    public String getPath() {
        return path;
    }

    public InetSocketAddress getAddr() {
        return addr;
    }
}
