package com.immo.adseeker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.immo.adseeker.connect.JSONParser;
import com.immo.adseeker.connect.ServerConnect;
import com.immo.adseeker.filters.bannersFilter;
import com.immo.adseeker.filters.wordsFilter;
import com.immo.adseeker.support.Analytic;
import com.immo.adseeker.support.Logic;
import com.immo.adseeker.support.SupportMain;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    public String urlServer;
    public String urlJson;
    public String urlLoadInfo;

    public Analytic analyticLog;
    public boolean logsSent;
    public String searcher, siteUrl;

    public Button[] btns = new Button[4];
    //Список всех ссылок найденных в файле
    protected ArrayList<String> linksInPage = new ArrayList<>();
    protected ArrayList<String> brokenLinksInPage = new ArrayList<>();

    protected LinksFragment linksFragment;
    public SearchFragment searchFragment;
    protected BrowserFragment browserFragment;
    public SupportMain supportMain;
    protected JSONParser jsonParser;

    public boolean mainLink, sentFile, sentScreenshot, sentBrokenLinks, sentWorkLinks, sentAnalytic, fileNotNeedSent;
    public int numberServerSite;
    public String saveServerDir, currentParseLink;
    //Какой файл по счету в папке
    public int countParseLinks;
    //Сколько ожидает таймер прогрузки страницы.Сколько ожидает таймер, во избежания мгновенных результатов, что сайт загружен
    protected int timeoutLoadSite = 10000, delayLoadSite = 5000;

    public ArrayList<String> logCollector = new ArrayList<>();

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        //Убирает шапку
        getSupportActionBar().hide();
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(ContextCompat.getColor(this,R.color.colorBlack));
        }

        linksFragment = new LinksFragment();
        searchFragment = new SearchFragment();
        browserFragment = new BrowserFragment();
        supportMain = new SupportMain(this);
        jsonParser = new JSONParser();
        //Отображение фрагментов
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_place,searchFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_place,linksFragment).commit();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_place,browserFragment).commit();

        //Обьявляет кнопки и выдает им обработчик событий
        btns[0] = findViewById(R.id.searchBtn);
        btns[1] = findViewById(R.id.linksBtn);
        btns[2] = findViewById(R.id.browserBtn);
        btns[3] = findViewById(R.id.stopBtn);

        for (Button btn : btns) {
            btn.setOnClickListener(this);
            btn.setOnClickListener(this);
        }
        btns[0].setEnabled(false);
    }


    //Обработчик кнопки
    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.searchBtn:
                getSupportFragmentManager().beginTransaction().show(searchFragment).hide(linksFragment).hide(browserFragment).commit();
                btns[0].setEnabled(false);
                btns[1].setEnabled(true);
                btns[2].setEnabled(true);
                break;
            case R.id.linksBtn:
                //Переключение html фрагмента
                getSupportFragmentManager().beginTransaction().show(linksFragment).hide(searchFragment).hide(browserFragment).commit();
                btns[0].setEnabled(true);
                btns[1].setEnabled(false);
                btns[2].setEnabled(true);
                break;
            case R.id.browserBtn:
                //Переключение browser фрагмента
                getSupportFragmentManager().beginTransaction().show(browserFragment).hide(searchFragment).hide(linksFragment).commit();
                btns[0].setEnabled(true);
                btns[1].setEnabled(true);
                btns[2].setEnabled(false);
                break;
            case R.id.stopBtn:
                searchFragment.searchTasks.clear();
                logicContinues();

                break;
            case R.id.link:
                //Обработчик нажатия на ссылки
                //Перекидывает на окно браузера и открывает страницу с ссылкой
                TextView tvLink = v.findViewById(R.id.link);

                linksFragment.startAsync(tvLink.getText().toString());

                getSupportFragmentManager().beginTransaction().show(browserFragment).hide(searchFragment).hide(linksFragment).commit();
                btns[0].setEnabled(true);
                btns[1].setEnabled(true);
                btns[2].setEnabled(false);
                break;


        }
    }
    //Финальная функция в цикле парса страницы
    protected void endParse(String data) {
        if (linksFragment.urlsSites.size() > 0) {
            Log.i("FinalParse", "true");
            String name = new SupportMain(this).splitLink(linksFragment.parsingLink);;
            ArrayList<ArrayList<String>> allLinks = new SupportMain(this).parseOnLinks(data);

            if(searchFragment.wordsPropertyCB.isChecked()){
                Log.i("Found words","true");
                analyticLog.setNumKeyWords(new wordsFilter().searchWord(this, data, JSONParser.keyWords));
            }
            else{
                Log.i("Found words","false");
            }

            if(searchFragment.bannersPropertyCB.isChecked()){
                Log.i("Found banners","true");
                analyticLog.setNumBanners(new bannersFilter().searchBanners(this, data));
            }
            else{
                Log.i("Found banners","false");
            }

            linksInPage = allLinks.get(0);
            //Если больше 0 редиректов или больше 0 найденных слов

            String redirects = analyticLog.getRedirects();

            if(!redirects.equals("") || analyticLog.getNumKeyWords() > 0 || analyticLog.getNumBanners() > 0) {
                writeFiles(name, data, allLinks);
                fileNotNeedSent = false;
            }
            else{
                fileNotNeedSent = true;
            }

            if(linksFragment.randomLinksFromMineSiteForParse.size() == 0 && mainLink){
                //Выдает ссылки по странице
                Log.i("EndParse","Get links");
                mainLink = false;
                linksFragment.randomLinksFromMineSiteForParse.addAll(supportMain.getRandomLinks(linksInPage, searchFragment.howManyLinksToCheck));
            }
            countParseLinks++;
            //При отправке логика работает после самой отправки в ServerConnect(), если файл отправлять не надо то логика срабатывает здесь
            if(fileNotNeedSent){
                logicContinues();
            }
        }
    }

    //Логика как вести себя приложению после отработанного цикла
    public void logicContinues(){
        //Проверка на все выполненные условия потоков отправки на сервер
        if(sentFile || !searchFragment.htmlPropertyCB.isChecked() || fileNotNeedSent){
            if((sentScreenshot && sentBrokenLinks && sentWorkLinks) || fileNotNeedSent){
                runOnUiThread(() -> {
                    sentFile = false;sentScreenshot = false;sentBrokenLinks = false;sentWorkLinks = false;sentAnalytic = false;
                    if(searchFragment.searchTasks.size() == 0){
                        //Конец парса
                        Log.i("EndParse", "Parse end");
                        btns[1].performClick();
                        String dataVisitedSites = "";
                        for(String str : logCollector){
                            dataVisitedSites += str + "\n";
                            Log.i("logs", str);
                        }
                        //Отправляет файл на сервер
                        new ServerConnect(this, "LogVisitedSites").execute(urlLoadInfo, dataVisitedSites, searchFragment.date + "_" + searchFragment.time + "_log.txt");
                    }
                    else if (linksFragment.urlsSites.size() == 0) {
                        new Logic().NextTask(this);
                    } else if (linksFragment.randomLinksFromMineSiteForParse.size() > 0) {
                        new Logic().NextLink(linksFragment);
                    } else if (linksFragment.urlsSites.get(0).size() > 0) {
                        new Logic().NextSite(this, linksFragment);
                    } else if (linksFragment.urlsSites.get(0).size() == 0) {
                        new Logic().NextSearcher(this,searchFragment,linksFragment);
                    }
                });
            }
        }

    }
    //Закрыват клавиатуру, чтобы не мешать скриншоту
    public void closeKeyboard(){
        View view = getCurrentFocus();
        if(view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    //Создание скриншота
    private void makeScreenshot(String fileName){
        View view = getWindow().getDecorView().findViewById(R.id.main);
        Bitmap bitmap = SupportMain.getScreenshot(view);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] b = stream.toByteArray();
        bitmap.recycle();
        String base64File = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            base64File = Base64.getEncoder().encodeToString(b);
        }
        new ServerConnect(this,"screen").execute(urlLoadInfo, base64File, fileName);
    }

    private void writeFiles(String name, String data, ArrayList<ArrayList<String>> allLinks){
        if(searchFragment.htmlPropertyCB.isChecked()){
            Log.i("Html file sent","true");
            new ServerConnect(this, "file").execute(urlLoadInfo, data, name + ".html");
        }
        else{
            Log.i("Html file sent","false, checkbox html don`t checked");
        }

        //Делает скриншот страницы
        makeScreenshot("screen.png");

        String workLinks = "";
        for (String link : linksInPage)workLinks += link + "\n";
        new ServerConnect(this, "workLinks").execute(urlLoadInfo, workLinks, "workLinks.txt");

        brokenLinksInPage = allLinks.get(1);
        String brokenLinks = "";
        for (String link : brokenLinksInPage)brokenLinks += link + "\n";
        new ServerConnect(this, "brokenLinks").execute(urlLoadInfo, brokenLinks, "brokenLinks.txt");
    }

    public void clearInfoNextSite(){
        countParseLinks = 0;
        linksFragment.clearInfo();
        browserFragment.webView.loadUrl("about:blank");
        mainLink = true;
    }

    public void clearInfoNextSession(){
        logCollector.clear();
    }


}




