package co.aerobotics.android.data.cvshandler;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadCSVTask {

    private static final String TAG = "Download Task";
    private Context context;

    private String downloadUrl, downloadFileName;
    private ProgressDialog progressDialog;

    public DownloadCSVTask(Context context, String downloadUrl) {
        this.context = context;
        this.downloadUrl = downloadUrl;
        downloadFileName = downloadUrl.substring(downloadUrl.lastIndexOf( '/' ),downloadUrl.length());//Create file name by picking download file name from URL
        //downloadFileName = "csv4.csv";//Create file name by picking download file name from URL

        Log.e(TAG, downloadFileName);
        new DownloadingTask().execute();
    }

    private class DownloadingTask extends AsyncTask<Void, Void, Void>{
        File apkStorage = null;
        File outputFile = null;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Downloading " +  downloadFileName + "...");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void result){
            try{
                if(outputFile != null){
                    progressDialog.dismiss();
                    Toast.makeText(context, "Downloaded successfully", Toast.LENGTH_SHORT);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {}}, 3000);

                    Log.e(TAG, "Download Failed");
                    Toast.makeText(context, "Download Failed", Toast.LENGTH_SHORT);
                }
            } catch (Exception e){
                e.printStackTrace();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {}}, 3000);

                Log.e(TAG, "Download Failed with Exception - " + e.getLocalizedMessage());
                Toast.makeText(context, "Download Failed with Exception - " + e.getLocalizedMessage(), Toast.LENGTH_SHORT);
            }
            super.onPostExecute(result);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try{
                URL url = new URL(downloadUrl);
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.connect();

                //Check if connection is okay
                if (c.getResponseCode() != HttpURLConnection.HTTP_OK){
                   Log.e(TAG, "Server returned HTTP " + c.getResponseCode() + " " + c.getResponseMessage());
                   Toast.makeText(context, "Server returned HTTP " + c.getResponseCode() + " " + c.getResponseMessage() , Toast.LENGTH_LONG);
                }

                //Check if there's an SD card. "/../../Download Test" is the current csv directory
                if(new CheckForSDCard().isSDCardPresent()){
                    apkStorage = new File(Environment.getExternalStorageDirectory() + "/" + "Download Test");
                } else Toast.makeText(context, "No SD Card.", Toast.LENGTH_SHORT);

                outputFile = new File(apkStorage, downloadFileName);

                //Create new file if outputFile doesn't exist to avoid duplicate files
                if(!outputFile.exists()){
                    outputFile.createNewFile();
                    Log.e(TAG, "File Created");
                    Toast.makeText(context, "File created",Toast.LENGTH_SHORT);
                }

                FileOutputStream fos = new FileOutputStream(outputFile);
                InputStream is = c.getInputStream();
                byte[] buffer = new byte[1024];
                int len1 = 0;
                while((len1 = is.read(buffer)) != -1)
                    fos.write(buffer, 0, len1);

                fos.close();
                is.close();

            } catch(Exception e){
                e.printStackTrace();
                outputFile = null;
                Log.e(TAG, "Download Error Exception " + e.getMessage());
                Toast.makeText(context, "Download Error Exception", Toast.LENGTH_SHORT);
            }
            return null;
        }
    }

}
