package ir.kasra_sh.Handlers;

import co.paralleluniverse.fibers.Suspendable;
import ir.kasra_sh.MikroWebServer.HTTPUtils.*;
import ir.kasra_sh.MikroWebServer.IO.HandlerEx;

public class FileServerHandler extends HandlerEx {

    @Suspendable
    @Override
    public int handle() {
        try {
            conn.socketIO().getSocket().shutdownInput();
            String context = getContext();
            //System.out.println("File Server Context : "+context);

            if (!(conn.getMethod() == HTTPMethod.GET)) {
                sendResponse(ResponseCode.METHOD_NOT_ALLOWED, ResponseString.METHOD_NOT_ALLOWED);
                return 0;
            }

            //System.out.println("Sending");
            serveFile(context, "*");
            //conn.writer.getHeader().setContentType();
        }catch (Exception e) {
            //e.printStackTrace();
        }
        return 0;
    }

}
