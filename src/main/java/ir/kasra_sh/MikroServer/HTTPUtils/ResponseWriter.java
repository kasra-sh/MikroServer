package ir.kasra_sh.MikroServer.HTTPUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;

public class ResponseWriter {
    public ResponseHeader header = new ResponseHeader();
    private String h;
    boolean rh=false;
    private KSocket KSocket;

    public ResponseWriter(KSocket KSocket) {
        this.KSocket = KSocket;
    }

    public void setLength(int len){
        header.setContentLength(len);
    }

    public void writeHeader(){
        if (!rh) {
            try {
                KSocket.writeString(header.getFullHeader());
            } catch (IOException e) {
                //e.printStackTrace();
            }
            KSocket.flush();
            rh = true;
        }
    }

    /*public ResponseHeader getHeader() {
        return header;
    }*/

    public void append(String s) throws IOException {
        writeHeader();
        KSocket.writeString(s);
        KSocket.flush();
    }

    public void append(byte[] b, int i, int len) throws IOException {
        writeHeader();
        KSocket.writeBytes(b,i,len);
        KSocket.flush();
    }

    public void appendLine(String s){
        writeHeader();
        try {
            KSocket.writeString(s);
            KSocket.writeString("\n");
        } catch (IOException e) {
            e.printStackTrace();
        }

        KSocket.flush();
    }

    public void finish(){
        KSocket.flush();
        KSocket.close();
    }

    public void write(String s){
        writeHeader();
        try {
            KSocket.writeString(s);
            KSocket.flush();
        } catch (IOException e) {}
    }

    public void write(byte[] b,int i1, int i2) throws IOException {
        KSocket.writeBytes(b,i1,i2);
        KSocket.flush();
    }

    public void writeAll(String s){
        if (!rh) {
            header.setContentLength(s.length());
            writeHeader();
        }
        try {
            KSocket.writeString(s);
        } catch (IOException e) {}
        KSocket.flush();
        KSocket.close();
    }

    public void writeResponse(int responseCode, String s) throws IOException {
        if (!rh) {
            header.setStatus(responseCode);
            header.setContentLength(s.length());
            writeHeader();
        }
//        try {
//            compress(s.getBytes());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        KSocket.writeString(s);
        KSocket.flush();
        KSocket.close();
    }

    public void writeResponseCompressed(int responseCode, String s){
        byte[] comp;
        try {
            comp = compress(s.getBytes("UTF-8"));
            if (!rh) {
                header.setStatus(responseCode);
                header.setContentLength(comp.length);
                header.setProperty("Content-Encoding", "deflate");
                writeHeader();
            }
            try {
                KSocket.writeBytes(comp);
                KSocket.flush();
                KSocket.close();
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
            KSocket.writeBytes(b,i,len);
            KSocket.flush();
            KSocket.close();
        } catch (IOException e) { }
    }


    public void writeAll(byte[] b, int i, int len){
        if (!rh) {
            header.setContentLength(len);
            writeHeader();
        }
        try {
            KSocket.writeBytes(b,i,len);
            KSocket.flush();
            KSocket.close();
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
