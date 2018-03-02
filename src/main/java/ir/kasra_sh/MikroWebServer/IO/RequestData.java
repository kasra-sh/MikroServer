package ir.kasra_sh.MikroWebServer.IO;

import java.util.Map;

public class RequestData {
    public RequestType getType() {
        return type;
    }

    protected void setType(RequestType type) {
        this.type = type;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    protected void setHeader(Map<String, String> header) {
        this.header = header;
    }

    enum RequestType {
        POST, GET
    }

    private RequestType type;

    private Map<String,String> header;


}
