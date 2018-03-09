package ir.kasra_sh.MikroWebServer.HTTPUtils;

import ir.kasra_sh.MikroWebServer.Utils.MimeTypes;

import java.util.Properties;

public class ResponseHeader {
    private String status = "HTTP/1.1 # ";
    private String head;
    private StringBuffer sb = new StringBuffer(4096);
    private StringBuffer hdr = new StringBuffer(1024);
    private Properties headers = new Properties();

    public ResponseHeader() {
        headers.setProperty("Server","MikroServer");
    }

    public void setStatus(int responseCode){
        head =  status.replaceFirst("#", ResponseString.codeToString(responseCode));
    }

    public void setContentLength(int length){
        headers.setProperty("Content-Length", String.valueOf(length));
    }

    public void setContentTypeByExtension(String ext){
        setContentType(MimeTypes.byExt(ext));
    }

    public void setContentType(String type){
        headers.setProperty("Content-Type",type);
    }

    public void setProperty(String name, String value){
        headers.setProperty(name, value);
    }

    public String getProperty(String name){
        return headers.getProperty(name);
    }

    public void setEncoding(){}

    public String getFullHeader(){
        sb.setLength(0);
        if (head != null)
            sb.append(head).append("\r\n");
        else {
            setStatus(ResponseCode.OK);
        }
        for (String n:
             headers.stringPropertyNames()) {
            hdr.setLength(0);
            hdr.append(n).append(": ").append(headers.getProperty(n));
            sb.append(hdr).append("\r\n");
        }
        sb.append("\r\n");

        return sb.toString();
    }

}
