package ir.kasra_sh.MikroServer.HTTPUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class KSocket {
    private java.net.Socket socket = null;
    private DataInputStream dis;
    private DataOutputStream dos;
    private byte[] lineBuffer;
    private String lastLine;
    private int lastInt;
    private byte lastByte;
    private char lastChar;
    private int lineBufferSize = 2048;

    public KSocket(java.net.Socket socket) throws IOException {
        this.socket = socket;
        init();
    }

    public KSocket(java.net.Socket socket, int lineBufferSize) throws IOException {
        this.socket = socket;
        this.lineBufferSize = lineBufferSize;
        init();
    }

    private void init() throws IOException {
        dis = new DataInputStream(socket.getInputStream());
        dos = new DataOutputStream(socket.getOutputStream());
        lineBuffer = new byte[lineBufferSize];
    }


    public void flush() {
        try {
            dos.flush();
        } catch (IOException e) {}
    }

    public void close(){
        try {
            socket.close();
            dos.close();
        } catch (IOException e) {}
    }

    public byte readByte(int i) throws IOException {
        lastByte = dis.readByte();
        return lastByte;
    }

    public int readInt(int i) throws IOException {
        lastInt = dis.readInt();
        return lastInt;
    }

    public char readChar() throws IOException {
        lastChar = dis.readChar();
        return lastChar;
    }

    public int readBytes(byte[] b, int start, int len) throws IOException {
        return dis.read(b, start, len);
    }

    public int readBytes(byte[] b) throws IOException {
        return dis.read(b);
    }

    public String readLineString(byte[] ... endSeq) throws IOException, IndexOutOfBoundsException {
        setLineBufferSize(lineBufferSize);
        int index=0;
        boolean match = false;
        while (true) {
            lineBuffer[index] = dis.readByte();
            if (index>0)
            for (int j = 0; j < endSeq.length; j++) {
                //System.out.println();
                if (index>=endSeq[j].length) {
                    match = true;
                    for (int i = 0; i < endSeq[j].length; i++) {
                        //System.out.println("Seq"+j+" checking "+i+" : "+(char)lineBuffer[(index - endSeq[j].length)+i+1]);
                        if (lineBuffer[(index - endSeq[j].length)+i+1] == endSeq[j][i]){
                            continue;
                        } else {
                            match = false;
                            break;
                        }
                    }
                } else match = false;

                if (match) {
                    break;
                }
            }


            if (match) {
                lastLine = new String(lineBuffer, 0, index+1);
                //System.out.println("Index = "+index);
                //System.out.println(lineBuffer[index]);
                return lastLine;
            }
            index++;
        }
    }

    public int readLineBytes(byte[] b, int limit, byte[] ... endSeq) throws IOException, IndexOutOfBoundsException {
        setLineBufferSize(limit);
        int index=0;
        boolean match = false;
        while (true) {
            if (index > limit) {
                throw new IndexOutOfBoundsException("limit reached !");
            }
            lineBuffer[index] = dis.readByte();
            //System.out.println("read char : "+(char)lineBuffer[index]);
            //if (index>0)
            for (int j = 0; j < endSeq.length; j++) {
                //System.out.println();
                if (index>=endSeq[j].length-1) {
                    match = true;
                    for (int i = 0; i < endSeq[j].length; i++) {
                        //System.out.println("Seq"+j+" checking "+i+" : "+(char)lineBuffer[(index - endSeq[j].length)+i+1]);
                        if (lineBuffer[(index - endSeq[j].length)+i+1] == endSeq[j][i]) {
                            continue;
                        } else {
                            match = false;
                            break;
                        }
                    }
                } else match = false;

                if (match) {
                    break;
                }
            }

            if (match) {
                lastLine = new String(lineBuffer, 0, index+1);
                System.arraycopy(lineBuffer, 0, b, 0, index+1);
                //System.out.println("Index = "+index);
                //System.out.println(lineBuffer[index]);
                return index+1;
            }
            index++;
        }
    }


    public void writeByte(int b) throws IOException {
        dos.writeByte(b);
    }

    public void writeInt(int i) throws IOException {
        dos.writeInt(i);
    }

    public void writeBytes(byte[] bytes, int start, int len) throws IOException {
        dos.write(bytes, start, len);
    }

    public void writeBytes(byte[] bytes) throws IOException {
        dos.write(bytes);
    }

    public void writeString(String s) throws IOException {
        dos.write(s.getBytes("UTF-8"));
    }

    public void writeChar(int c) throws IOException {
        dos.writeChar(c);
    }

    public void writeChars(String s) throws IOException {
        dos.writeChars(s);
    }

    public void setLineBufferSize(int size) {
        this.lineBufferSize = lineBufferSize;
        if (size > lineBufferSize) {
            lineBuffer = new byte[size];
        }
    }

    public Socket getSocket() {
        return socket;
    }
    public int getLastInt() {
        return lastInt;
    }
    public char getLastChar() {
        return lastChar;
    }

    public String getLastLine() {
        return lastLine;
    }

}
