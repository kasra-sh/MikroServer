package ir.kasra_sh.MikroWebServer.IO;

import co.paralleluniverse.fibers.Suspendable;
import ir.kasra_sh.MikroWebServer.HTTPUtils.*;
import ir.kasra_sh.MikroWebServer.Utils.MimeTypes;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Suspendable
public abstract class HandlerEx {
    protected String CONTEXT;
    protected HTTPConnectionEx conn;
    protected ResponseWriterEx res;
    protected Request req;

    public void setConnection(HTTPConnectionEx conn){
        this.conn = conn;
        this.conn.setContext(CONTEXT);
        this.res = conn.writer;
        this.req = new Request(conn);
    }

    public void setContext(String c){
        CONTEXT = c;
    }

    public String getContext() {
        return CONTEXT;
    }

    @Suspendable
    public abstract int handle();

    @Suspendable
    protected int serveFile(String context, String ... ext) throws Exception {
        String[] prts = conn.getRoute().split("/");
        int c = context.split("/").length;
        StringBuilder npath = new StringBuilder(255);
        for (int i = c; i < prts.length; i++) {
            npath.append("/").append(prts[i]);
        }
        //System.out.println(npath);
        Path fp = Paths.get(conn.getFilePath(), npath.toString());
        String extPath = fp.toAbsolutePath().toString();
        //System.out.println("extPath : "+extPath);
        if (extPath.length()>1 && (!fp.toFile().isDirectory())){
            String[] parts = npath.toString().split("\\.");

            String extention = parts[parts.length-1];
            boolean ex = false;
            if (ext.length==1) {
                if (ext[0].equals("*")) {
                    ex = true;
                }
            }else {
                for (String e:
                        ext) {
                    if (extention.equalsIgnoreCase(e)) {
                        ex = true;
                        break;
                    }
                }
            }


            if (!ex) {
                conn.writer.writeResponse(ResponseCode.NOT_ACCEPTABLE, "Mime not supported !");
                return ResponseCode.NOT_ACCEPTABLE;
            }

            conn.writer.getHeader().setContentType(MimeTypes.byExt(extention));
            if (Files.exists(fp) && (!(npath.toString().contains("../")||(npath.toString().contains("/.."))))) {

                conn.writer.getHeader().setStatus(ResponseCode.OK);

                FileInputStream fis = new FileInputStream(fp.toFile());
                // FIXME: 11/14/17
                //System.out.println("Serving : "+fp);
                // print file extension
                //System.out.println(parts[parts.length-1]);
                conn.writer.getHeader().setContentType(MimeTypes.byExt(parts[parts.length-1]));
                conn.writer.getHeader().setProperty("Accept-Ranges","bytes");
                long sz = Files.size(fp);
                if (conn.getMethod() == HTTPMethod.HEAD){
                    conn.writer.getHeader().setContentLength((int)sz);
                    conn.writer.writeHeader();
                    conn.writer.finish();
                    return 0;
                }
                String range = conn.getHeader("Range");
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
                //conn.writer.getHeader().setContentLength((int)sz);
                //conn.writer.writeHeader();
                byte[] buf;
                int rsz=0;
                if (rng != null) {
                    if (rng[0]>rng[1] || rng[1]>sz) throw new Exception("Range error !");
                    rsz = rng[1] - rng[0];
                    if (rsz == 0) rsz = 1;
                    conn.writer.getHeader().setStatus(ResponseCode.PARTIAL_CONTENT);
                    conn.writer.getHeader().setProperty("Content-Range","bytes "+rng[0]+"-"+rng[1]+"/"+sz);
                    conn.writer.getHeader().setContentLength(rsz);
                    int rem = rsz;
                    if (rsz>204800) {
                        buf = new byte[204800];
                    } else buf = new byte[rsz+1];
                    fis.skip(rng[0]);
                    conn.writer.writeHeader();
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
                            conn.writer.write(buf,0,r);
                        } else break;
                        if (r<buf.length) break;
                    }
                    fis.close();
                    conn.writer.finish();
                    return 0;

                } else {
                    conn.writer.getHeader().setStatus(ResponseCode.OK);
                    conn.writer.getHeader().setContentLength((int)sz);
                    if (sz>204800) {
                        buf = new byte[204800];
                    } else buf = new byte[(int)sz];
                    conn.writer.writeHeader();
                    while (true){
                        r = fis.read(buf);
                        if (r>0) {
                            conn.writer.write(buf,0,r);
                        } else break;
                        if (r<buf.length) break;
                    }
                    fis.close();
                    conn.writer.finish();
                    conn.socketIO().close();
                    return 0;
                }

                //ServerStats.addProc(System.nanoTime()-t);

            } else {
                System.out.println("Not Found : "+fp);
                conn.writer.getHeader().setStatus(ResponseCode.NOT_FOUND);
                conn.writer.writeAll("Not Found !");
                conn.writer.finish();
                //System.out.println(new String(h,0,l));
                System.out.println("From IP: "+ conn.socketIO().getSocket().getInetAddress());
                // FIXME: 11/14/17
                return ResponseCode.NOT_FOUND;
            }
        } else {
            // FIXME: 11/14/17
            System.out.println("No file address or Directory");
            //System.out.println(new String(h,0,l));
            System.out.println("From IP: "+ conn.socketIO().getSocket().getInetAddress());
            conn.writer.finish();
            //System.out.println();
            return ResponseCode.NOT_FOUND;
        }
    }

    protected void sendResponse(int resposeCode, String text) {
        try {
            conn.writer.writeResponse(resposeCode, text);
        } catch (IOException e) { }
    }

}
