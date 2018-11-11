package com.openclassrooms.go4lunch.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Class used for reading content from URLs
 */
class DownloadURL {

    /**
     * Read a given URL and return the response of the query as a String object
     * @param myUrl URL
     * @return String object containing the response
     * @throws IOException Exception
     */
    String readUrl(String myUrl) throws IOException
    {
        String data = "";
        InputStream inputStream = null;
        HttpURLConnection urlConnection = null;

        try {
            URL url = new URL(myUrl);
            urlConnection=(HttpURLConnection) url.openConnection();
            urlConnection.connect();

            inputStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();

            String line;
            while((line = br.readLine()) != null)
            {
                sb.append(line);
            }

            data = sb.toString();
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            assert inputStream != null;
            inputStream.close();
            urlConnection.disconnect();
        }
        Log.d("DownloadURL","Returning data= "+data);

        return data;
    }
}
