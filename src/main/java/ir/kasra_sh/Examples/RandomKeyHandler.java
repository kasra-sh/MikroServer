package ir.kasra_sh.Examples;

import co.paralleluniverse.fibers.Suspendable;
import ir.kasra_sh.HTTPUtils.ResponseCode;
import ir.kasra_sh.HTTPUtils.ResponseString;
import ir.kasra_sh.MikroWebServer.IO.Handler;
import ir.kasra_sh.MikroWebServer.Utils.Validator;
import redis.clients.jedis.Jedis;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;


public class RandomKeyHandler extends Handler {

    public static String root;
    private Jedis jd;
    @Suspendable
    @Override
    public int handle() {
        //String username = conn.getOption("user");
        String resource = conn.getOption("res");
        //System.out.println(resource);
        if (resource != null) {
            Path path = Paths.get(root,resource);
            //System.out.println(path);
            if (Files.exists(path) && Validator.validateFilePath(path.toString())) {
                jd = new Jedis("localhost");
                //System.out.println(jd.get(username));
                String uuid = UUID.randomUUID().toString().replaceAll("-", "");
                jd.set(resource+"_"+uuid,"15");
                jd.expire(resource+"_"+uuid, 43200); //12 hours
                jd.close();
                sendResponse(ResponseCode.OK, uuid);
                return 0;
            }
        }

        sendResponse(ResponseCode.BAD_REQUEST, ResponseString.BAD_REQUEST);
        return 0;

    }
}
