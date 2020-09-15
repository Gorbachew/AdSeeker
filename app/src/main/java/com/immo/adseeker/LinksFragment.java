package com.immo.adseeker;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.immo.adseeker.R;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class LinksFragment extends Fragment {
    //Сколько ссылок будет проверять на основной странице
    public String parsingLink;
    //Список всех найденных сайтов
    public ArrayList<ArrayList<String>> urlsSites = new ArrayList<>();
    //Список рандомных ссылок для проверки
    public ArrayList<String> randomLinksFromMineSiteForParse = new ArrayList<>();
    private LinearLayout parent;
    private MainActivity mainActivity;
    public int numberPage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = ((MainActivity)getActivity());
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(com.immo.adseeker.R.layout.links_fragment, container, false);
        parent = view.findViewById(R.id.links_place);
        return view;
    }
    //Парс полученного документа от поисковика, вывод информации на экран
    @SuppressLint("SetTextI18n")
    public void displayLinks(ArrayList<Document> docs) {
        String title = "";
        parent.removeAllViews();
        if(docs.size() != 0){
            for (Document doc : docs) {
                int countLinks = 0;

                ArrayList<String> urlSearch = new ArrayList<>();

                Elements links = doc.select("a[href]");
                LayoutInflater ltInflater = getLayoutInflater();
                //Заголовок перед выдачей и его настройки
                View vHead = ltInflater.inflate(R.layout.link_template, parent, false);
                vHead.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                TextView tvLinkHead = vHead.findViewById(R.id.link);
                TextView tvHeaderHead = vHead.findViewById(R.id.header);
                //Если новый заголовок то отсчет страниц обнуляется
                if(!title.equals(doc.title())){
                    title = doc.title();
                    numberPage = 1;
                }
                else numberPage++;
                tvLinkHead.setText(title);
                tvLinkHead.setGravity(Gravity.CENTER);
                tvLinkHead.setTextSize(20);
                tvLinkHead.setTextColor(getResources().getColor(R.color.colorWhite));

                parent.addView(vHead);
                //Выводит результаты на экран
                for (Element link : links) {
                    //Тело вывода
                    if ((link.parent().parent().parent().hasClass("serp-item") && !link.parent().parent().hasClass("recommendations")) || //Фильтр яндекса на вывод результатов запроса
                            (link.children().hasClass("LC20lb"))) { //Фильтр гугла на вывод результатов запроса
                        //Тело вывода
                        View v = ltInflater.inflate(R.layout.link_template, parent, false);
                        TextView tvLink = v.findViewById(R.id.link);
                        String urlStr = link.absUrl("href");

                        urlSearch.add(urlStr);

                        tvLink.setText(urlStr);
                        TextView tvHeader = v.findViewById(R.id.header);
                        tvHeader.setText(link.text());
                        parent.addView(v);
                        countLinks++;
                    }
                }
                Log.i("Search sites", String.valueOf(urlSearch.size()));
                urlsSites.add(urlSearch);

                tvHeaderHead.setText(getResources().getString(R.string.html_text1) + numberPage + " " + getResources().getString(R.string.html_text0) + " " + urlsSites.get(urlsSites.size() - 1).size());
                tvHeaderHead.setTextColor(getResources().getColor(R.color.colorWhite));
            }
            //После завершения вывода ссылок
            //Запускает парсер всех сайтов, что выдал поисковик
            for(ArrayList<String> urls : urlsSites){
                for(String site : urls){
                    Log.i("site ",site);
                }
            }
            if(urlsSites.size() > 0 && !mainActivity.searchFragment.dontParsCB.isChecked()){

                parsingLink = urlsSites.get(0).get(0);
                urlsSites.get(0).remove(0);
                startAsync(parsingLink);

                Log.i("Start Parse", "Count sites: " + urlsSites.size());
            }
        }
        else {
            Toast.makeText(getActivity(),getResources().getString(R.string.html_error0),Toast.LENGTH_LONG).show();
        }
    }

    public void clearInfo(){
        urlsSites.clear();
        parent.removeAllViews();
    }


    public void startAsync(String task){

        //Переходит на страницу браузера
        mainActivity.btns[2].performClick();
        //Отображает в браузере страницу по урл
        mainActivity.browserFragment.displayPage(task);
    }

}
