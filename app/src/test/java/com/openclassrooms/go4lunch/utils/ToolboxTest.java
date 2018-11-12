package com.openclassrooms.go4lunch.utils;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.LatLng;
import com.openclassrooms.go4lunch.models.Restaurant;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;

public class ToolboxTest {

    @Test
    public void getCurrentDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String dateToParse = Toolbox.getCurrentDate();
        Date date = new Date();
        try {
            date = format.parse(dateToParse);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String dateParsed = format.format(date);
        assertEquals("Dates are different", dateParsed, dateToParse);
    }

    @Test
    public void toBounds() {
        LatLng latLng = new LatLng(44.837650,-0.579941);
        LatLngBounds latLngBounds = Toolbox.toBounds(latLng);
        assertNotNull("toBounds returned a null object", latLngBounds);
    }

    @Test
    public void findDuplicates() {
        Restaurant res1 = new Restaurant();
        Restaurant res2 = new Restaurant();
        Restaurant res3 = new Restaurant();
        Restaurant res4 = new Restaurant();
        Restaurant res5 = new Restaurant();

        res1.setId("123");
        res2.setId("12345");
        res3.setId("1235667");
        res4.setId("1235532");
        res5.setId("12355324546546");

        List<Restaurant> list1 = new ArrayList<>();
        list1.add(res1);
        list1.add(res2);
        list1.add(res3);
        List<Restaurant> list2 = new ArrayList<>();
        list2.add(res3);
        list2.add(res4);
        list2.add(res5);



        List<Restaurant> listToCompare = new ArrayList<>();
        listToCompare.addAll(list1);
        listToCompare.addAll(list2);

        List<Restaurant> listToBeCompared = new ArrayList<>();
        listToBeCompared.add(res3);


        assertEquals("List are different", listToBeCompared, Toolbox.findDuplicates(listToCompare));
    }
}