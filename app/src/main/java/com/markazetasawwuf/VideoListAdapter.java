package com.markazetasawwuf;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.markazetasawwuf.Adapter.CommonObject;
import com.markazetasawwuf.Service.DownloadVideosTask;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by sierrasolutionsmacuser5 on 24/1/17.
 */
public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.ViewHolder> {
    private static ArrayList<CommonObject> video;
    private Activity ctx;


  public class ViewHolder extends RecyclerView.ViewHolder {
       public TextView name;
      public CardView card;
        public  Button download;
        public  ImageView thumbnail;
        public ViewHolder(View itemView) {
            super(itemView);
             name = (TextView)itemView.findViewById(R.id.info_text);
             thumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
             download = (Button) itemView.findViewById(R.id.download);
             card = (CardView)itemView.findViewById(R.id.card_view);
            ViewGroup.LayoutParams lp = card.getLayoutParams();
            DisplayMetrics dm = new DisplayMetrics();
            ctx.getWindowManager().getDefaultDisplay().getMetrics(dm);
            lp.width = dm.widthPixels / 2;
            lp.height = lp.width;
            card.setLayoutParams(lp);

        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public VideoListAdapter(ArrayList<CommonObject> myDataset, Activity mContext) {
        video = myDataset;
        ctx = mContext;
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public VideoListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        // create a new view
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.videorow, parent, false);

        return new ViewHolder(itemView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.name.setText(video.get(position).name);
        holder.name.setSelected(true);
        MyApplication.getContext().setPTFont(holder.name);
        holder.name.setBackgroundColor(Color.parseColor(Constants.Color2));
        getThumbnail(video.get(position).thumbnailId, new ThumbnailCallback() {
            @Override
            public void onSuccess(Bitmap image) {
                holder.thumbnail.setImageBitmap(image);
            }
        });
        File path = new File(Environment.getExternalStorageDirectory() + File.separator + Constants.APP_NAME);
        final File file = new File(path, video.get(position).name);
        if(file.exists()){
            holder.download.setText(R.string.Play);
        }
        else {
            holder.download.setText(R.string.Download);
        }
        holder.download.setTypeface(MyApplication.getContext().getTipoFont());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playOrDownloadVideo(file,position);
            }
        });
        holder.download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                playOrDownloadVideo(file,position);
            }
        });

    }

    public void playOrDownloadVideo(File file,int position){
        if(!file.exists()){
            if(MyApplication.isNetworkAvailble(ctx)){
                final ArrayList<String> params = new ArrayList<String>();
                String url = Constants.BLOBURL+video.get(position).videoId+ Constants.DOWNLOAD;
                params.add(url);
                params.add(video.get(position).name);
                if(MyApplication.getSessionToken() == null) {
                    MyApplication.getContext().getSession(new MTCallback() {
                        @Override
                        public void onSuccess(Boolean success) {
                            downloadVideoWithParams(params);
                        }
                    });

                }else{
                    downloadVideoWithParams(params);
                }
            }
            else{
                MyApplication.getContext().showCustomProgress(ctx,R.drawable.error, Constants.NO_INTERNET);
                MyApplication.getContext().scheduleDismiss();
            }
        }else{
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(file), "video/mp4");
            ctx.startActivity(intent);
        }
    }
    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return video.size();
    }

    private void getThumbnail(String thumbnailID, final ThumbnailCallback callback) {
        String url = Constants.BLOBURL+thumbnailID+ Constants.DOWNLOAD;
        Map<String, String> params = new HashMap<>();
        params.put("QB-Token", MyApplication.getSessionToken());
        ImageLoader imgLoader = ImageLoader.getInstance();
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .extraForDownloader(params)
                .imageScaleType(ImageScaleType.NONE_SAFE).showImageOnLoading(R.drawable.icn_3)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        if(imgLoader.isInited()){
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.loadImage(url,new SimpleImageLoadingListener(){
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    super.onLoadingComplete(imageUri, view, loadedImage);
                    callback.onSuccess(loadedImage);
                }
            });
        }else{
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(ctx)
                    .defaultDisplayImageOptions(defaultOptions)
                    .memoryCache(new WeakMemoryCache())
                    .diskCacheSize(100 * 1024 * 1024).build();
            ImageLoader.getInstance().init(config);
            imgLoader.loadImage(url,new SimpleImageLoadingListener(){
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    super.onLoadingComplete(imageUri, view, loadedImage);
                    callback.onSuccess(loadedImage);
                }
            });
        }
    }
    private void downloadVideoWithParams(ArrayList<String> params) {
        new DownloadVideosTask(ctx,this).execute(params);
    }


}

interface ThumbnailCallback{
   void onSuccess(Bitmap image);
}
