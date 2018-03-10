package ir.kasra_sh.FileCache;

public class FixedWindowQueue {
    private Object[] q;
    private int len=0;
    private int cap;
    private boolean isFull=false;

    public FixedWindowQueue(int size){
        cap = size;
        q = new Object[size];
    }

    public Object pushRaw(Object t){
        Object e = null;
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

    public String push(String t){
        String e = null;
        if (len<cap) {
            q[len] = t;
            len++;
        } else {
            isFull = true;
            e = q[0].toString();
            System.arraycopy(q,1,q,0,cap-1);
            q[cap-1] = t;
        }
        return e;
    }

    public Object[] peekRaw(){
        return q.clone();
    }

    public String[] peek(){
        String[] t = new String[len];
        for (int i = 0; i < len; i++) {
            t[i] = q[i].toString();
        }
        return t;
    }

    public int size(){
        return len;
    }
}
