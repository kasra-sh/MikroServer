package ir.kasra_sh.Handlers;

import co.paralleluniverse.fibers.Suspendable;
import ir.kasra_sh.MikroWebServer.HTTPUtils.HTTPConnection;
import ir.kasra_sh.MikroWebServer.HTTPUtils.ResponseCode;
import ir.kasra_sh.MikroWebServer.IO.Handler;
import ir.kasra_sh.MikroWebServer.Utils.MimeTypes;

import java.util.Map;

public class EchoHandler extends Handler{
    @Override
    @Suspendable
    public int handle(HTTPConnection conn) {
        String rt = conn.getRoute();
        conn.writer.getHeader().setStatus(ResponseCode.OK);
        conn.writer.getHeader().setContentType(MimeTypes.Text.HTML);
        conn.writer.getHeader().setProperty("Connection","close");

        StringBuilder sb = new StringBuilder(2048);
        sb.append(rt).append("<br>");
        for (Map.Entry e:
                conn.getHeaders().entrySet()) {
            sb.append(e.getKey().toString()).append(":").append(e.getValue()).append("<br>");
        }
        conn.writer.writeAll(sb.toString());
        conn.writer.finish();
        return 0;
    }
}
