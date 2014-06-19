
package com.nexle.makeavatar;

import java.util.UUID;

public class StringUtils {

    public static String getString(int resourceId) {
        return CustomApplication.getAppContext().getResources().getString(resourceId);
    }
    
    public static String getNewPhotoID() {
        return UUID.randomUUID().toString();
    }
    
    public static String getCheckSum() {
        return UUID.randomUUID().toString() + System.currentTimeMillis();
    }
}
