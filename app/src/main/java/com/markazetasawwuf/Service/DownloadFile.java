package com.markazetasawwuf.Service;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.BaseAdapter;

import com.markazetasawwuf.Constants;
import com.markazetasawwuf.MyApplication;
import com.markazetasawwuf.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


public class DownloadFile extends AsyncTask<ArrayList<String>, Integer, String> {

    private Activity mContext;
    private BaseAdapter mAdapter;

    public DownloadFile(Activity context, BaseAdapter adapter) {
        mContext = context;
        mAdapter = adapter;
    }
    /**
     * Before starting background thread
     * */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        MyApplication.getContext().showDeterminate(mContext,0);
    }

    /**
     * Downloading file in background thread
     * */
    @Override
    protected String doInBackground(ArrayList<String>... arrayLists) {
        int count;
        try {
            File path = new File(Environment.getExternalStorageDirectory() + File.separator + Constants.APP_NAME);
            //File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
            URL url = new URL(arrayLists[0].get(0));
            URLConnection conection = url.openConnection();
            conection.connect();
            // getting file length
            int lenghtOfFile = conection.getContentLength();
            // input stream to read file - with 8k buffer
            InputStream input = new BufferedInputStream(url.openStream(), 8192);
            // Output stream to write file
            if (!path.exists()) {
                path.mkdir();
            }
            File file = new File(path, arrayLists[0].get(1));
            OutputStream output = new FileOutputStream(file);
            byte data[] = new byte[1024];
            long total = 0;
            while ((count = input.read(data)) != -1) {
                total += count;
                publishProgress((int)(total*100)/lenghtOfFile);
                // writing data to file
                output.write(data, 0, count);

            }

            // flushing output
            output.flush();
            // closing streams
            output.close();
            input.close();

        } catch (Exception e) {
//            Log.e("Error: ", e.getMessage());
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        MyApplication.getContext().setProgress(values[0]);

    }
    /**
     * After completing background task
     * **/
    @Override
    protected void onPostExecute(String file_url) {
        mContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MyApplication.getContext().stopProgress(mContext);
                MyApplication.getContext().showCustomProgress(mContext, R.drawable.complete, Constants.DOWNLOAD_COMPLETE);
                MyApplication.getContext().scheduleDismiss();
                mAdapter.notifyDataSetChanged();
            }
        });

    }
}


