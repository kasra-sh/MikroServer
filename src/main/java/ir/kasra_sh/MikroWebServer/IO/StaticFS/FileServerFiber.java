package ir.kasra_sh.MikroWebServer.IO.StaticFS;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import ir.kasra_sh.MikroWebServer.HTTPUtils.HTTPConnection;
import ir.kasra_sh.MikroWebServer.HTTPUtils.RequestParser;
import ir.kasra_sh.MikroWebServer.HTTPUtils.ResponseCode;
import ir.kasra_sh.MikroWebServer.HTTPUtils.ResponseWriter;
import ir.kasra_sh.MikroWebServer.Utils.MimeTypes;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Suspendable
public class FileServerFiber extends Fiber<Void> {

    private Path file;
    private HTTPConnection con;
    private byte[] h;
    private int l;
    private RequestParser requestParser;
    private Map.Entry<String,String> rd;
    private Socket _sock;

    public FileServerFiber(Map.Entry<String,String> rd, RequestParser rp, byte[] h, int len, Socket socket) {
        l = len;
        this.h = h;
        requestParser = rp;
        this.rd = rd;
        _sock = socket;
    }

    @Suspendable
    @Override
    protected Void run() throws SuspendExecution, InterruptedException {
        try {
            //double t = System.nanoTime();
            String context = rd.getKey().replace("*","");
            //System.out.println(new String(h,0,l));
            requestParser.parseHeaders(h,0,l);
            con = requestParser.getHTTPConnection();
            con.setSocket(_sock);
            con.writer = new ResponseWriter(_sock.getOutputStream());
            if (con == null){
                throw new Exception("null");
            }
            con.writer.getHeader().setStatus(ResponseCode.OK);
            con.writer.getHeader().setContentType(MimeTypes.Text.HTML);
            String[] prts = con.getRoute().split("/");
            int c = context.split("/").length;
            StringBuilder npath = new StringBuilder(255);
            for (int i = c; i < prts.length; i++) {
                npath.append("/").append(prts[i]);
            }
            //System.out.println(npath);
            Path fp = Paths.get(rd.getValue(), npath.toString());
            String extPath = fp.toAbsolutePath().toString();
            //System.out.println("extPath : "+extPath);
            if (extPath.length()>1 && (!fp.toFile().isDirectory())){
                String[] parts = npath.toString().split("\\.");
                con.writer.getHeader().setContentType(MimeTypes.byExt(parts[parts.length-1]));
                if (Files.exists(fp) && (!(npath.toString().contains("../")||(npath.toString().contains("/.."))))) {
                    FileInputStream fis = new FileInputStream(fp.toFile());
                    // FIXME: 11/14/17
                    //System.out.println("Serving : "+fp);
                    long sz = Files.size(fp);
                    int r=0;
                    con.writer.getHeader().setContentLength((int)sz);
                    con.writer.writeHeader();
                    byte[] buf;
                    if (sz>81920) {
                        buf = new byte[81920];
                    } else buf = new byte[(int)sz];
                    //ServerStats.addProc(System.nanoTime()-t);
                    while (true){
                        r = fis.read(buf);
                        if (r>0) {
                            con.writer.write(buf,0,r);
                        } else break;
                    }
                    con.writer.finish();
                    return null;
                } else {
                    System.out.println("Not Found : "+fp);
                    con.writer.getHeader().setStatus(ResponseCode.NOT_FOUND);
                    con.writer.writeAll("Not Found !");
                    con.writer.finish();
                    // FIXME: 11/14/17
                    return null;
                }
            } else {
                // FIXME: 11/14/17
                System.out.println("Err !");
                con.writer.finish();
                return null;
            }
        } catch (Exception e){
            System.out.println("Err !");
            con.writer.finish();
            e.printStackTrace();
        }
        return null;

    }
}
