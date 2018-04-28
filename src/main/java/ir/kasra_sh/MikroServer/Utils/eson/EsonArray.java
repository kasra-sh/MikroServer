package ir.kasra_sh.MikroServer.Utils.eson;

import ir.kasra_sh.MikroServer.Utils.ULog;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by blkr on 3/31/18.
 */

public class EsonArray {
    private static final String TAG = "ESONArray";

    private JSONArray jarr = new JSONArray();

    public EsonArray() {

    }

    public static EsonArray make() {
        return new EsonArray();
    }
    public JSONArray getArr() {
        return jarr;
    }

    public EsonArray setArr(JSONArray jarr) {
        this.jarr = jarr;
        return this;
    }
    public EsonArray add(EsonObj obj) {
        jarr.put(obj);
        return this;
    }

    public EsonObj get(int i) {
        EsonObj obj = new EsonObj();
        try {
            obj.setObj(jarr.getJSONObject(i));
        } catch (JSONException e) {
            ULog.wtf(TAG, e);
        }
        return obj;
    }

    public int length() {
        return jarr.length();
    }

    public String getString() {
        return jarr.toString();
    }
}
