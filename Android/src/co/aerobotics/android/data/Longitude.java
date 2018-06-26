package co.aerobotics.android.data;

public class Longitude {

    //final private double LONG_DISTANCE = 0.00001065297146141;
    final private double LONG_DISTANCE = 0.00001088051192966;
    private double longitude;

    public Longitude(double lon){
        longitude = lon;
    }

    public double getLongitude(){
        return longitude;
    }

    public double getLongitudeDistance(){
        return LONG_DISTANCE;
    }

    public double getMinScope(){
        return getLongitude() - (LONG_DISTANCE/2);
    }

    public double getMaxScope(){
        return getLongitude() + (LONG_DISTANCE/2);
    }

    //SET LONG_DISTANCE
}
