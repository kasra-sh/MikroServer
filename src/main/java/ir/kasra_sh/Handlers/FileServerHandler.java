package ir.kasra_sh.Handlers;

import ir.kasra_sh.MikroWebServer.HTTPUtils.HTTPConnection;
import ir.kasra_sh.MikroWebServer.HTTPUtils.ResponseCode;
import ir.kasra_sh.MikroWebServer.IO.FiberStreamWriter;
import ir.kasra_sh.MikroWebServer.IO.Handler;
import ir.kasra_sh.MikroWebServer.Utils.MimeTypes;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileServerHandler extends Handler {
    private static String root;
    private static File dir;

    public FileServerHandler(){

    }
    public FileServerHandler(String path){
        root = path;
        dir = new File(root);
    }
    //@Suspendable
    @Override
    public int handle(HTTPConnection conn) {
        try {
            conn.writer.getHeader().setStatus(ResponseCode.OK);
            conn.writer.getHeader().setContentType(MimeTypes.Text.HTML);
            String[] prts = conn.getRoute().split("/");
            int c = getContext().split("/").length;
            StringBuilder npath = new StringBuilder(255);
            for (int i = c; i < prts.length; i++) {
                npath.append("/").append(prts[i]);
            }
            //System.out.println(npath);
            Path fp = Paths.get(root,npath.toString());
            String extPath = fp.toAbsolutePath().toString();
            //System.out.println("extPath : "+extPath);
            if (extPath.length()>1 && (!fp.toFile().isDirectory())){
                String[] parts = npath.toString().split("\\.");
                conn.writer.getHeader().setContentType(MimeTypes.byExt(parts[parts.length-1]));
                if (Files.exists(fp) && (!(npath.toString().contains("../")||(npath.toString().contains("/.."))))) {
                    FileInputStream fis = new FileInputStream(fp.toFile());
                    // FIXME: 11/14/17
                    //System.out.println("Serving : "+fp);
                    long sz = Files.size(fp);
                    long r=0;
                    conn.writer.getHeader().setContentLength((int)sz);
                    conn.writer.writeHeader();
                    new FiberStreamWriter(fis,conn).start();
                    return 0;
                } else {
                    /*conn.writer.getHeader().setStatus(ResponseCode.NOT_FOUND);
                    conn.writer.writeAll("Not Found !");
                    conn.writer.finish();*/
                    return ResponseCode.NOT_FOUND;
                }
            } else {
                return ResponseCode.FORBIDDEN;
            }

        }catch (Exception e){
            e.printStackTrace();
            conn.writer.finish();
            return 0;
        }
    }

}
