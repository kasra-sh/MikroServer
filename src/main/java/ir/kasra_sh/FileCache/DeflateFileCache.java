package ir.kasra_sh.FileCache;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class DeflateFileCache {

    private int size;
    private int last=-1;
    private int first=-1;
    FixedFileQueue ffq;
    //Hashtable<String, SimpleEntry<Integer,DeflateFileObject>> files;

    public DeflateFileCache(int size) {
        this.size = size;
        ffq = new FixedFileQueue(size);
    }

    public synchronized byte[] getFileBytes(String file) {
        DeflateFileObject dfo = ffq.findByFileID(file);
        //System.out.println("dfc.getFileBytes = "+dfo);
        if (dfo == null) { // file is not cached
            //System.out.println("File is not cached "+file);
            dfo = new DeflateFileObject(file);
            //System.out.println("Pushing + "+dfo.getFileID());
            ffq.push(dfo);
            //System.out.println("Cached "+file);
            return dfo.getBytes();
        } else {
            //System.out.println("File is cached "+file);
            return dfo.getBytes();
        }
    }

    public synchronized DeflateFileObject getFileObject(String file) {
        return ffq.findByFileID(file);
    }

    private void replaceIfModified() {

    }


}
