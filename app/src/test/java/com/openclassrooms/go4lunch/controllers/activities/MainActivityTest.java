package com.openclassrooms.go4lunch.controllers.activities;

import android.content.Intent;
import android.net.Uri;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.junit.Test;

import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

import static org.junit.Assert.assertNotNull;

public class MainActivityTest {

    @Test
    public void prepareDetailActivity() {
        Place place = new Place() {
            @Override
            public String getId() {
                return null;
            }

            @Override
            public List<Integer> getPlaceTypes() {
                return null;
            }

            @Nullable
            @Override
            public CharSequence getAddress() {
                return null;
            }

            @Override
            public Locale getLocale() {
                return null;
            }

            @Override
            public CharSequence getName() {
                return null;
            }

            @Override
            public LatLng getLatLng() {
                return null;
            }

            @Nullable
            @Override
            public LatLngBounds getViewport() {
                return null;
            }

            @Nullable
            @Override
            public Uri getWebsiteUri() {
                return null;
            }

            @Nullable
            @Override
            public CharSequence getPhoneNumber() {
                return null;
            }

            @Override
            public float getRating() {
                return 0;
            }

            @Override
            public int getPriceLevel() {
                return 0;
            }

            @Nullable
            @Override
            public CharSequence getAttributions() {
                return null;
            }

            @Override
            public Place freeze() {
                return null;
            }

            @Override
            public boolean isDataValid() {
                return false;
            }
        };

        Intent intent = MainActivity.prepareDetailActivity(place);

        assertNotNull(intent);

    }
}