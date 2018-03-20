package ir.kasra_sh.MikroServer.HTTPUtils;


import ir.kasra_sh.MikroServer.Server.Logger;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class RequestParser {
    private HTTPConnection connection = null;// = new HTTPConnection();
    private KSocket is;
    private byte[] h = new byte[1024];
    private int hl = 0;
    private StringBuilder mtd = new StringBuilder(7);
    private StringBuilder route = new StringBuilder(2048);
    private StringBuilder k = new StringBuilder(256);
    private StringBuilder v = new StringBuilder(1024);
    private StringBuilder ver = new StringBuilder(10);
    private String[] spl;
    private String respStatus;
    private static final byte[] endLine = new byte[]{'\r','\n'};
    private int errCode=0;
    private boolean rh = false;
    private String lastLine;

    public RequestParser(KSocket KSocket){
        this.is = KSocket;
    }

    public String getRoute() throws IOException {
        //route = is.readLineBytes();
        connection = new HTTPConnection();
        connection.setRequestParser(this);
        connection.setSocket(is);
        readLine();
        //System.out.println(new String(h,0,hl));
        parseHead();
        setMethod();
        spl = route.toString().split("\\?");
        connection.setRoute(URLDecoder.decode(spl[0], "UTF-8"));
        rh = true;
        if (spl.length>2) {
            //System.out.println("SPL.length > 2");
            errCode = ResponseCode.BAD_REQUEST;
        }
        return spl[0];
    }

    private void parseOptions() {
        if (spl.length>2) {
            //System.out.println("parseOptions spl.length>2");
            errCode = ResponseCode.BAD_REQUEST;
            return;
        }
        if (spl.length!=2) {
            //errCode = ResponseCode.BAD_REQUEST;
            return;
        }
        try {
            String op = URLDecoder.decode(spl[1],"UTF-8");
            String[] kv = op.split("&");
            if (kv.length<1) {
                //System.out.println("kv.length<1");
                errCode = ResponseCode.BAD_REQUEST;
                return;
            }
            for (String x:
                 kv) {
                String[] sp = x.split("=");
                if (sp.length<1) {
                    //System.out.println("split(=) sp.length<1");
                    errCode = ResponseCode.BAD_REQUEST;
                    return;
                }
                if (sp.length==2) {
                    connection.setOption(sp[0],sp[1]);
                }
                if (sp.length==1) {
                    connection.setOption(sp[0],"");
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            errCode = ResponseCode.BAD_REQUEST;
            return;
        }

    }

    public String getStatus() throws IOException {
        if (!rh) {
            //route = is.readLineBytes();
            connection = new HTTPConnection();
            connection.setRequestParser(this);
            connection.setSocket(is);
            readLine();
            respStatus = new String(h,0,hl);
            //parseHead();
            //setMethod();
            //spl = route.toString().split("\\?");
            //connection.setRoute(URLDecoder.decode(spl[0], "UTF-8"));
            rh = true;
        }
        //System.out.println(respStatus);
        return respStatus;
    }

    public void parseResponseHeader() throws IOException {
        getStatus();
        while (true) {
            readLine();
            //System.out.println(new String(h,0,hl));
            if (hl == 2) {
                if (h[0] == '\r' && h[1] == '\n') {
                    //System.out.println("found header end");
                    break;
                }
            }
            else if (hl>2) {
                parseKV();
            } else {
                //System.out.println("! hl>2");
                errCode = ResponseCode.BAD_REQUEST;
                return;
            }
        }

    }

    public void parseHeader() throws IOException {
        if (!rh) {
            getRoute();
        }
        parseOptions();
        //int l=0;
        while (true) {
            readLine();
            //System.out.println(new String(h,0,hl));
            if (hl == 2) {
                if (h[0] == '\r' && h[1] == '\n') {
                    //System.out.println("found header end");
                    break;
                }
            }
            else if (hl>2) {
                parseKV();
            } else {
                //System.out.println("! hl>2");
                errCode = ResponseCode.BAD_REQUEST;
                return;
            }
        }

    }

    private void readLine() throws IOException {
        hl = is.readLineBytes(h,h.length,endLine);
        for (int i = 0; i < hl; i++) {
            connection.getRawHeader().append((char)h[i]);
        }

    }

    private void parseHead() throws IndexOutOfBoundsException {
        int spc = 0;
        int cur = 0;
        while (cur<hl-2){
            if (h[cur]==' ') {
                spc++;
                cur++;
            }
            // Read Method
            if (spc == 0) {
                if (mtd.capacity()==mtd.length()) {
                    System.out.println("mtd.capacity = mtd.length");
                    errCode = ResponseCode.BAD_REQUEST;
                    return;
                    //throw new IndexOutOfBoundsException("Bad Request Method !");
                }
                mtd.append((char) h[cur]);
            }
            // Read Route
            else if (spc == 1) {
                if (route.capacity()==route.length()) {
                    System.out.println("route.capacity == route.length");
                    errCode = ResponseCode.URI_TOO_LONG;
                    return;
                    //throw new IndexOutOfBoundsException("Bad Request Route !");
                }
                route.append((char) h[cur]);
            } else if (spc == 2) {
                //version
                if (ver.capacity() == ver.length()) {
                    System.out.println("ver.capacity() == ver.length()");
                    errCode = ResponseCode.HTTP_VER_NOT_SUPPORTED;
                    return;
                }
                ver.append((char) h[cur]);
            }
            cur++;
            if (cur==hl-2) {
                if (!ver.toString().matches("HTTP\\/1\\.\\d")) {
                    //System.out.println(ver.toString());
                    //System.out.println("!ver.toString().equals(\"HTTP/1.1\")");
                    errCode = ResponseCode.HTTP_VER_NOT_SUPPORTED;
                }
            }
        }

    }

    private void setMethod(){
        String m = mtd.toString().toUpperCase();
        switch (m) {
            case "POST":
                connection.setMethod(HTTPMethod.POST);
                break;
            case "GET":
                connection.setMethod(HTTPMethod.GET);
                break;
            case "HEAD":
                connection.setMethod(HTTPMethod.HEAD);
                break;
            case "OPTIONS":
                connection.setMethod(HTTPMethod.OPTIONS);
                break;
        }
    }

    private void parseKV(){
        boolean colon=false;
        int cur = 0;
        k.setLength(0);
        v.setLength(0);
        while (cur<hl-2){
            if (h[cur]==':' && !colon){
                colon = true;
                cur++;
                cur++;
                continue;
            }
            if (colon){
                v.append((char) h[cur]);
                cur++;
            } else {
                k.append((char) h[cur]);
                cur++;
            }
        }
        //System.out.println(k.toString()+": "+v.toString());
        connection.setHeader(k.toString(), v.toString());
    }

    public void getBody() throws IOException{
        int ln=-1;
        try {
            ln = Integer.valueOf(connection.getHeader("Content-Length"));
        } catch (Exception w){
            if (Logger.DEBUG) w.printStackTrace();
        }

        if (connection.getMethod() != HTTPMethod.POST || ln<=0) {
            return;
        }

        if (connection.body!=null) return;
        connection.setBodySize(ln);
        connection.body = new byte[ln];

        int l=0;
        int start = 0;
        while (start<ln){
            l = is.readBytes(h,0,h.length);
            if (l<0) break;
            try {
                System.arraycopy(h,0,connection.body,start,l);
            } catch (Exception e){
                e.printStackTrace();
            }
            start+=l;
        }
    }

    public void getMultiPartBody(){
        if (connection.getMethod()!= HTTPMethod.POST) {
            //System.out.println("not post");
            return;
        }

        if (connection.multipart != null) {
            return;
        }
        //System.out.println(connection.getHeaders().stringPropertyNames());
        String ct = connection.getHeader("Content-Type");
        if (!ct.startsWith("multipart")) {
            return;
        }
        byte[] stream = connection.getBodyBytes();
        //System.out.println(connection.getBody());
        try {
            String bound = "--"+ct.substring(ct.indexOf("=")+1);
            int prev=0;
            int next=0;
            connection.multipart = new HashMap<>();
            while (stream.length-(prev = seekNext(stream,prev,bound.getBytes()))>4) {
                //System.out.println("PREV = "+prev);
                prev = seekNext(stream, prev+2, "nt-Dispo".getBytes());
                prev = seekNext(stream, prev, "name=\"".getBytes());
                next = seekNext(stream, prev, "\"".getBytes());
                String key = new String(stream, prev, next - prev - 1);
                prev = next;
                prev = seekNext(stream, prev, "\r\n\r\n".getBytes());
                next = seekNext(stream, prev, bound.getBytes());
                byte[] b = new byte[next - prev - bound.length()-2];
                System.arraycopy(stream,prev,b,0,b.length);
                //System.out.println(new String(b).replace("\r\n","RN"));
                connection.multipart.putIfAbsent(key,b);
            }
            //System.out.println(next);
            //System.out.println(seekNext(stream,next+1,bound.getBytes()));


        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private int seekNext(byte[] stream, int init, byte[] ... endSeq) throws IOException, IndexOutOfBoundsException {
        //setLineBufferSize(limit);
        int index=init;
        boolean match = false;
        while (true) {

            for (int j = 0; j < endSeq.length; j++) {
                //System.out.println();
                if (index+endSeq.length>=stream.length) {
                    return -1;
                }
                if (index>=endSeq[j].length-1) {
                    match = true;
                    for (int i = 0; i < endSeq[j].length; i++) {
                        //System.out.println("Seq"+j+" checking "+i+" : "+(char)lineBuffer[(index - endSeq[j].length)+i+1]);
                        if (stream[(index - endSeq[j].length)+i+1] == endSeq[j][i]) {
                            continue;
                        } else {
                            match = false;
                            break;
                        }
                    }
                } else match = false;

                if (match) {
                    break;
                }
            }

            if (match) {
                //lastLine = new String(h, 0, index+1);
                //System.arraycopy(h, 0, b, 0, index+1);
                //System.out.println("Index = "+index);
                //System.out.println(lineBuffer[index]);
                return index+1;//-endSeq.length+1;
            }
            index++;
        }
    }

    public HTTPConnection getHTTPConnection(){
        return this.connection;
    }

    public int getErrCode(){
        return errCode;
    }
}
