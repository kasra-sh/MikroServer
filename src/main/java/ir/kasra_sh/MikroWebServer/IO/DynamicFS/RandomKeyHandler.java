package ir.kasra_sh.MikroWebServer.IO.DynamicFS;

import co.paralleluniverse.fibers.Suspendable;
import ir.kasra_sh.MikroWebServer.HTTPUtils.HTTPConnection;
import ir.kasra_sh.MikroWebServer.HTTPUtils.ResponseCode;
import ir.kasra_sh.MikroWebServer.IO.Handler;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;


public class RandomKeyHandler extends Handler {

    static String root = "/home/blkr/Music/hls/";
    @Suspendable
    @Override
    public int handle(HTTPConnection conn) {
        //String username = conn.getOption("user");
        String resource = conn.getOption("res");
        if (resource != null) {
            if (Files.exists(Paths.get(root,resource))) {
                Jedis jd = new Jedis("localhost");

                //System.out.println(jd.get(username));
                String uuid = UUID.randomUUID().toString().replaceAll("-", "");
                jd.set(resource+"_"+uuid,"15");
                jd.expire(resource+"_"+uuid, 3600);
                jd.close();
                conn.writer.getHeader().setStatus(ResponseCode.OK);
                conn.writer.writeAll(uuid);
                return 0;
            }

        }
        conn.writer.getHeader().setStatus(ResponseCode.BAD_REQUEST);
        conn.writer.writeAll("Bad Request!");
        return 0;

    }
}
