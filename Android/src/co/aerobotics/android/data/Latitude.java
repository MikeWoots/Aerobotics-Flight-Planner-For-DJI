package co.aerobotics.android.data;

public class Latitude {

    private double latitude;
    final private double LAT_DISTANCE = 0.000009011160749139;

    public Latitude(double lat){
        latitude = lat;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLatitudeDistance(){
        return LAT_DISTANCE;
    }

    public double getMinScope(){
        return getLatitude() - (LAT_DISTANCE/2);
    }

    public double getMaxScope(){
        return getLatitude() + (LAT_DISTANCE/2);
    }

    //SET LAT DISTANCE

}
