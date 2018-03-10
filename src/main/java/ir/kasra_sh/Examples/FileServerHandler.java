package ir.kasra_sh.Examples;

import co.paralleluniverse.fibers.Suspendable;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import ir.kasra_sh.FileCache.DeflateFileCache;
import ir.kasra_sh.HTTPUtils.*;
import ir.kasra_sh.MikroWebServer.IO.Handler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileServerHandler extends Handler {

    private long MAX_CACHED_FILE_SIZE = 5*1024*1024; // MAX = 5MB

    private static DeflateFileCache cache = new DeflateFileCache(200);
    @Suspendable
    @Override
    public int handle() {
        try {
            conn.socketIO().getSocket().shutdownInput();
            //String context = getContext();
            //System.out.println("File Server Context : "+context);

            if (!(conn.getMethod() == HTTPMethod.GET)) {
                sendResponse(ResponseCode.METHOD_NOT_ALLOWED, ResponseString.METHOD_NOT_ALLOWED);
                return 0;
            }

            //System.out.println("Sending");
            Path fp = Paths.get(conn.getFilePath(), req.getRouteExtra());
            if (!Files.exists(fp)){
                sendResponse(ResponseCode.NOT_FOUND,ResponseString.NOT_FOUND);
                return 0;
            }
            if (req.getArg("cached").equals("false") || Files.size(fp)>MAX_CACHED_FILE_SIZE) {
                serveFile(getContext(), "*");
                return 0;
            }
            String addr = fp.toString();
            byte[] b = cache.getFileBytes(addr);
            int size = ((int) cache.getFileObject(addr).getSize());
            InputStream in = new ByteInputStream(b, size);
            //System.out.println("FileName : "+fp.getFileName().toString());
            //serveFile(context, "*");
            sendFileFromStream(in, fp.getFileName().toString(), cache.getFileObject(addr).getSize(), true);

            //conn.writer.getHeader().setContentType();
        }catch (Exception e) {
            //e.printStackTrace();
        }
        return 0;
    }

}
