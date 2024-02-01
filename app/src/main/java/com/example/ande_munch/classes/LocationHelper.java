package com.example.ande_munch.classes;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class LocationHelper {
    private Context context;

    public LocationHelper(Context context) {
        this.context = context;
    }

    public interface LocationCallback {
        void onLocationResult(double[] formattedLocation);
    }


    public void getCurrentLocation(final LocationCallback locationCallback) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // Check if the app has permission to access the device's location
        if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Get the last known location
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                double[] formattedLocation = formatLocationArray(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                locationCallback.onLocationResult(formattedLocation);
            } else {
                // Request location updates
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        // Remove location updates after the first result
                        locationManager.removeUpdates(this);
                        double[] formattedLocation = formatLocationArray(location.getLatitude(), location.getLongitude());
                        locationCallback.onLocationResult(formattedLocation);
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                    }
                });
            }
        }
    }

    public static class LongLatLon {
        public double Lat;
        public double Lon;

        public LongLatLon(double lat, double lon) {
            this.Lat = lat;
            this.Lon = lon;
        }
    }

    public static LongLatLon convertToLongFormat(String formattedLocation) { //eg: lat lon rn is in "(23.444, 43.333)" so needa extract it
        // Remove spaces
        String cleanLocation = formattedLocation.replace(" ", "");

        // Split the string by comma
        String[] latLonArray = cleanLocation.split(",");

        try {
            // Validate array length
            if (latLonArray.length != 2) {
                throw new IllegalArgumentException("Invalid format for location string: " + formattedLocation);
            }

            // Convert the latitude and longitude to double
            double Lat = Double.parseDouble(latLonArray[0]);
            double Lon = Double.parseDouble(latLonArray[1]);

            return new LongLatLon(Lat, Lon);
        } catch (NumberFormatException e) {
            // Log the problematic string
            System.err.println("Error parsing latitude or longitude: " + formattedLocation);
            // Propagate the exception or handle it as appropriate for your application
            throw new NumberFormatException("Invalid format for latitude or longitude in location string: " + formattedLocation);
        }
    }

    private double[] formatLocationArray(double latitude, double longitude) {
        double[] locationArray = {latitude, longitude};
        return locationArray;
    }
}
