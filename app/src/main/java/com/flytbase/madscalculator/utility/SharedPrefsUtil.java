package com.flytbase.madscalculator.utility;

import android.content.Context;
import android.content.SharedPreferences;

import com.flytbase.madscalculator.model.CalculationHistory;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class SharedPrefsUtil {

    public static final String PREF_KEY = "history_list";
    private static final String PREFERENCE_NAME = "com.flytbase.madscalculator.historySharedPreferences";

    public static void saveData(Context context, ArrayList<CalculationHistory> historyList) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(historyList);
        editor.putString(PREF_KEY, json);
        editor.apply();
    }

    public static ArrayList<CalculationHistory> loadData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(PREF_KEY, null);
        Type type = new TypeToken<ArrayList<CalculationHistory>>() {
        }.getType();
        ArrayList<CalculationHistory> historyList = gson.fromJson(json, type);
        ArrayList<CalculationHistory> revHistoryList = new ArrayList<>();
        if (historyList == null) {
            historyList = new ArrayList<>();
        }
        if (historyList.size() > 10) {
            historyList.subList(0, historyList.size() - 10).clear();
        }
        for (int i = historyList.size() - 1; i >= 0; i--) {
            revHistoryList.add(historyList.get(i));
        }
        return revHistoryList;
    }

    public static void clearData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

}