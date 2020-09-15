package com.immo.adseeker.support;

import com.immo.adseeker.MainActivity;

public class Connect {
    //dev
    //private String urlServer = "10.192.160.32:80";
    private static String urlServerDev = "192.168.1.158:8080";
    private static String urlJsonDev = "http://" + urlServerDev + "/ru.immo.adseeker.site/api/getinfo";
    private static String urlLoadInfoDev = "http://" + urlServerDev + "/ru.immo.adseeker.site/api/addinfo";

    //stable
    private static String urlServerStable = "adseeker.imb2bs.com";
    private static String urlJsonStable = "http://" + urlServerStable + "/api/getinfo";
    private static String urlLoadInfoStable = "http://" + urlServerStable + "/api/addinfo";

    public Connect(MainActivity mainActivity, String server){
        if(server.equals("Dev")){
            mainActivity.urlServer = urlServerDev;
            mainActivity.urlJson = urlJsonDev;
            mainActivity.urlLoadInfo= urlLoadInfoDev;
        }
        else if(server.equals("Stable")){
            mainActivity.urlServer = urlServerStable;
            mainActivity.urlJson = urlJsonStable;
            mainActivity.urlLoadInfo= urlLoadInfoStable;
        }
    }
}
