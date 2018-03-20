package ir.kasra_sh.MikroServer.HTTPUtils;

import co.paralleluniverse.fibers.Suspendable;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

@Suspendable
public class HTTPConnection {
    private Properties p = new Properties();
    private Properties o = new Properties();
    private HTTPMethod method;
    private String route;
    private KSocket socket;
    private StringBuilder rawHeader = new StringBuilder(2048);
    public ResponseWriter writer;
    private int headerSize = 0;
    private int bodySize = 0;
    protected byte[] body = null;
    private String bodyString = null;
    private RequestParser requestParser;
    private String filePath = null;
    private String context;
    public Request req = new Request(this);
    protected HashMap<String, byte[]> multipart;


    protected void setSocket(KSocket socket){
        this.socket = socket;
    }

    public KSocket kSocket(){
        return socket;
    }

    @Suspendable
    public void setHeader(String name, String value){
        p.setProperty(name, value);
    }

    @Suspendable
    public String getHeader(String name){
        String v = p.getProperty(name);
        if (v == null) {
            v = p.getProperty(name.toLowerCase());
        }
        return v;
    }

    @Suspendable
    public void setOption(String name, String value){
        o.putIfAbsent(name, value);
    }

    @Suspendable
    public String getOption(String name){
        return o.getProperty(name);
    }

    @Suspendable
    public HTTPMethod getMethod() {
        return method;
    }

    @Suspendable
    public void setMethod(HTTPMethod method) {
        this.method = method;
    }

    @Suspendable
    public String getRoute() {
        if (route == null)
            return "";
        else
            return route;
    }

    @Suspendable
    public void setRoute(String route) {
        if (!route.endsWith("/"))
            this.route = route+"/";
        else
            this.route = route;
    }

    @Suspendable
    public Properties getHeaders(){
        return p;
    }

    @Suspendable
    public Properties getOptions(){
        return o;
    }

    public int getHeaderSize() {
        return headerSize;
    }

    public void setHeaderSize(int headerSize) {
        this.headerSize = headerSize;
    }

    public int getBodySize() {
        return bodySize;
    }

    public void setBodySize(int bodySize) {
        this.bodySize = bodySize;
    }

    public byte[] getBodyBytes() {
        try {
            if (this.body == null) {
                requestParser.getBody();
            }
            return this.body;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getBody() {
        try {
            if (this.bodyString == null) {
                requestParser.getBody();
                this.bodyString = new String(this.body);
            }
            return this.bodyString;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public HashMap<String, byte[]> getMultiPart(){
        requestParser.getMultiPartBody();
        return multipart;
        //return null;
    }

    public byte[] getFormData(String name) {
        requestParser.getMultiPartBody();
        return multipart.get(name);
    }

    public String getFormDataString(String name) {
        requestParser.getMultiPartBody();
        return new String(multipart.get(name));
    }

    public void setRequestParser(RequestParser requestParser) {
        this.requestParser = requestParser;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public StringBuilder getRawHeader() {
        return rawHeader;
    }

    public void setRawHeader(StringBuilder rawHeader) {
        this.rawHeader = rawHeader;
    }
}
