package com.sufism.markazetasawwuf;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.sufism.markazetasawwuf.Adapter.CommonObject;
import com.sufism.markazetasawwuf.Service.DownloadFile;

import java.io.File;
import java.util.ArrayList;



/**
 * Created by sierrasolutionsmacuser5 on 15/2/17.
 */

public class AudioAdapter extends BaseAdapter {
    private static ArrayList<CommonObject> audio;
    private LayoutInflater mInflater;
    private Activity ctx;
    private KProgressHUD hud;
    private String sessionToken;
    private Bitmap bmp;

    public AudioAdapter(Activity context, ArrayList<CommonObject> results){
        audio = results;
        ctx = context;
        mInflater = LayoutInflater.from(context);
    }
    @Override
    public int getCount() {
        return audio.size();
    }

    @Override
    public Object getItem(int i) {
        return audio.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if(view == null){
            view = mInflater.inflate(R.layout.audiorow, null);
            holder = new ViewHolder();
            holder.audioName = (TextView)view.findViewById(R.id.audioname);
            holder.surfacaView = (SurfaceView) view.findViewById(R.id.surfaceView);
            holder.download = (Button)view.findViewById(R.id.downloadAudio);
            view.setTag(holder);
        } else {
            holder = (AudioAdapter.ViewHolder) view.getTag();
        }
        holder.audioName.setTypeface(MyApplication.getContext().getPTFont());
        holder.audioName.setSelected(true);
        holder.audioName.setText(audio.get(i).name);
        if (i % 2 == 1) {
            holder.surfacaView.setBackgroundColor(Color.parseColor(Constants.Color1));
        }else {
            holder.surfacaView.setBackgroundColor(Color.parseColor(Constants.Color2));
        }
        File path = new File(Environment.getExternalStorageDirectory() + File.separator + Constants.APP_NAME);
        final File file = new File(path, audio.get(i).name);
        if(file.exists()){
            holder.download.setText(R.string.Play);
            holder.download.setTextColor(Color.YELLOW);
        }
        else {
            holder.download.setText(R.string.Download);
            holder.download.setTextColor(Color.WHITE);
        }
        holder.download.setTypeface(MyApplication.getContext().getTipoFont());
        holder.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                if(!file.exists()){
                    if(MyApplication.isNetworkAvailble(ctx)){
                        final ArrayList<String> params = new ArrayList<String>();
                        String url = Constants.BLOBURL+audio.get(i).audioId+Constants.DOWNLOAD;
                        params.add(url);
                        params.add(audio.get(i).name);
                        if(MyApplication.getSessionToken() == null) {
                            MyApplication.getContext().getSession(new MTCallback() {
                                @Override
                                public void onSuccess(Boolean success) {
                                    downloadAudioWithParams(params);
                                }
                            });

                        }else{
                            downloadAudioWithParams(params);
                        }
                    }
                    else{
                        MyApplication.getContext().showCustomProgress(ctx,R.drawable.error,Constants.NO_INTERNET);
                        MyApplication.getContext().scheduleDismiss();
                    }
                }else{
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setDataAndType(Uri.fromFile(file), "audio/mp3");
                    ctx.startActivity(intent);
                }

            }
        });
        return view;
    }
    private void downloadAudioWithParams(ArrayList<String> params) {
        new DownloadFile(ctx,this).execute(params);
    }
    static class ViewHolder{
        TextView audioName;
        SurfaceView surfacaView;
        Button download;
    }
}
