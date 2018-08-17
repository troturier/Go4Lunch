/* Copyright 2016 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For additional information, contact:
 * Environmental Systems Research Institute, Inc.
 * Attn: Contracts Dept
 * 380 New York Street
 * Redlands, California, USA 92373
 *
 * email: contracts@esri.com
 *
 */

package com.openclassrooms.go4lunch.places;

import android.location.Location;

import com.esri.arcgisruntime.geometry.Envelope;
import com.openclassrooms.go4lunch.BasePresenter;
import com.openclassrooms.go4lunch.BaseView;
import com.openclassrooms.go4lunch.data.Place;

import java.util.List;


public interface PlacesContract {

  interface View extends BaseView<Presenter> {

    void showNearbyPlaces(List<Place> places);

    void showProgressIndicator(String message);

    void showMessage(String message);


  }

  interface Presenter extends BasePresenter {

    void setPlacesNearby(List<Place> places);

    void setLocation(Location location);

    void getPlacesNearby();

    Envelope getExtentForNearbyPlaces();

  }
}
