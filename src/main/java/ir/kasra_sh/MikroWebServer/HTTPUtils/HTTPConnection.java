package ir.kasra_sh.MikroWebServer.HTTPUtils;

import co.paralleluniverse.fibers.Suspendable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Properties;

@Suspendable
public class HTTPConnection {
    private Properties p = new Properties();
    private Properties o = new Properties();
    private HTTPMethod method;
    private String route;
    private Socket socket;
    public ResponseWriter writer;
    private int headerSize = 0;
    private int bodySize = 0;
    public byte[] body;
    private RequestParser requestParser;


    public void setSocket(Socket socket){
        this.socket = socket;
    }

    public Socket getSocket(){
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

    public void getBody() {
        try {
            requestParser.getBody();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setRequestParser(RequestParser requestParser) {
        this.requestParser = requestParser;
    }
}
