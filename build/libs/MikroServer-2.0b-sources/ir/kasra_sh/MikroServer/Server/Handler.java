package ir.kasra_sh.MikroServer.Server;

import co.paralleluniverse.fibers.Suspendable;
import ir.kasra_sh.MikroServer.HTTPUtils.*;
import ir.kasra_sh.MikroServer.Utils.MimeTypes;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Suspendable
public abstract class Handler {
    protected String CONTEXT;
    protected HTTPConnection conn;
    protected ResponseWriter res;
    protected Request req;

    public void setConnection(HTTPConnection conn){
        this.conn = conn;
        this.conn.setContext(CONTEXT);
        this.res = conn.writer;
        this.req = conn.req;
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
                res.writeResponse(ResponseCode.NOT_ACCEPTABLE, "Mime not supported !");
                return ResponseCode.NOT_ACCEPTABLE;
            }

            res.header.setContentType(MimeTypes.byExt(extention));
            if (Files.exists(fp) && (!(npath.toString().contains("../")||(npath.toString().contains("/.."))))) {

                res.header.setStatus(ResponseCode.OK);

                FileInputStream fis = new FileInputStream(fp.toFile());
                // FIXME: 11/14/17
                //System.out.println("Serving : "+fp);
                // print file extension
                //System.out.println(parts[parts.length-1]);
                res.header.setContentType(MimeTypes.byExt(parts[parts.length-1]));
                res.header.setProperty("Accept-Ranges","bytes");
                long sz = Files.size(fp);
                if (conn.getMethod() == HTTPMethod.HEAD){
                    res.header.setContentLength((int)sz);
                    res.writeHeader();
                    res.finish();
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
                //res.header.setContentLength((int)sz);
                //res.writeHeader();
                byte[] buf;
                int rsz=0;
                if (rng != null) {
                    if (rng[0]>rng[1] || rng[1]>sz) throw new Exception("Range error !");
                    rsz = rng[1] - rng[0];
                    if (rsz == 0) rsz = 1;
                    res.header.setStatus(ResponseCode.PARTIAL_CONTENT);
                    res.header.setProperty("Content-Range","bytes "+rng[0]+"-"+rng[1]+"/"+sz);
                    res.header.setContentLength(rsz);
                    int rem = rsz;
                    if (rsz>204800) {
                        buf = new byte[204800];
                    } else buf = new byte[rsz+1];
                    fis.skip(rng[0]);
                    res.writeHeader();
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
                            res.write(buf,0,r);
                        } else break;
                        if (r<buf.length) break;
                    }
                    fis.close();
                    res.finish();
                    return 0;

                } else {
                    res.header.setStatus(ResponseCode.OK);
                    res.header.setContentLength((int)sz);
                    if (sz>204800) {
                        buf = new byte[204800];
                    } else buf = new byte[(int)sz];
                    res.writeHeader();
                    while (true){
                        r = fis.read(buf);
                        if (r>0) {
                            res.write(buf,0,r);
                        } else break;
                        if (r<buf.length) break;
                    }
                    fis.close();
                    res.finish();
                    conn.socketIO().close();
                    return 0;
                }

                //ServerStats.addProc(System.nanoTime()-t);

            } else {
                System.out.println("Not Found : "+fp);
                res.header.setStatus(ResponseCode.NOT_FOUND);
                res.writeAll("Not Found !");
                res.finish();
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
            res.finish();
            //System.out.println();
            return ResponseCode.NOT_FOUND;
        }
    }

    @Suspendable
    protected int sendFileFromStream(InputStream in, String name, long size, boolean deflate) throws Exception {
        //System.out.println("sending");
        String ext = name.substring(name.lastIndexOf(".")+1);
        //System.out.println(ext);

        res.header.setContentType(MimeTypes.byExt(ext));
        res.header.setStatus(ResponseCode.OK);
        res.header.setProperty("Accept-Ranges","bytes");
        long sz = size;
        if (conn.getMethod() == HTTPMethod.HEAD){
            res.header.setContentLength((int)sz);
            res.writeHeader();
            res.finish();
            return 0;
        }

        if (deflate) {
            res.header.setProperty("Content-Encoding","deflate");
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
        //res.getHeader().setContentLength((int)sz);
        //res.writeHeader();
        byte[] buf;
        int rsz=0;
        if (rng != null) {
            if (rng[0]>rng[1] || rng[1]>sz) throw new Exception("Range error !");
            rsz = rng[1] - rng[0];
            if (rsz == 0) rsz = 1;
            res.header.setStatus(ResponseCode.PARTIAL_CONTENT);
            res.header.setProperty("Content-Range","bytes "+rng[0]+"-"+rng[1]+"/"+sz);
            res.header.setContentLength(rsz);
            int rem = rsz;
            if (rsz>204800) {
                buf = new byte[204800];
            } else buf = new byte[rsz+1];
            in.skip(rng[0]);
            res.writeHeader();
            while (true){
                if (rem<=buf.length) {
                    r = in.read(buf, 0, rem);
                    rem-=r;
                }
                else {
                    r = in.read(buf);
                    rem-=r;
                }
                if (r>0) {
                    res.write(buf,0,r);
                } else break;
                if (r<buf.length) break;
            }
            in.close();
            res.finish();
            return 0;

        } else {
            res.header.setStatus(ResponseCode.OK);
            res.header.setContentLength((int)sz);
            if (sz>204800) {
                buf = new byte[204800];
            } else buf = new byte[(int)sz];
            res.writeHeader();
            while (true){
                r = in.read(buf);
                if (r>0) {
                    res.write(buf,0,r);
                } else break;
                if (r<buf.length) break;
            }
            in.close();
            res.finish();
            conn.socketIO().close();
            return 0;
        }

                //ServerStats.addProc(System.nanoTime()-t);
    }

    protected void sendResponse(int resposeCode, String text) {
        try {
            res.writeResponse(resposeCode, text);
        } catch (IOException e) { }
    }

}
