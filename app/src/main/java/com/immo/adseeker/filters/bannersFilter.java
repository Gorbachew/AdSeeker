package com.immo.adseeker.filters;

import android.util.Log;
import com.immo.adseeker.MainActivity;

public class bannersFilter {
    final String[] idBanners = new String[]{"advgalaxy.com","xyz0k4gfs"};

    public int searchBanners(MainActivity mainActivity, String data) {
        String result = "";

        for(int i = 0; i < idBanners.length; i++){
            int find = data.indexOf(idBanners[i]);
            if(find != -1){
                mainActivity.analyticLog.setNumBanners(1);
                result += idBanners[i];
            }

        }
        mainActivity.analyticLog.setBanners(result + ">");
        Log.i("banners founded", String.valueOf(mainActivity.analyticLog.getNumBanners()));
        return mainActivity.analyticLog.getNumBanners();
    }

}
