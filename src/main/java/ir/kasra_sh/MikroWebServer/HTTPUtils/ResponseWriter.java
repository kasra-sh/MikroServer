package ir.kasra_sh.MikroWebServer.HTTPUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class ResponseWriter {
    private ResponseHeader header = new ResponseHeader();
    private String h;
    boolean rh=false;
    private PrintWriter response;
    private OutputStream outputStream;

    public ResponseWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
        response = new PrintWriter(outputStream);
    }

    public void setLength(int len){
        header.setContentLength(len);
    }

    public void writeHeader(){
        if (!rh) {
            response.write(header.getFullHeader());
            response.flush();
            rh = true;
        }
    }

    public ResponseHeader getHeader() {
        return header;
    }

    public void append(String s){
        writeHeader();
        response.write(s);
        response.flush();
    }

    public void append(byte[] b, int i, int len) throws IOException {
        writeHeader();
        outputStream.write(b,i,len);
    }

    public void appendLine(String s){
        writeHeader();
        response.write(s);
        response.write('\n');
        response.flush();
    }

    public void finish(){
        response.flush();
        response.close();
    }

    public void write(String s){
        writeHeader();
        response.write(s);
        response.flush();
    }

    public void write(byte[] b,int i1, int i2) throws IOException {
        outputStream.write(b,i1,i2);
        outputStream.flush();
    }

    public void writeAll(String s){
        if (!rh) {
            header.setContentLength(s.length());
            writeHeader();
        }
        response.write(s);
        response.flush();
        response.close();
    }

    public void writeAll(byte[] b, int i, int len){
        if (!rh) {
            header.setContentLength(len);
            writeHeader();
        }
        try {
            outputStream.write(b,i,len);
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) { }
    }
}
