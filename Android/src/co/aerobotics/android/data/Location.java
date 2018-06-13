package co.aerobotics.android.data;

public class Location {
    private double alt;
    private Longitude longitude;
    private Latitude latitude;

    public Location(double lon, double lat, double alt){
        this.alt = alt;
        longitude = new Longitude(lon);
        latitude = new Latitude(lat);
    }

    public double getlon(){
        return longitude.getLongitude();
    }

    public double getlat(){
        return latitude.getLatitude();
    }

    public boolean isLatWithinScope(double lat){
        if(lat > latitude.getMinScope() && lat < latitude.getMaxScope())
            return true;
        return false;
    }

    public boolean isLongWithinScope(double lon){
        if(lon > longitude.getMinScope() && lon < longitude.getMaxScope())
            return true;
        return false;
    }

    public double getalt(){
        return alt;
    }

}
