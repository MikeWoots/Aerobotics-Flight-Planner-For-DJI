package co.aerobotics.android.data.csvhandler;

public class Longitude {
    final private double LONG_DISTANCE = 0.00001065297146141;
    private double longitude;

    public Longitude(double lon){
        longitude = lon;
    }

    public double getLongitude(){
        return longitude;
    }

    public double getMinScope(){
        return getLongitude() - (LONG_DISTANCE/2);
    }

    public double getMaxScope(){
        return getLongitude() + (LONG_DISTANCE/2);
    }

}
