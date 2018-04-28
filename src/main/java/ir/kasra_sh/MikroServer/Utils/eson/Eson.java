package ir.kasra_sh.MikroServer.Utils.eson;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import ir.kasra_sh.MikroServer.Utils.ULog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Created by blkr on 3/31/18.
 *
 */

public class Eson {
    private static final String TAG = "ESON";

    private static Gson gson = new Gson();

    public static EsonObj objFrom(String json) {
        EsonObj esonObj = new EsonObj();
        try {
            esonObj.setObj(new JSONObject(json));
        } catch (JSONException e) {
            ULog.wtf(TAG, e);
        }
        return esonObj;
    }

    public static EsonArray arrayFrom(String json) {
        EsonArray esonArr = new EsonArray();
        try {
            esonArr.setArr(new JSONArray(json));
//            Log.e(TAG, new JSONArray(json).toString());
        } catch (JSONException e) {
            ULog.wtf(TAG, e);
        }
        return esonArr;
    }

    private static <T> T parseObj(String json, Class<T> tClass) {
        T obj = null;
        try {
            JSONObject jo = new JSONObject(json);
            obj = tClass.newInstance();
            Field[] fields = tClass.getDeclaredFields();
            for (Field f :
                    fields) {
                if (f.getName().equals("$change") || f.getName().equals("serialVersionUID")) {
                    continue;
                }
//                Log.e(TAG, f.getName());
                f.setAccessible(true);
                Annotation a;
                String name = f.getName();
                boolean required = false;
//                try {
//                    a = f.getAnnotation(EsonElement.class);
//                    name = ((EsonElement)a).name();
//                    //ULog.w(TAG, "Field "+f.getName()+" not annotated with JsonElement (using field name)");
//                }catch (Exception e){
//                    ULog.w(TAG, "no JsonElement annotation ... trying SerializedName");
                try {
                    a = f.getAnnotation(SerializedName.class);
                    name = ((SerializedName) a).value();
//                        ULog.w(TAG,"SerializedName : "+name);
                } catch (Exception ee){
//                        ULog.w(TAG, "Field "+f.getName()+" not annotated with JsonElement or SerializedName!");
                }
//                }

//                if (!f.getType().isPrimitive()) {
//                    f.set(f.getType(), jo.get(name));
//                    continue;
//                }
                try {
                    Class t = f.getType();
                    if (t == Integer.class) {
                        f.set(obj, jo.getInt(name));
                    }
                    else if (t == Long.class) {
                        f.set(obj, jo.getLong(name));
                    }
                    else if (t == String.class) {
                        f.set(obj, jo.getString(name));
                    }
                    else if (t == Double.class) {
                        f.set(obj, jo.getDouble(name));
                    }
                    else if (t == Float.class) {
                        f.set(obj, (float)jo.getDouble(name));
                    }
                    else if (t == Boolean.class) {
                        f.set(obj, jo.getBoolean(name));
                    }
                    else if (t == EsonObj.class) {
                        EsonObj eo = new EsonObj();
                        eo.setObj(jo.getJSONObject(name));
                        f.set(obj, eo);
                    }
                    else if (t == EsonArray.class) {
                        EsonArray eo = new EsonArray();
                        eo.setArr(jo.getJSONArray(name));
                        f.set(obj, eo);
                    }
                    else f.set(obj, jo.get(name));
                } catch (Exception e){
//                    e.printStackTrace();
                    ULog.w(TAG, "Ignored field "+name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NullPointerException("could not instantiate class");
        }
        return obj;
    }

    public static <T> ArrayList<T> fromJsonArray(String json, Class<T> tClass) {
        ArrayList<T> ret = new ArrayList<>();
        try {
            EsonArray ear = Eson.arrayFrom(json);
            for (int i = 0; i < ear.length(); i++) {
//                ULog.e(TAG, ear.get(i).getString());
                ret.add(parseObj(ear.get(i).toString(), tClass));
            }
        } catch (Exception e) {
            ULog.wtf(TAG, e);
        }
        return ret;
    }

    public static <T> T fromJson(String json , Class<T> tClass) {
        return parseObj(json, tClass);
        //return obj;
    }

    public static String toJson(Object src) {
        return new Gson().toJson(src);
    }

    public static EsonObj make() {
        return new EsonObj();
    }

    public static EsonObj wrapObj(JSONObject jsonObject) {
        return new EsonObj().setObj(jsonObject);
    }

    public static EsonArray wrapArr(JSONArray jsonArray) {
        return new EsonArray().setArr(jsonArray);
    }

}
