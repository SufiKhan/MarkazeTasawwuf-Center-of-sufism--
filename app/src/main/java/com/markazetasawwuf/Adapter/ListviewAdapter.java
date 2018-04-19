package com.markazetasawwuf.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.markazetasawwuf.Constants;
import com.markazetasawwuf.MyApplication;
import com.markazetasawwuf.R;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ListviewAdapter extends BaseAdapter {
    private static ArrayList<Taleem> taleem;
    private LayoutInflater mInflater;
    private Boolean downloadThumb;
    private Context ctx;
    public ListviewAdapter(Context context, ArrayList<Taleem> results,Boolean isDownload){
        taleem = results;
        mInflater = LayoutInflater.from(context);
        downloadThumb = true;
        ctx = context;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return taleem.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return taleem.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
       final ViewHolder holder;
        if(view == null){
            view = mInflater.inflate(R.layout.row, null);
            holder = new ViewHolder();
            holder.txtTeaching = (TextView) view.findViewById(R.id.teaching);
            holder.txtTeacher = (TextView) view.findViewById(R.id.teacher);
            holder.icon = (ImageView)view.findViewById(R.id.imageView);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        holder.txtTeaching.setText(taleem.get(i).teaching);
        holder.txtTeacher.setText(taleem.get(i).teacher);
        MyApplication.getContext().setPTFont(holder.txtTeacher);
        MyApplication.getContext().setPTFont(holder.txtTeaching);
        if(taleem.get(i).res != 0){
            if(downloadThumb && i > 1){
                getThumbnail(String.valueOf(taleem.get(i).res) , new bookThumbnailCallback() {
                    @Override
                    public void onSuccess(Bitmap image) {
                        holder.icon.setImageBitmap(image);
                    }
                });
            }
            else {
                holder.icon.setImageResource(taleem.get(i).res);
            }
        }
        return view;
    }
    private void getThumbnail(String thumbnailID, final bookThumbnailCallback callback) {
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
    static class ViewHolder{
        TextView txtTeaching, txtTeacher;
        ImageView icon;
    }
}

interface bookThumbnailCallback{
    void onSuccess(Bitmap image);
}