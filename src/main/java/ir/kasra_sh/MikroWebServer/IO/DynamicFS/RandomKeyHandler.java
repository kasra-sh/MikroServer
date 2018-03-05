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

    public static String root = "/home/blkr/Music/hls/";
    static JedisPool jd = new JedisPool("localhost");
    @Suspendable
    @Override
    public int handle(HTTPConnection conn) {
        //String username = conn.getOption("user");
        String resource = conn.getOption("res");
        //System.out.println(resource);
        if (resource != null) {
            if (Files.exists(Paths.get(root,resource))) {
                Jedis j = jd.getResource();
                //System.out.println(jd.get(username));
                String uuid = UUID.randomUUID().toString().replaceAll("-", "");
                j.set(resource+"_"+uuid,"15");
                j.expire(resource+"_"+uuid, 3600);
                j.close();
                conn.writer.getHeader().setStatus(ResponseCode.OK);
                conn.writer.writeAll(uuid);
                //System.out.println("RRRRRRRRRRRRRR");
                return 0;
            }

        }
        conn.writer.getHeader().setStatus(ResponseCode.BAD_REQUEST);
        conn.writer.writeAll("Bad Request!");
        return 0;

    }
}
