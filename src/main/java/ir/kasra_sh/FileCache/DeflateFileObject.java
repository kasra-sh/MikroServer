package ir.kasra_sh.FileCache;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.zip.Deflater;

public class DeflateFileObject {
    private byte[] data;
    private int currentSize=0;
    private String fileID;
    private FileTime lastModified;

    DeflateFileObject(String file) {
        fileID = file;
        loadFromFile(file);
    }

    public InputStream getInputStream() {
        return new ByteInputStream(data, currentSize);
    }

    public void readBytes(byte[] b) {
        System.arraycopy(data,0,b,0,currentSize);
    }

    public byte[] getBytes() {
        return data;
    }

    public void loadFromFile(String file) {
        try {
            Path p = Paths.get(file);
            data = compress(Files.readAllBytes(p));
            //System.out.println("Compressed to "+data.length/1024+"kb");
            this.currentSize = data.length;
        } catch (Exception e){
            e.printStackTrace();
        }
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

    public String getFileID() {
        return fileID;
    }

    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

    public FileTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(FileTime lastModified) {
        this.lastModified = lastModified;
    }

    public long getSize() {
        return currentSize;
    }

}
