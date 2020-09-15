package com.immo.adseeker.connect;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.immo.adseeker.MainActivity;
import com.immo.adseeker.support.Analytic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

public class ServerConnect extends AsyncTask<String,Void,Void> {
    @SuppressLint("StaticFieldLeak")
    private MainActivity mainActivity;
    private String typeFile;

    public ServerConnect(Activity activity,String type){
        mainActivity = (MainActivity)activity;
        typeFile = type;
    }


    //execute param 0 - urlServer, 1 - fileDir/ data, 2 - name file
    @Override
    protected Void doInBackground(String... link) {
        String myURL = link[0];
        String procParam;
        if(!typeFile.equals("LogVisitedSites")){
            if(mainActivity.saveServerDir == null || mainActivity.saveServerDir.equals("")){
                generateDirForFile(link[2]);
            }
            String data = link[1];
            String file = link[2];

            procParam = generateParamForFile(data, file);
        }
        else{
            generateDirForLog();
            String data = link[1];
            String file = link[2];
            procParam = generateParamForLog(data, file);
        }

        DispatchFiles(myURL, procParam);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        //file.delete();
        switch (typeFile){
            case "file":
                mainActivity.sentFile = true;
                Log.i("Server Connect","File sent");
                break;
            case "screen":
                mainActivity.sentScreenshot = true;
                Log.i("Server Connect","Screenshot sent");
                break;
            case "workLinks":
                mainActivity.sentWorkLinks = true;
                Log.i("Server Connect","File with work links sent");
                break;
            case "brokenLinks":
                mainActivity.sentBrokenLinks = true;
                Log.i("Server Connect","File with broken links sent");
                break;
            case "LogVisitedSites":
                Log.i("Server Connect","File with LogVisitedSites sent");
                mainActivity.logsSent = true;
                break;
        }
        if(!mainActivity.logsSent){
            mainActivity.logicContinues();
        }
    }

    private void DispatchFiles(String myURL, String procParam){
        try {
            byte[] data;
            InputStream is;

            URL url = new URL(myURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", "" + procParam.getBytes().length);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // конвертируем передаваемую строку в UTF-8
            data = procParam.getBytes("UTF-8");
            OutputStream os = conn.getOutputStream();
            // передаем данные на сервер
            os.write(data);
            os.flush();
            os.close();
            data = null;
            conn.connect();

            int responseCode= conn.getResponseCode();
            // передаем ответ сервер
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (responseCode == 200) {    // Если все ОК (ответ 200)
                is = conn.getInputStream();
                byte[] buffer = new byte[8192]; // размер буфера
                // Далее так читаем ответ
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                data = baos.toByteArray();
                String resultString;
                resultString = new String(data, "UTF-8");  // сохраняем в переменную ответ сервера, у нас "OK"
                Log.i("Server response", resultString);
            }
            conn.disconnect();
        } catch (MalformedURLException e) {
            Log.e("ServerConnectEx1", e.toString());
        } catch (IOException e) {
            Log.e("ServerConnectEx2", e.toString());
        } catch (Exception e) {
            Log.e("ServerConnectEx3", e.toString());
        }
    }

    private String generateParamForFile(String data, String file){
        String param;
        param = "dir=" + mainActivity.saveServerDir + "&";
        param += "name=" + file + "&";
        //param += "data=" + conversionByte(fileDir,file)  + "&";
        if(!typeFile.equals("screen")){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                byte[] b = data.getBytes();
                param += "data=" + Base64.getEncoder().encodeToString(b)  + "&";
            }
        }
        else{
            param += "data=" + data  + "&";
        }
        if(!mainActivity.sentAnalytic) {
            param += "analytic=" + generateAnalytic();
        }
        return param.replaceAll("[+]","%2B");
    }
    private String generateParamForLog(String data, String file){
        String param;
        param = "dir=" + mainActivity.saveServerDir + "&";
        param += "name=" + file + "&";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            byte[] b = data.getBytes();
            param += "data=" + Base64.getEncoder().encodeToString(b)  + "&";
        }
        return param.replaceAll("[+]","%2B");
    }

    private void generateDirForFile(String domen){
        mainActivity.searcher = mainActivity.searchFragment.changedUrlsSearchers.get(0).split("/")[2];
        mainActivity.siteUrl = domen;
        String newDomen = splitDomen(mainActivity.siteUrl);
        mainActivity.numberServerSite++;
        mainActivity.saveServerDir = "Analytics/" +
                mainActivity.searchFragment.date + "/" +
                mainActivity.searchFragment.searchTask + "/" +
                mainActivity.searcher + "/"
                + newDomen;
    }

    private void generateDirForLog(){
        mainActivity.numberServerSite++;
        mainActivity.saveServerDir = "logs/";
    }


    private String splitDomen(String domen){
        String[] splited = domen.split("\\.");
        String newDomen;
        if(splited.length > 3)
            newDomen = mainActivity.numberServerSite + "_" + splited[1] + "." + splited[2];
        else if(splited.length > 2)
            newDomen = mainActivity.numberServerSite + "_" + splited[0] + "." + splited[1];
        else
            newDomen = mainActivity.numberServerSite + "_" + splited[0];
        return newDomen;

    }

    private String generateAnalytic(){
        Analytic analytic = mainActivity.analyticLog;
        analytic.setSearcher(mainActivity.searcher);
        analytic.setDate(mainActivity.searchFragment.date + " " + mainActivity.searchFragment.time);
        analytic.setTask(mainActivity.searchFragment.searchTask);
        analytic.setSite(mainActivity.siteUrl);
        analytic.setUrl(mainActivity.currentParseLink);
        analytic.setDir(mainActivity.saveServerDir);
        String[] errors;

        if(!analytic.getErrors().equals("")){
            errors  = analytic.getErrors().split(">");
        }
        else{
            errors = new String[0];
        }
        analytic.setNumErrors(errors.length);

        String[] redirs;

        if(!analytic.getRedirects().equals("")){
            redirs = analytic.getRedirects().split(">");
        }
        else {
            redirs =  new String[0];
        }

        analytic.setNumRedir(redirs.length);

        String analyticLine = "";
        analyticLine += analytic.getSearcher() + "#";
        analyticLine += analytic.getDate() + "#";
        analyticLine += analytic.getTask() + "#";
        analyticLine += analytic.getSite() + "#";
        analyticLine += analytic.getUrl() + "#";
        analyticLine += analytic.getDir() + "#";
        analyticLine += checkNull(String.valueOf(analytic.getNumErrors()));
        analyticLine += checkNull(String.valueOf(analytic.getNumRedir()));
        analyticLine += checkNull(String.valueOf(analytic.getNumLinks()));
        analyticLine += checkNull(String.valueOf(analytic.getNumBrokenLinks()));
        analyticLine += checkNull(String.valueOf(analytic.getNumKeyWords()));
        analyticLine += checkNull(String.valueOf(analytic.getNumBanners()));

        analyticLine += "]";
        //Other tables value
        analyticLine += checkNull(analytic.getErrors());
        analyticLine += checkNull(analytic.getRedirects());
        analyticLine += checkNull(analytic.getKeyWords());
        analyticLine += checkNull(analytic.getBanners());

        Log.i("AnalyticLine", analyticLine);

        //]Received Http Error>##2##
        //www.google.com#16-06-2020 13-25#13-25_porn#www.porn2012.com.html#http://www.porn2012.com/ru/#Analytics/16-06-2020/13-25_porn/www.google.com/0_porn2012.com#1#0#271#0#0#0#

        mainActivity.sentAnalytic = true;

        return analyticLine;
    }

    private String checkNull(String checked){
        if( checked.equals("")){
            return "0#";
        }
        else{
            return checked + "#";
        }

    }

}
