package co.aerobotics.android.data;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class WaypointMissionDebugTxt {

    private Context context;
    private String text_to_save = "";

    public WaypointMissionDebugTxt(Context context){
        this.context = context;
    }


    public void append_to_file(float starting_altitude, float mission_altitude, float csv_altitude_waypoint, float altitude_adjust, float final_altitude, int point){
        String to_append =
                "WayPoint " + point +
                " \nStarting altitude: " + starting_altitude +
                ",\nMission set altitude: " + mission_altitude +
                ",\nCSV altitude at drone simulated lat/lng: " + csv_altitude_waypoint +
                ",\nAltitude should adjust by: " + altitude_adjust +
                ",\nFinal WayPoint Altitude should be: " + final_altitude + "\n\n";
        append_to_file(to_append);
    }

    private void append_to_file(String to_append){
        text_to_save = text_to_save.concat(to_append);
    }

    public void createText(){
        Environment.getExternalStorageState();
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, "drone_debug.txt");
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(text_to_save);
            writer.flush();
            writer.close();
            Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
