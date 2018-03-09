package ir.kasra_sh.MikroWebServer.HTTPUtils;

import co.paralleluniverse.fibers.Suspendable;

import java.io.IOException;
import java.util.Properties;

@Suspendable
public class HTTPConnectionEx {
    private Properties p = new Properties();
    private Properties o = new Properties();
    private HTTPMethod method;
    private String route;
    private SocketIO socket;
    private StringBuilder rawHeader = new StringBuilder(2048);
    public ResponseWriterEx writer;
    private int headerSize = 0;
    private int bodySize = 0;
    public byte[] body = null;
    private RequestParserEx requestParser;
    private String filePath = null;
    private String context;


    public void setSocket(SocketIO socket){
        this.socket = socket;
    }

    public SocketIO socketIO(){
        return socket;
    }

    @Suspendable
    public void setHeader(String name, String value){
        p.setProperty(name, value);
    }

    @Suspendable
    public String getHeader(String name){
        return p.getProperty(name);
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

    public byte[] getBody() {
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

    public void setRequestParser(RequestParserEx requestParser) {
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
