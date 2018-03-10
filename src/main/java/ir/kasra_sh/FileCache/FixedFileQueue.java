package ir.kasra_sh.FileCache;

public class FixedFileQueue {
    private DeflateFileObject[] q;
    private int len=0;
    private int cap;
    private boolean isFull=false;

    public FixedFileQueue(int size){
        cap = size;
        q = new DeflateFileObject[size];
    }

    public DeflateFileObject pushRaw(DeflateFileObject t){
        DeflateFileObject e = null;
        if (len<cap) {
            q[len] = t;
            len++;
        } else {
            isFull = true;
            e = q[0];
            System.arraycopy(q,1,q,0,cap-1);
            q[cap-1] = t;
        }
        return e;
    }

    public DeflateFileObject push(DeflateFileObject t){
        DeflateFileObject e=null;
        if (len<cap) {
            q[len] = t;
            System.out.println(t.getFileID()+" Pushed");
            len++;
        } else {
            isFull = true;
            e = q[0];
            System.arraycopy(q,1,q,0,cap-1);
            q[cap-1] = t;
        }
        return e;
    }

    public Object[] peekRaw(){
        return q.clone();
    }

    public DeflateFileObject findByFileID(String fileID) {
        //System.out.println("Finding by ID");
        //System.out.println("Len = "+len);
        try {
            for (int i = 0; i < len; i++) {
                if (q[i].getFileID().equals(fileID)) {
                    //System.out.println(q[i].getFileID());
                    return q[i];
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("Not Found");
        return null;
    }

    public DeflateFileObject getDisposableObject() {
        if (isFull) {
            return q[0];
        } else return null;
    }

    public int size(){
        return len;
    }
}
