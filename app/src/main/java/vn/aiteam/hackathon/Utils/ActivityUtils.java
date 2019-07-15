package vn.aiteam.hackathon.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ActivityUtils {
    public static void nextActivity(Context context, Class<?> cls){
        nextActivity(context,cls,null);
    }

    public static void nextActivity(Context context, Class<?> cls, Bundle data){
        Intent i = new Intent(context, cls);
        if(data != null){
            i.putExtras(data);
        }
        context.startActivity(i);
    }

    public static void nextActivityForResult(Context context, Class<?> cls,int resultCode){
        nextActivityForResult(context,cls,resultCode,null);
    }
    public static void nextActivityForResult(Context context, Class<?> cls,int resultCode,Bundle data){
        Intent i = new Intent(context, cls);
        if(data != null){
            i.putExtras(data);
        }
        if(context instanceof Activity){
            Activity act = (Activity)context;
            act.startActivityForResult(i,resultCode);
            return;
        }

    }
}
