package com.openclassrooms.go4lunch.utils;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import com.openclassrooms.go4lunch.models.Restaurant;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Class containing useful tools to avoid code duplication
 */
public class Toolbox {

    /**
     * Retrieve the current date of the system in the yyyy-MM-dd format
     * @return Formatted string of the date
     */
    public static String getCurrentDate(){
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return format.format(date);
    }

    /**
     * Give a LatLngBounds object from a LatLng one with a radius of 10000 meters
     * @param center LatLng object
     * @return LatLngBounds object
     */
    public static LatLngBounds toBounds(LatLng center) {
        double distanceFromCenterToCorner = (double) 10000 * Math.sqrt(2.0);
        LatLng southwestCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0);
        LatLng northeastCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0);
        return new LatLngBounds(southwestCorner, northeastCorner);
    }

    /**
     * Finds all duplicate objects in a list and returns them as another list
     * @param listContainingDuplicates List containing duplicate Restaurant objects
     * @return List of all duplicate objects
     */
    public static List<Restaurant> findDuplicates(List<Restaurant> listContainingDuplicates) {

        final Set<Restaurant> setToReturn = new HashSet<>();
        final Set<Restaurant> set1 = new HashSet<>();

        for (Restaurant restaurant : listContainingDuplicates) {
            if (!set1.add(restaurant)) {
                setToReturn.add(restaurant);
            }
        }
        List<Restaurant> restaurantList = new ArrayList<>(setToReturn);
        Collections.sort(restaurantList, (o1, o2) -> o1.getDistance().compareTo(o2.getDistance()));
        return restaurantList;
    }
}
