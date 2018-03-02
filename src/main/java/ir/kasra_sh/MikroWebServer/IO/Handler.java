package ir.kasra_sh.MikroWebServer.IO;

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import ir.kasra_sh.MikroWebServer.HTTPUtils.HTTPConnection;
import ir.kasra_sh.MikroWebServer.HTTPUtils.ResponseWriter;

public abstract class Handler {
    private String CONTEXT;

    public void setContext(String c){
        CONTEXT = c;
    }

    public String getContext() {
        return CONTEXT;
    }

    public abstract int handle(HTTPConnection conn);
}
