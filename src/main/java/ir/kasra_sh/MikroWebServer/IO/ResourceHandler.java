package ir.kasra_sh.MikroWebServer.IO;

import co.paralleluniverse.fibers.Suspendable;
import ir.kasra_sh.MikroWebServer.HTTPUtils.HTTPConnection;

public abstract class ResourceHandler {
    private String CONTEXT;

    public void setContext(String c){
        CONTEXT = c;
    }

    public String getContext() {
        return CONTEXT;
    }

    @Suspendable
    public abstract void handle(HTTPConnection conn);
}
