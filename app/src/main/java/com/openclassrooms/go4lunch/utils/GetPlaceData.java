package com.openclassrooms.go4lunch.utils;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.RequestManager;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;
import com.openclassrooms.go4lunch.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static android.support.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread;
import static com.openclassrooms.go4lunch.controllers.fragments.RestaurantsListFragment.mGoogleApiClient;

/**
 * Class used to retrieve data of a Place object and in particular here its cover photo
 */
public class GetPlaceData extends AsyncTask<Object, String, String> {

    private String googlePlacesData;
    private String placeId;
    @SuppressWarnings("FieldCanBeLocal")
    private String url;

    public interface AsyncResponse {
        void processFinish(String output);
    }

    private AsyncResponse delegate;

    public GetPlaceData(AsyncResponse delegate){
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
            delegate.processFinish(placeId);
        }
        else {
            delegate.processFinish(null);
        }
    }

    private void getData(List<HashMap<String, String>> placeList)
    {
        if (placeList.size() > 0) {
            HashMap<String, String> googlePlace = placeList.get(0);

            String id = googlePlace.get("id");

            Log.d("dataPlace", "ID RETRIEVED : " + id);

            placeId = id;
        }
    }

    /**
     * Method to retrieve the photo of a given restaurant
     * @param id Place id
     * @param iv ImageView holder
     * @param glideP RequestManager
     */
    public static void getPhotos(String id, ImageView iv, RequestManager glideP) {
        new AsyncTask<Void,Void,Void>(){
            Bitmap image;
            Boolean exist = false;
            @Override
            protected Void doInBackground(Void... params) {

                PlacePhotoMetadataResult result = Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, id).await();
                if (result.getStatus().isSuccess()) {
                    PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();
                    if (photoMetadataBuffer.getCount() > 0) {
                        PlacePhotoMetadata photo = photoMetadataBuffer.get(0);

                        image = photo.getScaledPhoto(mGoogleApiClient,400,400).await()
                                .getBitmap();

                        Log.d("Bitmap", String.valueOf(image));
                        exist = true;
                    }
                    photoMetadataBuffer.release();
                }
                else {
                    try {
                        runOnUiThread(() -> iv.setImageResource(R.drawable.background_image_r));
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                if(exist) {
                    glideP.load(image).into(iv);
                    super.onPostExecute(aVoid);
                }
                else{
                    try {
                        runOnUiThread(() -> iv.setImageResource(R.drawable.background_image_r));
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
            }
        }.execute();
    }
}
