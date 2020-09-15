package com.immo.adseeker;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.immo.adseeker.connect.JSONParser;
import com.immo.adseeker.support.Connect;
import com.immo.adseeker.support.SaveSystem;
import com.immo.adseeker.support.SupportMain;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class SearchFragment extends Fragment {

    protected static int countSearcher = 2;
    //urls для ссылок поисковиков
    private String[] urlsSearchers = new String[countSearcher];

    public String time,date;

    public ArrayList<String> changedUrlsSearchers = new ArrayList<>();
    //Документы полученные из jSoup по результатам поисковиков
    private ArrayList<Document> docsSeached = new ArrayList<>();
    private int numberOfsites;
    //Список всех запросов
    public ArrayList<String> searchTasks = new ArrayList<>();
    //Для отправки в бд время_запрос
    public String searchTask;

    private Spinner spinnerServers, spinnerNumberOfsites, spinnerDeepParse;
    private ArrayList<String> servers = new ArrayList<>();
    private ArrayList<String> numbers = new ArrayList<>();
    private ArrayList<String> deepParseNumbers = new ArrayList<>();

    private TextView searchKeyWords;
    private CheckBox googleSearchCB;
    private CheckBox yandexSearchCB;
    CheckBox htmlPropertyCB;
    CheckBox bannersPropertyCB;
    CheckBox redirectPropertyCB;
    CheckBox wordsPropertyCB;
    CheckBox dontParsCB,manualParsCB;


    private EditText editText,editText2;
    public EditText etPhoneNum,etOperator;
    TextView errorPhoneOp;

    private MainActivity mainActivity;

    int howManyLinksToCheck;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Collections.addAll(numbers, "10", "20", "40", "50", "100");
        Collections.addAll(deepParseNumbers, "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20");
        Collections.addAll(servers, "Stable adseeker.imb2bs.com", "Dev localhost");
        mainActivity = ((MainActivity) getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Вызывается при первом отображении на экране фрагмента
        //Если нужно что-то вывести до отображения, то выдаст нулл
        View view = inflater.inflate(R.layout.search_fragment, container, false);
        spinnerServers = view.findViewById(R.id.spinner_servers);
        spinnerNumberOfsites = view.findViewById(R.id.spinner_numberOfsites);
        spinnerDeepParse = view.findViewById(R.id.spinner_deepParse);

        searchKeyWords = view.findViewById(R.id.searchKeyWords);
        googleSearchCB = view.findViewById(R.id.searchCBgoogle);
        yandexSearchCB = view.findViewById(R.id.searchCByandex);
        htmlPropertyCB = view.findViewById(R.id.searchCBhtml);
        bannersPropertyCB = view.findViewById(R.id.searchCBbanners);
        redirectPropertyCB = view.findViewById(R.id.searchCBredirect);
        wordsPropertyCB = view.findViewById(R.id.searchCBwords);
        dontParsCB = view.findViewById(R.id.dontParse);
        manualParsCB = view.findViewById(R.id.manualParse);

        Button searchBtn = view.findViewById(R.id.search_btn);
        //Button searchJSONBtn = view.findViewById(R.id.searchJSON_btn);
        editText = view.findViewById(R.id.search_ask1);
        editText2 = view.findViewById(R.id.search_ask2);
        etPhoneNum = view.findViewById(R.id.etNumber);
        etOperator = view.findViewById(R.id.etOperator);
        errorPhoneOp = view.findViewById(R.id.search_ErrorText);
        errorPhoneOp.setVisibility(View.INVISIBLE);

        loadData();

        SupportMain.setSpinner(getActivity(), spinnerServers, servers);
        SupportMain.setSpinner(getActivity(), spinnerNumberOfsites, numbers);
        SupportMain.setSpinner(getActivity(), spinnerDeepParse, deepParseNumbers);

        //Обработка нажатия кнопки поиска на клавиатуре или на экране
        searchBtn.setOnClickListener(v -> {
            searchTask = null;
            newDate();
            if(etPhoneNum.getText().length() > 0 || etOperator.getText().length() > 0){

                saveData();
                if(manualParsCB.isChecked()) {
                    if (editText.getText().toString().length() > 0 || editText2.getText().toString().length() > 0) {
                        setOptions(false);
                    }
                }
                else{
                    new Connect(mainActivity,String.valueOf(spinnerServers.getSelectedItem()).split(" ")[0]);
                    new JSONParser(this).execute(mainActivity.urlJson);
                }
                clear();
            }
            else{
                errorPhoneOp.setVisibility(View.VISIBLE);
            }
        });
        return view;
    }

    public void setOptions(boolean json){
        newSession();
        if(json){
            getJsonOptions();
        }
        else {
            getAppOptions();
        }
        startSearch();
    }

    public void startSearch() {
        //Сбрасывает поток, иначе преведет к исключению
        mainActivity.runOnUiThread(() -> {
            nextTask();
            if (searchTask == null) {
                generateSearchTask();
            }
            //https://www.google.com/search?q= запрос &num= кол-во выдачи макс 100 &start= 10 - c какого по счету запроса выводит результат
            //https://yandex.ru/search/?text= запрос &p= номер стр (кол-во выдачи не нашел у яндекса, можно зациклить сколько стр надо вывести)
            try {
                urlsSearchers[0] = "https://www.google.com/search?q=" + searchTasks.get(0);
                urlsSearchers[1] = "https://yandex.ru/search/?text=" + searchTasks.get(0);
            }
            catch (Exception e){
                Log.e("getUrlsSearchers","exception");
            }
            if(searchTasks.size() != 0){
                mainActivity.logCollector.add("Оператор  |  Номер  |  Откуда  |  Куда  |  Время  |  Комментарий");
                mainActivity.logCollector.add(" ");
            }

            //Переходит на страницу ссылок
            mainActivity.btns[1].performClick();
            //Проверка на отмеченные поисковики и генерация запросов на каждую страницу
            int countPage = 0;
            if(googleSearchCB.isChecked()){
                for(int i = numberOfsites;i > 0;i -= 10){
                    changedUrlsSearchers.add(urlsSearchers[0] + "&start=" + countPage * 10);
                    countPage++;
                }
            }
            countPage = 0;
            if(yandexSearchCB.isChecked()){
                for(int i = numberOfsites;i > 0;i -= 10){
                    changedUrlsSearchers.add(urlsSearchers[1] + "&p=" + countPage);
                    countPage++;
                }
            }
            //Поисковики в которых нужно проводить запросы
            AsyncSearchLinks asyncSearch = new AsyncSearchLinks();
            asyncSearch.execute();
            mainActivity.closeKeyboard();
        });
    }

    private void generateSearchTask(){
        searchTask = time + "_" + searchTasks.get(0);
    }

    public void clear(){
        new JSONParser().clear();
        searchTasks.clear();
        editText.setText("");
        editText2.setText("");
        JSONParser.keyWords.clear();
        searchKeyWords.setText("");
    }

    private void saveData(){
        String[] saveData = new String[2];
        saveData[0] = etPhoneNum.getText().toString();
        saveData[1] = etOperator.getText().toString();
        SaveSystem.saveData(Objects.requireNonNull(this.getActivity()), saveData);
    }

    private void loadData(){
        String[] saveData = SaveSystem.loadData(Objects.requireNonNull(this.getActivity()));
        if(saveData.length > 0){
            etPhoneNum.setText(saveData[0]);
            etOperator.setText(saveData[1]);
        }
    }

    private void newSession(){
        nextTask();
        searchTasks.clear();
        mainActivity.clearInfoNextSession();
        mainActivity.logsSent = false;
        mainActivity.saveServerDir = null;
    }

    private void nextTask(){
        mainActivity.clearInfoNextSite();
        docsSeached.clear();
        changedUrlsSearchers.clear();
    }

    private void getJsonOptions(){
        searchTasks.addAll(JSONParser.searchTasks);

        if(searchTasks.size() > 0){
            editText.setText(searchTasks.get(0));
        }
        if(searchTasks.size() > 1){
            editText2.setText(searchTasks.get(1));
        }

        if(JSONParser.googleSearch) googleSearchCB.setChecked(true);
        else googleSearchCB.setChecked(false);

        if(JSONParser.yandexSearch) yandexSearchCB.setChecked(true);
        else yandexSearchCB.setChecked(false);

        if(JSONParser.checkHTML) htmlPropertyCB.setChecked(true);
        else  htmlPropertyCB.setChecked(false);

        if(JSONParser.checkBanners) bannersPropertyCB.setChecked(true);
        else  bannersPropertyCB.setChecked(false);

        if(JSONParser.checkRedirect) redirectPropertyCB.setChecked(true);
        else  redirectPropertyCB.setChecked(false);

        if(JSONParser.checkWords) wordsPropertyCB.setChecked(true);
        else  wordsPropertyCB.setChecked(false);

        numberOfsites = JSONParser.numberOfSites;
        for (int i=0;i<spinnerNumberOfsites.getCount();i++){
            if (spinnerNumberOfsites.getItemAtPosition(i).toString().equalsIgnoreCase(String.valueOf(numberOfsites))){
                spinnerNumberOfsites.setSelection(i);
                break;
            }
        }
        howManyLinksToCheck = JSONParser.deepParse;
        for (int i=0;i<spinnerDeepParse.getCount();i++){
            if (spinnerDeepParse.getItemAtPosition(i).toString().equalsIgnoreCase(String.valueOf(howManyLinksToCheck))){
                spinnerDeepParse.setSelection(i);
                break;
            }
        }
        if(JSONParser.keyWords.size() > 0){
            String data = getResources().getString(R.string.search_words);
            for(String word : JSONParser.keyWords){
                data += " " + word + ", ";
            }
            searchKeyWords.setText(data);
        }


    }
    private void getAppOptions(){
        if(editText.getText().toString().length() > 0)searchTasks.add(editText.getText().toString());
        if(editText2.getText().toString().length() > 0)searchTasks.add(editText2.getText().toString());

        numberOfsites = Integer.parseInt((String) spinnerNumberOfsites.getSelectedItem());
        howManyLinksToCheck = Integer.parseInt((String) spinnerDeepParse.getSelectedItem());
    }

    private void newDate(){
        time = new SupportMain().getDate("time");
        date = new SupportMain().getDate("date");
    }

    //Асинхронный обработчик запроса ссылок из поисковиков
    @SuppressLint("StaticFieldLeak")
    private class AsyncSearchLinks  extends AsyncTask<String, Void, Void> {
        //Функция в процессе работы
        //Здесь нельзя выводить что либо в интрефейс!Все либо сначала работы, либо в конце
        @Override
        protected Void doInBackground(String... searchers) {
            for(String searcher: changedUrlsSearchers ){
                try {
                    Log.i("Search Task", searcher);
                    Connection connection = Jsoup.connect(searcher);
                    connection.timeout(30 * 1000);
                    docsSeached.add(connection.get());
                } catch (IOException e) {
                    Log.e("Jsoup Search Task Error", e.toString());
                    cancel(true);
                }
            }
            return null;
        }
        //Функция в конце выполнения обработчика
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //Выводит ссылки парся html код страницы
            mainActivity.linksFragment.displayLinks(docsSeached);
        }
        //Прерывание потока
        @Override
        protected void onCancelled() {
            super.onCancelled();
            Toast.makeText(getActivity(),getResources().getString(R.string.search_error_jsoup),Toast.LENGTH_LONG).show();
        }

    }
}
