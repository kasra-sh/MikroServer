package ir.kasra_sh.HTTPUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;

public class ResponseWriter {
    private ResponseHeader header = new ResponseHeader();
    private String h;
    boolean rh=false;
    private SocketIO socketIO;

    public ResponseWriter(SocketIO socketIO) {
        this.socketIO = socketIO;
    }

    public void setLength(int len){
        header.setContentLength(len);
    }

    public void writeHeader(){
        if (!rh) {
            try {
                socketIO.writeString(header.getFullHeader());
            } catch (IOException e) {
                //e.printStackTrace();
            }
            socketIO.flush();
            rh = true;
        }
    }

    public ResponseHeader getHeader() {
        return header;
    }

    public void append(String s) throws IOException {
        writeHeader();
        socketIO.writeString(s);
        socketIO.flush();
    }

    public void append(byte[] b, int i, int len) throws IOException {
        writeHeader();
        socketIO.writeBytes(b,i,len);
        socketIO.flush();
    }

    public void appendLine(String s){
        writeHeader();
        try {
            socketIO.writeString(s);
            socketIO.writeString("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        socketIO.flush();
    }

    public void finish(){
        socketIO.flush();
        socketIO.close();
    }

    public void write(String s){
        writeHeader();
        try {
            socketIO.writeString(s);
            socketIO.flush();
        } catch (IOException e) {}
    }

    public void write(byte[] b,int i1, int i2) throws IOException {
        socketIO.writeBytes(b,i1,i2);
        socketIO.flush();
    }

    public void writeAll(String s){
        if (!rh) {
            header.setContentLength(s.length());
            writeHeader();
        }
        try {
            socketIO.writeString(s);
        } catch (IOException e) {}
        socketIO.flush();
        socketIO.close();
    }

    public void writeResponse(int responseCode, String s) throws IOException {
        if (!rh) {
            header.setStatus(responseCode);
            header.setContentLength(s.length());
            writeHeader();
        }
        try {
            compress(s.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        socketIO.writeString(s);
        socketIO.flush();
        socketIO.close();
    }

    public void writeResponseCompressed(int responseCode, String s){
        byte[] comp;
        try {
            comp = compress(s.getBytes());
            if (!rh) {
                header.setStatus(responseCode);
                header.setContentLength(comp.length);
                header.setProperty("Content-Encoding", "deflate");
                writeHeader();
            }
            try {
                socketIO.writeBytes(comp);
                socketIO.flush();
                socketIO.close();
            } catch (Exception e) {
                //e.printStackTrace();
            }
        } catch (IOException e) {
            //e.printStackTrace();
        }

        //String x = new String(comp,0,comp.length);
        //System.out.println(x);


    }

    public void writeResponse(int responseCode, byte[] b, int i, int len){
        if (!rh) {
            header.setStatus(responseCode);
            header.setContentLength(len);
            writeHeader();
        }
        try {
            socketIO.writeBytes(b,i,len);
            socketIO.flush();
            socketIO.close();
        } catch (IOException e) { }
    }


    public void writeAll(byte[] b, int i, int len){
        if (!rh) {
            header.setContentLength(len);
            writeHeader();
        }
        try {
            socketIO.writeBytes(b,i,len);
            socketIO.flush();
            socketIO.close();
        } catch (IOException e) { }
    }

    private byte[] compress(byte[] data) throws IOException {
        Deflater deflater = new Deflater();
        deflater.setInput(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);

        deflater.finish();
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        outputStream.close();
        return outputStream.toByteArray();
    }

}
