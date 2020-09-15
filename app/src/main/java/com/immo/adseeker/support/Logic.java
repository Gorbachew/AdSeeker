package com.immo.adseeker.support;

import android.util.Log;

import com.immo.adseeker.LinksFragment;
import com.immo.adseeker.MainActivity;
import com.immo.adseeker.SearchFragment;

public class Logic {

    public void NextLink(LinksFragment linksFragment){
        Log.i("EndParse", "next link");
        //Ищет ссылки по странице
        linksFragment.parsingLink = linksFragment.randomLinksFromMineSiteForParse.get(0);
        linksFragment.randomLinksFromMineSiteForParse.remove(0);
        linksFragment.startAsync(linksFragment.parsingLink);
    }

    public void NextSite(MainActivity mainActivity, LinksFragment linksFragment){
        Log.i("EndParse", "next site");
        mainActivity.mainLink = true;
        mainActivity.saveServerDir = null;
        mainActivity.countParseLinks = 0;
        //Ищет следующий сайт
        linksFragment.parsingLink = linksFragment.urlsSites.get(0).get(0);
        linksFragment.urlsSites.get(0).remove(0);

        linksFragment.startAsync(linksFragment.parsingLink);
    }
    public void NextSearcher(MainActivity mainActivity, SearchFragment searchFragment, LinksFragment linksFragment){
        Log.i("EndParse", "next searcher");
        mainActivity.numberServerSite = 0;
        //Переключается на следующий поисковик
        searchFragment.changedUrlsSearchers.remove(0);
        linksFragment.urlsSites.remove(0);
        // linksFragment.parsingLink = linksFragment.urlsSites.get(0).get(0);
        mainActivity.sentFile = true;
        mainActivity.sentScreenshot = true;
        mainActivity.sentBrokenLinks = true;
        mainActivity.sentWorkLinks = true;
        mainActivity.sentAnalytic = true;
        mainActivity.btns[1].performClick();
        mainActivity.logicContinues();
    }
    public void NextTask(MainActivity mainActivity){
        Log.i("EndParse", "next task");
        mainActivity.searchFragment.searchTask = null;
        mainActivity.searchFragment.searchTasks.remove(0);
        if(mainActivity.searchFragment.searchTasks.size() > 0){
            mainActivity.searchFragment.startSearch();
        }
        else{
            mainActivity.logicContinues();
        }

    }
}
