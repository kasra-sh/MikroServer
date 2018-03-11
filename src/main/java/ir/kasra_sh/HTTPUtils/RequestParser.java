package ir.kasra_sh.HTTPUtils;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

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
    private static final byte[] endLine = new byte[]{'\r','\n'};
    private int errCode=0;
    private boolean rh = false;

    public RequestParser(KSocket KSocket){
        this.is = KSocket;
    }

    public String getRoute() throws IOException {
        //route = is.readLineBytes();
        connection = new HTTPConnection();
        connection.setRequestParser(this);
        connection.setSocket(is);
        readLine();
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
            ln = Integer.valueOf(connection.getHeaders().getProperty("Content-Length"));
        } catch (Exception w){ }

        if (connection.getMethod() != HTTPMethod.POST || ln<=0) {
            return;
        }
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

    public HTTPConnection getHTTPConnection(){
        return this.connection;
    }

    public int getErrCode(){
        return errCode;
    }
}
