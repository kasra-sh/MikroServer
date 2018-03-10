package ir.kasra_sh.Examples;

import co.paralleluniverse.fibers.Suspendable;
import ir.kasra_sh.HTTPUtils.HTTPMethod;
import ir.kasra_sh.HTTPUtils.ResponseCode;
import ir.kasra_sh.MikroWebServer.IO.Handler;
import ir.kasra_sh.MikroWebServer.Utils.MimeTypes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;

@Suspendable
public class UploadHandler extends Handler {

    private static HashSet<String> mimes = new HashSet<>();
    private static String root = "/home/blkr/www/uploader/userfiles/";

    static {
        mimes.add(MimeTypes.Image.PNG);
        mimes.add(MimeTypes.Image.JPEG);
        mimes.add(MimeTypes.Video.MP4);
        mimes.add(MimeTypes.Video.MPEG);
        mimes.add(MimeTypes.Text.TXT);
        mimes.add(MimeTypes.Application.BIN);
    }

    @Suspendable
    @Override
    public int handle() {
        String key = conn.getOption("key");
        if (key == null) {
            key="";
        }
        if (conn.getMethod() == HTTPMethod.POST && key.equals("123456")) {
            int fsize = conn.getBodySize();
            if (fsize > 0) {
                String ct = conn.getHeader("Content-Type");
                if (mimes.contains(ct)) {
                    conn.getBody();
                    String username = conn.getHeader("x-username");
                    String ext = "";
                    if (ct.equals(MimeTypes.Text.TXT)) ext = "txt";
                    else if (ct.equals(MimeTypes.Application.BIN)) ext = "bin";
                    else ext = ct.split("/")[1];

                    String fileaddr = root+username+System.nanoTime() + "." +ext;

                    try {
                        Files.write(Paths.get(fileaddr), conn.body);
                        System.out.println("Uploaded "+fileaddr);
                    } catch (IOException e) { e.printStackTrace();}
                    conn.writer.getHeader().setStatus(ResponseCode.OK);
                    conn.writer.writeHeader();
                    conn.writer.finish();
                    return 0;
                }
            }
        }
        conn.writer.getHeader().setStatus(ResponseCode.UNAUTHORIZED);
        conn.writer.writeHeader();
        conn.writer.finish();
        return 0;
    }
}
