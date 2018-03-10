package ir.kasra_sh.Examples;

import ir.kasra_sh.HTTPUtils.ResponseCode;
import ir.kasra_sh.MikroWebServer.IO.Handler;

import java.io.IOException;


public class GsonTestHandler extends Handler {
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
