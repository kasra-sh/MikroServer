package ir.kasra_sh.Handlers;

import ir.kasra_sh.MikroWebServer.HTTPUtils.ResponseCode;
import ir.kasra_sh.MikroWebServer.IO.HandlerEx;

import java.io.IOException;


public class GsonTestHandler extends HandlerEx{
    @Override
    public int handle() {
        try {
            byte[] body = conn.getBody();
            conn.writer.writeResponse(ResponseCode.OK, body, 0, body.length);
            conn.writer.finish();
            return 0;
        } catch (Exception e){
            try {
                conn.writer.writeResponse(ResponseCode.BAD_REQUEST, "Bad Request");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            conn.writer.finish();
            return -1;
        }
    }
}
