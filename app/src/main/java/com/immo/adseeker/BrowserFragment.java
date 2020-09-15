package com.immo.adseeker;
import android.annotation.SuppressLint;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.immo.adseeker.support.Analytic;
import com.immo.adseeker.support.SupportMain;

public class BrowserFragment extends Fragment {

    WebView webView;
    private boolean loadHtml, loadJS;
    private MainActivity mainActivity;
    private CountDownTimer timerDelay,timerCooldown;
    private boolean delay;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = ((MainActivity)getActivity());
    }
    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saveInstanceState){

        View view = inflater.inflate(com.immo.adseeker.R.layout.browser_fragment, container, false);

        webView = view.findViewById(com.immo.adseeker.R.id.webView);
        //Подключает поддержку JS
        webView.getSettings().setJavaScriptEnabled(true);
        //Подключает интерфейс Js для функции в 0 параметре
        webView.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");
        //Перенаправляет ссылки на браузер внутри программы
        webView.setWebViewClient(new MyWebViewClient());
        //HTML интерфейс сайта
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                    loadHtml = true;
                    checkFinishLoad(false);
            }
            //Отслеживание переадресаций
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if(mainActivity.searchFragment.redirectPropertyCB.isChecked()){
                    Log.e("Redirects", String.valueOf(request.getUrl()));
                    mainActivity.analyticLog.setRedirects(request.getUrl() + ">");

                    String operator = mainActivity.searchFragment.etOperator.getText().toString();
                    String number = mainActivity.searchFragment.etPhoneNum.getText().toString();
                    String time = new SupportMain().getDate("time");

                    mainActivity.logCollector.add(operator + "  |  " + number + "  |  " + mainActivity.currentParseLink + "  |  " + request.getUrl() + "  |  " + time + "  |  ...");
                    mainActivity.logCollector.add(" ");

                    return super.shouldOverrideUrlLoading(view, request);
                }
                else{
                    Log.e("Redirects", "CheckBox redirects false: " + request.getUrl());
                    return false;
                }
            }
            //Ошибка загрузки страницы
            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if(!mainActivity.analyticLog.getErrors().contains("Received Http Error")){
                        mainActivity.analyticLog.setErrors("Received Http Error>");
                    }

                    Log.e("Error","Received Http Error = " + request.getUrl() + " | " + errorResponse.getData());
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
                mainActivity.analyticLog.setErrors("Received Ssl Error>");
                Log.e("Error","Received Ssl Error = " + handler.toString() + " | " + error.getUrl());
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mainActivity.analyticLog.setErrors(error.getDescription() + ">");
                    checkFinishLoad(true);
                    Log.e("Error"," Received Error = " + request.getUrl() + " " + error.getDescription());
                }
            }
        });
        //JS интерфейс сайта
        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if(progress == 100) {
                    loadJS = true;
                    checkFinishLoad(false);
                }
            }
            //Позволяет работать js alert();
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                result.confirm();
                return super.onJsAlert(view, url, message, result);
            }
            //Веб-контент запрашивает разрешение на доступ к указанным ресурсам
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                super.onPermissionRequest(request);
                Log.e("Permission Request", request.toString());
            }
            //Запрос геолокации
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                super.onGeolocationPermissionsShowPrompt(origin, callback);
                Log.e("Geolocation Permissions", callback.toString());
            }
            //Подтверждения перехода с текущей страницы

            @Override
            public boolean onJsBeforeUnload(WebView view, String url, String message, JsResult result) {
                Log.e("Js Before Unload", url + " " + message + " " + result);
                result.confirm();
                return super.onJsBeforeUnload(view, url, message, result);
            }

        });
        //Таймер, ждет минуту после получения данных о прогрессе загрузки сайта, если какой то сайт завис то продолжает работу программы скипая
        timerCooldown = new CountDownTimer(mainActivity.timeoutLoadSite,mainActivity.timeoutLoadSite) {
            @Override
            public void onTick(long millisUntilFinished) { }
            @Override
            public void onFinish() {
                Log.e("mainTimer","Finished Skip site");
                mainActivity.analyticLog.setErrors("Load timer timeout>");
                checkFinishLoad(true);
            }
        };
        //Ограничитель на очень быстрое получение от страницы, что она загружена (она не успевает прогрузиться на экране)
        timerDelay = new CountDownTimer(mainActivity.delayLoadSite,mainActivity.delayLoadSite) {
            @Override
            public void onTick(long millisUntilFinished) { }
            @Override
            public void onFinish() {
                delay = true;
                checkFinishLoad(false);
            }
        };
        return view;
    }

    //Проверка на завершение цикла загрузки
    private void checkFinishLoad(boolean error){
        if (error){
            Log.e("LoadError", "error true");
            ((MainActivity) getActivity()).endParse("error");
            loadJS = false;loadHtml = false;delay = false;
        }
        else if(loadHtml && loadJS && delay) {
            webView.loadUrl("javascript:window.HTMLOUT.processHTML('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
            loadJS = false;loadHtml = false;delay = false;
        }
    }

    //Начало цикла загрузки
    void displayPage(String url){
        //Функция для передачи данных из MainActivity после асинхронного запроса в сеть
        loadJS = false;loadHtml = false;delay = false;
        mainActivity.fileNotNeedSent = false;
        mainActivity.currentParseLink = url;
        webView.loadUrl(url);
        timerDelay.start();
        timerCooldown.start();
        mainActivity.analyticLog = new Analytic();
        Log.i("mainTimer","Started " + url);
    }

    //Интерфейс работы джаваскрипта отображаемой страницы во внутреннем браузере
    class MyJavaScriptInterface
    {
        @JavascriptInterface
        public void processHTML(String html)
        {
            ((MainActivity) getActivity()).endParse(html);
            timerCooldown.cancel();
        }
    }

    //Функция для перенаправления ссылки во внутренний браузер.
    private static class MyWebViewClient extends WebViewClient {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            view.loadUrl(request.getUrl().toString());
            return true;
        }
    }
}
