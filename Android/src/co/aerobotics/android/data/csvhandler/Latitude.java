package co.aerobotics.android.data.csvhandler;

public class Latitude {

    final private double LAT_DISTANCE = 0.000009011160749139;
    private double latitude;

    public Latitude(double lat){
        latitude = lat;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getMinScope(){
        return getLatitude() - (LAT_DISTANCE/2);
    }

    public double getMaxScope(){
        return getLatitude() + (LAT_DISTANCE/2);
    }

}
