package com.immo.adseeker.filters;

import android.util.Log;

import com.immo.adseeker.MainActivity;

import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;

public class wordsFilter {


    public int searchWord(MainActivity mainActivity, String data, ArrayList<String> words){

        int foundWords = 0;
        int[] countWords = new int[words.size()];
        String result = "";

        for(int i = 0; i < words.size(); i++){
            countWords[i] = StringUtils.countMatches(data,words.get(i));
            foundWords += countWords[i];
            result += words.get(i) + ":" + countWords[i] + ">";
        }
        Log.i("wordFound",result);
        mainActivity.analyticLog.setKeyWords(result);
        return foundWords;
    }
}
