package ir.kasra_sh.MikroWebServer.HTTPUtils;

import ir.kasra_sh.MikroWebServer.HTTPUtils.HTTPMethod;
import ir.kasra_sh.MikroWebServer.HTTPUtils.HTTPConnection;
import ir.kasra_sh.MikroWebServer.HTTPUtils.ResponseCode;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class RequestParser {
    private HTTPConnection properties = null;// = new HTTPConnection();
    private byte[] hdr;
    private int index;
    private int len;
    private StringBuffer nsb = new StringBuffer(255);
    private StringBuffer vsb = new StringBuffer(255);
    private StringBuffer mtd = new StringBuffer(8);
    private StringBuffer route = new StringBuffer(255);
    private int cur;
    private int errCode = 0;
    private InputStream is;
    private int hlen;
    private byte[] buff = new byte[8192];

    public RequestParser(InputStream is){
        this.is = is;
    }

    public String extractRoute(byte[] header, int i, int len) throws IndexOutOfBoundsException {
        //System.out.println("<<< HEADER >>>"+new String(header,i,len));
        nsb.setLength(0);
        vsb.setLength(0);
        mtd.setLength(0);
        route.setLength(0);
        properties = new HTTPConnection();
        properties.setRequestParser(this);
        hdr = header;
        index = i;
        cur = index;
        this.len = len;
        parseHead();
        setMethod();
        String[] spl = route.toString().split("\\?");
        return spl[0];
    }

    public int parseHeaders(byte[] header, int i, int len) throws IndexOutOfBoundsException {
        nsb.setLength(0);
        vsb.setLength(0);
        mtd.setLength(0);
        route.setLength(0);
        if (properties == null)
            properties = new HTTPConnection();
        hdr = header;
        index = i;
        cur = index;
        this.len = len;
        int end = getHeaderEnd();
        properties.setHeaderSize(end+5);
        hlen = end+5;
        //System.out.println("Header size : "+(end+5));
        //System.out.println(new String(header,0,len));
        if (end<0){
            return -1;
        }
        parseHead();
        setMethod();
        String[] spl = route.toString().split("\\?");
        String[] opts;
        properties.setRoute(spl[0]);
        if (spl.length == 2){
            opts = spl[1].split("&");
            StringBuilder k,v;
            k = new StringBuilder();
            v = new StringBuilder();
            boolean e;
            for (String kv:
                    opts){
                e = false;
                k.setLength(0);
                v.setLength(0);
                for (int j = 0; j < kv.length(); j++) {
                    if (kv.charAt(j) == '='){
                        e = true;
                        continue;
                    }
                    if (e){
                        v.append(kv.charAt(j));
                    } else
                        k.append(kv.charAt(j));
                }
                properties.setOption(k.toString(),v.toString());
            }
        }

        while (!isEOH(cur)){
            if (isEOL(cur)) cur+=2;
            parseKV();
        }
        int ln=0;
        try {
            ln = Integer.valueOf(properties.getHeaders().getProperty("Content-Length"));
        }catch (Exception w){ }
        properties.setBodySize(ln);
        return cur+3;

    }

    public void getBody() throws IOException {
        int ln=-1;
        try {
            ln = Integer.valueOf(properties.getHeaders().getProperty("Content-Length"));
        }catch (Exception w){ }
        System.out.println(properties.getHeader("Content-Length"));
        //System.out.println("ln : "+ln);
        //System.out.println("len read : "+len);
        if (properties.getMethod() != HTTPMethod.POST || ln<=0) {
            return;
        }
        //System.out.println("has "+(ln-(len-hlen+1)));
        properties.setBodySize(ln);
        properties.body = new byte[ln];
        //System.out.println("At hlen : ("+(char)(hdr[hlen])+")("+(char)(hdr[hlen+1])+")");
        //System.out.println("Reading from "+(hlen-1)+" to "+len);
        System.arraycopy(hdr, hlen-1, properties.body, 0, len - hlen + 1);

        int l=0;
        int start = len-hlen+1;
        while (start!=ln){
            l = is.read(buff,0,buff.length);
            if (l<0) break;
            //System.out.println("l = "+l);
            try {
                System.arraycopy(buff,0,properties.body,start,l);
            } catch (Exception e){
                e.printStackTrace();
            }
            start+=l;
            //System.out.println("Start is "+start);
        }

        if (l<len - (hlen+ln)) {
            properties.setHeaderSize((len-hlen)+l);
        }

        System.out.println("Body size : " + properties.getBodySize());
    }

    public int getHeaderEnd(){
        for (int i = 0; i < (len-3); i++) {
            if (isEOH(i)) return i;
        }
        return -1;
    }

    private void parseHead() throws IndexOutOfBoundsException {
        int spc=0;
        while (!isEOL(cur)){
            if (hdr[cur]==' ') {
                spc++;
                cur++;
            }
            // Read Method
            if (spc == 0) {
                if (mtd.capacity()==mtd.length()) {
                    errCode = ResponseCode.BAD_REQUEST;
                    return;
                    //throw new IndexOutOfBoundsException("Bad Request Method !");
                }
                mtd.append((char) hdr[cur]);
            }
            // Read Route
            else if (spc == 1) {
                if (route.capacity()==route.length()) {
                    errCode = ResponseCode.BAD_REQUEST;
                    return;
                    //throw new IndexOutOfBoundsException("Bad Request Route !");
                }
                route.append((char) hdr[cur]);
            } else if (spc == 2) {
                ;
            }
            cur++;
        }

    }

    private void setMethod(){
        String m = mtd.toString().toUpperCase();
        switch (m) {
            case "POST":
                properties.setMethod(HTTPMethod.POST);
                break;
            case "GET":
                properties.setMethod(HTTPMethod.GET);
                break;
            case "HEAD":
                properties.setMethod(HTTPMethod.HEAD);
                break;
            case "OPTIONS":
                properties.setMethod(HTTPMethod.OPTIONS);
                break;
        }
    }

    private void parseKV(){
        //cur=0;
        boolean colon=false;
        nsb.setLength(0);
        vsb.setLength(0);
        while (!isEOL(cur)){
            if (hdr[cur]==':'){
                colon = true;
                cur++;
                cur++;
                continue;
            }
            if (colon){
                vsb.append((char) hdr[cur]);
                cur++;
            } else {
                nsb.append((char) hdr[cur]);
                cur++;
            }
        }
        //System.out.println("Key : "+nsb.toString()+", Value : "+vsb.toString());
        properties.setHeader(nsb.toString(), vsb.toString());
    }

    private boolean isEOL(int i){
        if ((i+1)<len){
            if (hdr[i]=='\r' && hdr[i+1]=='\n'){
                return true;
            }
        }
        return false;
    }

    private boolean isEOH(int i){
        if ((i+3)<len) {
            if (isEOL(i) && isEOL(i+2)) {
                return true;
            }
        }
        return false;
    }

    public HTTPConnection getHTTPConnection(){
        return this.properties;
    }

    public int getErrCode(){
        return errCode;
    }
}
