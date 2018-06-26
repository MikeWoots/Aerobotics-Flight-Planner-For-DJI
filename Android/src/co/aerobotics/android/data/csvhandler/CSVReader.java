package co.aerobotics.android.data.csvhandler;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {
    /*
    When user clicks on farm area, he gets its farm_id and its DEM url to the server
    DownloadCSVTask downloads the csv from this url and saves to sd card. CSVReader takes
    farm_id, finds the downloaded csv and converts it to list of lats,longs, and alts.
    One can now get the altitude of a certain area from this class.
     */
    private Context context;
    private String filename;
    private List<Location> locations = new ArrayList<>();

    //filename can be farm_id
    public CSVReader(Context context, String filename){
        this.context = context;
        this.filename = filename;
        readFile(filename);
    }

    //Download Test is the current csv download file destination
    private void readFile(String filename){
        try {
            File csv = new File(Environment.getExternalStorageDirectory(), "Download Test/" + filename);
            FileInputStream fis = new FileInputStream(csv);

            if(fis!=null){
                BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));

                String line;
                while((line = br.readLine()) != null){
                    String[] location = line.split(",");
                    locations.add(new Location(Double.valueOf(location[0]), Double.valueOf(location[1]), Double.valueOf(location[2])));
                }
            }
        } catch(FileNotFoundException e){

        } catch(IOException e){

        }
    }

    public Double getAlt(double lon, double lat){
        double alt = 0; boolean y,z;
        for(Location x: locations ){
            y = false; z = false;
            if(x.isLatWithinScope(lat)) y = true;
            if(x.isLongWithinScope(lon)) z = true;
            if(y&&z) return x.getalt();
        }
        return alt;
    }

}