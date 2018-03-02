package ir.kasra_sh.MikroWebServer.IO.StaticFS;

import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import com.sun.org.apache.xerces.internal.impl.xpath.regex.RegularExpression;
import ir.kasra_sh.MikroWebServer.HTTPUtils.*;
import ir.kasra_sh.MikroWebServer.Utils.MimeTypes;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;

import java.io.FileInputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Suspendable
public class FileServerFiber2 implements Runnable {

    private Path file;
    private HTTPConnection con;
    private byte[] h;
    private int l;
    private RequestParser requestParser;
    private Map.Entry<String,String> rd;
    private Socket _sock;

    public FileServerFiber2(Map.Entry<String,String> rd, RequestParser rp, byte[] h, int len, Socket socket) {
        l = len;
        this.h = h;
        requestParser = rp;
        this.rd = rd;
        _sock = socket;
    }

    @Suspendable
    @Override
    public void run() {
        try {
            //double t = System.nanoTime();
            String context = rd.getKey().replace("*","");
            //System.out.println(new String(h,0,l));
            //System.out.println("From IP: "+_sock.getInetAddress());
            requestParser.parseHeaders(h,0,l);
            con = requestParser.getHTTPConnection();
            con.setSocket(_sock);
            con.writer = new ResponseWriter(_sock.getOutputStream());
            if (con == null){
                throw new Exception("null");
            }
            if (!(con.getMethod() == HTTPMethod.GET || con.getMethod() == HTTPMethod.HEAD)) {
                throw new Exception("Method type error !");
            }
            //con.writer.getHeader().setContentType();

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
                    // print file extension
                    //System.out.println(parts[parts.length-1]);
                    con.writer.getHeader().setContentType(MimeTypes.byExt(parts[parts.length-1]));
                    con.writer.getHeader().setProperty("Accept-Ranges","bytes");
                    long sz = Files.size(fp);
                    if (con.getMethod() == HTTPMethod.HEAD){
                        con.writer.getHeader().setContentLength((int)sz);
                        con.writer.writeHeader();
                        con.writer.finish();
                        return;
                    }
                    String range = con.getHeader("Range");
                    int[] rng=null;
                    if (range!=null){
                        if (range.matches("\\s*bytes=\\s*([\\d]+)\\-([\\d]+)\\s*")){
                            String[] r = range.replaceAll("\\s*bytes=","").split("-");
                            rng = new int[]{Integer.valueOf(r[0].trim()),Integer.valueOf(r[1].trim())};
                            System.out.println("Range : "+rng[0]+","+rng[1]);
                        } else if (range.matches("\\s*bytes=\\d+-\\s*")) {
                            String[] r = range.replaceAll("\\s*bytes=","").split("-");
                            rng = new int[]{Integer.valueOf(r[0].trim()),(int)sz-1};
                            System.out.println("Range : "+rng[0]+","+rng[1]);
                        }
                        else throw new Exception("Range error!");
                    }

                    int r=0;
                    //con.writer.getHeader().setContentLength((int)sz);
                    //con.writer.writeHeader();
                    byte[] buf;
                    int rsz=0;
                    if (rng != null) {
                        if (rng[0]>rng[1] || rng[1]>sz) throw new Exception("Range error !");
                        rsz = rng[1] - rng[0];
                        if (rsz == 0) rsz = 1;
                        con.writer.getHeader().setStatus(ResponseCode.PARTIAL_CONTENT);
                        con.writer.getHeader().setProperty("Content-Range","bytes "+rng[0]+"-"+rng[1]+"/"+sz);
                        con.writer.getHeader().setContentLength(rsz);
                        int rem = rsz;
                        if (rsz>204800) {
                            buf = new byte[204800];
                        } else buf = new byte[rsz+1];
                        fis.skip(rng[0]);
                        con.writer.writeHeader();
                        while (true){
                            if (rem<=buf.length) {
                                r = fis.read(buf, 0, rem);
                                rem-=r;
                            }
                            else {
                                r = fis.read(buf);
                                rem-=r;
                            }
                            if (r>0) {
                                con.writer.write(buf,0,r);
                            } else break;
                            if (r<buf.length) break;
                        }
                        fis.close();
                        con.writer.finish();
                        return;

                    } else {
                        con.writer.getHeader().setStatus(ResponseCode.OK);
                        con.writer.getHeader().setContentLength((int)sz);
                        if (sz>204800) {
                            buf = new byte[204800];
                        } else buf = new byte[(int)sz];
                        con.writer.writeHeader();
                        while (true){
                            r = fis.read(buf);
                            if (r>0) {
                                con.writer.write(buf,0,r);
                            } else break;
                            if (r<buf.length) break;
                        }
                        fis.close();
                        con.writer.finish();
                        return;
                    }

                    //ServerStats.addProc(System.nanoTime()-t);

                } else {
                    System.out.println("Not Found : "+fp);
                    con.writer.getHeader().setStatus(ResponseCode.NOT_FOUND);
                    con.writer.writeAll("Not Found !");
                    con.writer.finish();
                    System.out.println(new String(h,0,l));
                    System.out.println("From IP: "+_sock.getInetAddress());
                    // FIXME: 11/14/17
                    return;
                }
            } else {
                // FIXME: 11/14/17
                System.out.println("<<<< ERROR >>>>");
                System.out.println(new String(h,0,l));
                System.out.println("From IP: "+_sock.getInetAddress());
                con.writer.finish();
                //System.out.println();
                return;
            }
        } catch (Exception e){
            con.writer.finish();
            System.out.println(new String(h,0,l));
            System.out.println("From IP: "+_sock.getInetAddress());
            e.printStackTrace();
        }
        return;

    }
}
