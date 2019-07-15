package vn.aiteam.hackathon.Utils;

import android.content.res.Resources;

public class UiUtils {

    public static int getResourceId(Resources resources, String pVariableName, String pResourcename, String pPackageName)
    {
        try {
            return resources.getIdentifier(pVariableName, pResourcename, pPackageName);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
