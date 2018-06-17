package kobayashi.taku.taptappun.net.spajam2018;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import java.util.Map;
import java.util.Set;

public class Preferences{

  public static SharedPreferences getCommonPreferences(Context context){
    return PreferenceManager.getDefaultSharedPreferences(context);
  }

  public static void saveCommonParam(Context context, String key, Object value){
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = sp.edit();
    if(value instanceof String){
      editor.putString(key, (String) value);
    }else if(value instanceof Integer){
      editor.putInt(key, (Integer) value);
    }else if(value instanceof Float){
      editor.putFloat(key, (Float) value);
    }else if(value instanceof Boolean){
      editor.putBoolean(key, (Boolean) value);
    }else if(value instanceof Long){
      editor.putLong(key, (Long) value);
    }else if(value instanceof Double){
      long val = Double.doubleToRawLongBits((Double) value);
      editor.putLong(key, val);
    }
    editor.commit();
  }

  public static void saveCommonParam(Context context, Map<String, Object> keyValue){
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = sp.edit();
    for(Map.Entry<String, Object> e : keyValue.entrySet()){
      if(e.getValue() instanceof String){
        editor.putString(e.getKey(), (String) e.getValue());
      }else if(e.getValue() instanceof Integer){
        editor.putInt(e.getKey(), (Integer) e.getValue());
      }else if(e.getValue() instanceof Float){
        editor.putFloat(e.getKey(), (Float) e.getValue());
      }else if(e.getValue() instanceof Boolean){
        editor.putBoolean(e.getKey(), (Boolean) e.getValue());
      }else if(e.getValue() instanceof Long){
        editor.putLong(e.getKey(), (Long) e.getValue());
      }else if( e.getValue() instanceof Double){
        long value = Double.doubleToRawLongBits((Double)  e.getValue());
        editor.putLong(e.getKey(), value);
      }
    }
    editor.commit();
  }

  public static void saveCommonParam(Context context, Bundle keyValue){
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    SharedPreferences.Editor editor = sp.edit();
    Set<String> keys = keyValue.keySet();
    for(String key : keys){
      if(keyValue.get(key) instanceof String){
        editor.putString(key, (String) keyValue.get(key));
      }else if(keyValue.get(key) instanceof Integer){
        editor.putInt(key, (Integer) keyValue.get(key));
      }else if(keyValue.get(key) instanceof Float){
        editor.putFloat(key, (Float) keyValue.get(key));
      }else if(keyValue.get(key) instanceof Boolean){
        editor.putBoolean(key, (Boolean) keyValue.get(key));
      }else if(keyValue.get(key) instanceof Long){
        editor.putLong(key, (Long) keyValue.get(key));
      }else if(keyValue.get(key) instanceof Double){
        long value = Double.doubleToRawLongBits((Double) keyValue.get(key));
        editor.putLong(key, value);
      }
    }
    editor.commit();
  }

  public static double getDouble(SharedPreferences sp, String key, double defaultValue){
    if ( !sp.contains(key)){
      return defaultValue;
    }
    return Double.longBitsToDouble(sp.getLong(key, 0));
  }
}
