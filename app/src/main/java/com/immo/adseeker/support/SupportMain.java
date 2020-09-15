package com.immo.adseeker.support;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.immo.adseeker.MainActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class SupportMain {
    private MainActivity mainActivity;
    public SupportMain(){

    }
    public SupportMain(Activity activity){
        mainActivity = (MainActivity) activity;
    }


    //Класс с кучей вспомогательных методов, для разгрузки основных
    public static void setSpinner(Context context, Spinner spinner, ArrayList<String> list){
        //Заполнение списков данными
        //Адаптеры
        ArrayAdapter<String> adapterSites = new ArrayAdapter<String>(context,android.R.layout.simple_spinner_item, list);
        // Определяем разметку для использования при выборе элемента
        adapterSites.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Применяем адаптер к элементу spinner
        spinner.setAdapter(adapterSites);
    }
    //Создание скриншота
    public static Bitmap getScreenshot(View view){
        View screenView = view.getRootView();
        screenView.setDrawingCacheEnabled(true);
        Bitmap bitmap = Bitmap.createBitmap(screenView.getDrawingCache());
        screenView.setDrawingCacheEnabled(false);
        return bitmap;
    }

    //Проверка ссылки на запрещенные символы
    static boolean checkValidUrl(String url){
        char[] signs = url.toCharArray();
        for(char sign : signs){
            if(sign == '"' || sign == '<' || sign == '>' || sign == '=' || sign == '(' || sign == ')'
                    || sign == '?' || sign == '!' || sign == '*' || sign == '\'' || sign == ';'
                    || sign == '@' || sign == '&' || sign == '+' || sign == '$' || sign == ','
                    || sign == '#' || sign == '[' || sign == ']' || sign == '%' || sign == '\\'){
                return false;
            }
            else if(signs.length <= 14){
                return false;
            }
        }
        return true;
    }

    //Выбирает рандомные ссылки из предложенных
    public ArrayList<String> getRandomLinks(ArrayList<String> linksInPage,int howManyLinksToCheck){
        ArrayList<String> list = new ArrayList<>();
        if(linksInPage.size() > 0){
            Random random = new Random();
            for(int i = 0;i < howManyLinksToCheck;i++){
                list.add(linksInPage.get(random.nextInt(linksInPage.size())));
            }
        }
        return list;
    }

    //Разбивает на ссылки
    public ArrayList<ArrayList<String>> parseOnLinks(String text){
        ArrayList<ArrayList<String>> allLinks = new ArrayList<>();
        ArrayList<String> parseLinks = new ArrayList<>();
        ArrayList<String> parseBreakLinks = new ArrayList<>();
        //Вывод информациии из выбранного файла
        String[] ads = text.split("http");
        String compileUrl = null;
        //Разбивка на http запросы ищет все ссылки в документе
        for(int i = 1;i < ads.length;i++){
            compileUrl = "http" + ads[i].split("[\"'><; ]")[0];
            //Проверка на валидные урл
            if(SupportMain.checkValidUrl(compileUrl)){
                parseLinks.add(compileUrl);
                mainActivity.analyticLog.setNumLinks(1);
            }
            else {
                parseBreakLinks.add(compileUrl);
                mainActivity.analyticLog.setNumBrokenLinks(1);
            }
        }
        allLinks.add(parseLinks);
        allLinks.add(parseBreakLinks);

        return allLinks;
    }

    public String splitLink(String link){
        String[] nameArr;
        String splitedLink;
        nameArr = link.split("/");
        if (nameArr.length > 1)
            splitedLink = nameArr[2];
        else
            splitedLink = nameArr[0];
        return splitedLink;
    }

    @SuppressLint("SimpleDateFormat")
    public String getDate(String type){
        Date date = null;
        SimpleDateFormat formatter = null;
        switch (type){
            case "time":
                formatter= new SimpleDateFormat("HH-mm");
                break;
            case "date":
                formatter= new SimpleDateFormat("dd-MM-yyyy");
                break;
        }
        date = new Date(System.currentTimeMillis());
        formatter.format(date);

        return formatter.format(date);
    }

}