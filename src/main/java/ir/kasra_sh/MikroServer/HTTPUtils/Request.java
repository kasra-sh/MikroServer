package ir.kasra_sh.MikroServer.HTTPUtils;

import java.util.Set;

public class Request {
    private HTTPConnection conn;
    public Request(HTTPConnection con) {
        this.conn = con;
    }

    public HTTPMethod getMethod() {
        return conn.getMethod();
    }

    public String getRoute() {
        return conn.getRoute();
    }

    public byte[] getBodyBytes() {
        return conn.getBodyBytes();
    }

    public String getBody() {
        return conn.getBody();
    }

    public String getFilePath() {
        return conn.getFilePath();
    }

    public int getContentLength() {
        try {
            return Integer.valueOf(getHeader("Content-Length"));
        } catch (Exception e){
            return 0;
        }
    }

    public String getArg(String name) {
        String v = conn.getOptions().getProperty(name);
        if (v==null) {
            v = conn.getOptions().getProperty(name.toLowerCase());
            //if (v == null) return null;
        }
        return v;
    }

    public Set<String> getArgs() {
        return conn.getOptions().stringPropertyNames();
    }

    public String getHeader(String name) {
        return conn.getHeader(name);
    }

    public Set<String> getHeaders() {
        return conn.getHeaders().stringPropertyNames();
    }

    public String getRouteExtra() {
        return conn.getRoute().substring(conn.getContext().length());
    }

}
