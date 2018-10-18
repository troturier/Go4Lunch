package com.openclassrooms.go4lunch.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


class PlaceDataParser {

    private HashMap<String, String> getPlace(JSONObject googlePlaceJson)
    {
        HashMap<String, String> googlePlaceMap = new HashMap<>();

        String id="";
        Boolean openNow = null;

        Log.d("DataParser","jsonobject ="+googlePlaceJson.toString());


        try {
            if(!googlePlaceJson.isNull("place_id")) {
                id = googlePlaceJson.getString("place_id");
            }
            if(!googlePlaceJson.isNull("opening_hours")) {
                JSONObject opening_hours = googlePlaceJson.getJSONObject("opening_hours");
                openNow = opening_hours.getBoolean("open_now");
            }

            googlePlaceMap.put("id", id);
            googlePlaceMap.put("open_now", String.valueOf(openNow));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return googlePlaceMap;

    }
    private List<HashMap<String, String>>getPlaces(JSONArray jsonArray)
    {
        int count = jsonArray.length();
        List<HashMap<String, String>> placelist = new ArrayList<>();
        HashMap<String, String> placeMap;

        for(int i = 0; i<count;i++)
        {
            try {
                placeMap = getPlace((JSONObject) jsonArray.get(i));
                placelist.add(placeMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return placelist;
    }

    List<HashMap<String, String>> parse(String jsonData)
    {
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        Log.d("json data", jsonData);

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getPlaces(Objects.requireNonNull(jsonArray));
    }
}
