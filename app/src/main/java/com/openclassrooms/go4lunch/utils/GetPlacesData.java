package com.openclassrooms.go4lunch.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class GetPlacesData extends AsyncTask<Object, String, String> {

    private String googlePlacesData;
    private String placeId;
    private Boolean openNow;
    @SuppressWarnings("FieldCanBeLocal")
    private String url;

    public interface AsyncResponse {
        void processFinish(String output, Boolean open);
    }

    private AsyncResponse delegate = null;

    public GetPlacesData(AsyncResponse delegate){
        this.delegate = delegate;
    }

    @Override
    protected String doInBackground(Object... objects){
        url = (String)objects[1];
        placeId = (String)objects[0];
        DownloadURL downloadURL = new DownloadURL();
        try {
            googlePlacesData = downloadURL.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return googlePlacesData;
    }

    @Override
    protected void onPostExecute(String s){

        List<HashMap<String, String>> placeList;
        PlaceDataParser parser = new PlaceDataParser();
        placeList = parser.parse(s);
        Log.d("dataPlace","called parse method");
        getData(placeList);
        if (placeId != null){
            delegate.processFinish(placeId, openNow);
        }
        else {
            delegate.processFinish(null, false);
        }
    }

    private void getData(List<HashMap<String, String>> placeList)
    {
        if (placeList.size() > 0) {
            HashMap<String, String> googlePlace = placeList.get(0);

            String id = googlePlace.get("id");

            Boolean open = Boolean.parseBoolean(googlePlace.get("open_now"));

            Log.d("dataPlace", "ID RETRIEVED : " + id);

            placeId = id;
            openNow = open;
        }
    }
}
