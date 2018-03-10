package ir.kasra_sh.Examples;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ir.kasra_sh.Examples.JsonObjects.Result;
import ir.kasra_sh.Examples.JsonObjects.User;
import ir.kasra_sh.HTTPUtils.ResponseCode;
import ir.kasra_sh.MikroWebServer.IO.Handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;


public class GsonTestHandler extends Handler {
    @Override
    public int handle() {
        try {
            byte[] body = conn.getBody();
            String bdy = new String(body);
            System.out.println(bdy);
            Gson gson = new GsonBuilder().create();
            User usr = gson.fromJson(new StringReader(bdy), User.class);
            System.out.println(usr.username+" : "+usr.password);
            res.writeResponse(ResponseCode.OK ,gson.toJson(new Result(200,"hi")));
            return 0;
        } catch (Exception e){
            e.printStackTrace();
            try {
                res.writeResponse(ResponseCode.BAD_REQUEST, "Bad Request");
                return 0;
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            conn.writer.finish();
            return -1;
        }
    }
}
