package ir.kasra_sh.MikroServer.Utils.eson;


import ir.kasra_sh.MikroServer.Utils.ULog;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by blkr on 3/31/18.
 *
 */

public class EsonObj {
    private static final String TAG = "ESONObj";

    private JSONObject jsonObject = new JSONObject();
    
    public EsonObj(){
        
    }

    public EsonObj(String json) {
        try {
            jsonObject = new JSONObject(json);
        }catch (Exception e){
            ULog.wtf(TAG,e);
        }
    }
    public JSONObject getObj() {
        return jsonObject;
    }

    public EsonObj setObj(JSONObject obj) {
        jsonObject = obj;
        return this;
    }

    public EsonObj add(String k, String v) {
        try {
            if (v == null) jsonObject.put(k, JSONObject.NULL);
            else jsonObject.put(k, v);
        } catch (Exception e){
            ULog.wtf(TAG, e);
        }
        return this;
    }

    public EsonObj add(String k, double v) {
        try {
            jsonObject.put(k, v);
        } catch (Exception e){
            ULog.wtf(TAG, e);
        }
        return this;
    }

    public EsonObj add(String k, boolean v) {
        try {
            jsonObject.put(k, v);
        } catch (Exception e){
            ULog.wtf(TAG, e);
        }
        return this;
    }
    public EsonObj add(String k, int v) {
        try {
            jsonObject.put(k, v);
        } catch (Exception e){
            ULog.wtf(TAG, e);
        }
        return this;
    }
    public EsonObj add(String k, long v) {
        try {
            jsonObject.put(k, v);
        } catch (Exception e){
            ULog.wtf(TAG, e);
        }
        return this;
    }

    public EsonObj add(String k, EsonObj v) {
        try {
            if (v == null) jsonObject.put(k, JSONObject.NULL);
            else jsonObject.put(k, v.getObj());
        } catch (Exception e){
            ULog.wtf(TAG, e);
        }
        return this;
    }

    public EsonObj add(String k, EsonArray v) {
        try {
            if (v == null) jsonObject.put(k, JSONObject.NULL);
            else jsonObject.put(k, v.getArr());
        } catch (Exception e){
            ULog.wtf(TAG, e);
        }
        return this;
    }

    public String getStr(String k) {
        return getStr(k, null);
    }

    public String getStr(String k, String defValue) {
        try {
            return jsonObject.getString(k);
        } catch (Exception e) {
            ULog.wtf(TAG, e);
        }
        return defValue;
    }

    public Integer getInt(String k) {
        return getInt(k, null);
    }

    public Integer getInt(String k, Integer defValue) {
        try {
            return jsonObject.getInt(k);
        } catch (Exception e) {
            ULog.wtf(TAG, e);
        }
        return defValue;
    }

    public Boolean getBool(String k) {
        return getBool(k, null);
    }

    public Boolean getBool(String k, Boolean defValue) {
        try {
            return jsonObject.getBoolean(k);
        } catch (Exception e) {
            ULog.wtf(TAG, e);
        }
        return defValue;
    }

    public Long getLong(String k) {
        return getLong(k, null);
    }

    public Long getLong(String k, Long defValue) {
        try {
            return jsonObject.getLong(k);
        } catch (Exception e) {
            ULog.wtf(TAG, e);
        }
        return defValue;
    }

    public Double getDouble(String k) {
        return getDouble(k, null);
    }

    public Double getDouble(String k, Double defValue) {
        try {
            return jsonObject.getDouble(k);
        } catch (Exception e) {
            ULog.wtf(TAG, e);
        }
        return defValue;
    }

    public EsonArray getArray(String k) {
        return new EsonArray().setArr(jsonObject.optJSONArray(k));
    }

    public <T> T mapObject(String k, Class<T> tClass) {
        return Eson.mapObject(jsonObject.optString(k,""), tClass);
    }

    public <T> ArrayList<T> mapArray(String k, Class<T> tClass) {
        return Eson.mapArray(jsonObject.optString(k, ""), tClass);
    }

    public <T> T mapSelf(Class<T> tClass) {
        return Eson.mapObject(jsonObject.toString(), tClass);
    }

    public String toString(int indent) {
        return jsonObject.toString();
    }

    @Override
    public String toString() {
        return jsonObject.toString();
    }


}
