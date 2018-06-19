package co.aerobotics.android.data;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import co.aerobotics.android.R;

public class CSVReader {

    private Context context;
    private String csvFile;
    private String line = "";
    private String cvsSplitBy = ",";
    private List<co.aerobotics.android.data.Location> locations = new ArrayList<>();

    public CSVReader(Context context, String csvFile){
        this.context = context;
        this.csvFile = csvFile;
    }

    public CSVReader(Context context){
        this.context = context;
    }

    public void readFile(){
        // The file that this method reads is hardcoded to read the R.raw.test(2) file.
        InputStream is = context.getResources().openRawResource(R.raw.test2);
        BufferedReader br = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));

        try{
            while ((line = br.readLine()) != null) {
                String[] location = line.split(cvsSplitBy);
                locations.add(new co.aerobotics.android.data.Location(Double.valueOf(location[0]), Double.valueOf(location[1]), Double.valueOf(location[2])));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public co.aerobotics.android.data.Location getLocation(int i){
        return locations.get(i);
    }

    public Double getAlt(double lon, double lat){
        double alt = 0; boolean y,z;
        for(co.aerobotics.android.data.Location x: locations ){
            y = false; z = false;
            if(x.isLatWithinScope(lat)) y = true;
            if(x.isLongWithinScope(lon)) z = true;
            if(y&&z) return x.getalt();
        }
        return alt;
    }

}
