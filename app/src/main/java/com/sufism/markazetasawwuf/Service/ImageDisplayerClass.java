package com.sufism.markazetasawwuf.Service;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.sufism.markazetasawwuf.Constants;
import com.sufism.markazetasawwuf.MyApplication;

import java.util.HashMap;
import java.util.Map;



/**
 * Created by sierrasolutionsmacuser5 on 17/2/17.
 */

public class ImageDisplayerClass {
    public void getImage(final ImageView _imageViewPhoto, String photoID, Context context) {
        String url = Constants.BLOBURL+photoID+Constants.DOWNLOAD;
        Map<String, String> params = new HashMap<>();
        params.put("QB-Token", MyApplication.getSessionToken());
        com.nostra13.universalimageloader.core.ImageLoader imgLoader = com.nostra13.universalimageloader.core.ImageLoader.getInstance();
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .extraForDownloader(params)
                .imageScaleType(ImageScaleType.NONE_SAFE)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        if(imgLoader.isInited()){
            com.nostra13.universalimageloader.core.ImageLoader imageLoader = com.nostra13.universalimageloader.core.ImageLoader.getInstance();
            imageLoader.loadImage(url,new SimpleImageLoadingListener(){
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    super.onLoadingComplete(imageUri, view, loadedImage);
                    _imageViewPhoto.setImageBitmap(loadedImage);
                }
            });
        }else{
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                    .defaultDisplayImageOptions(defaultOptions)
                    .memoryCache(new WeakMemoryCache())
                    .diskCacheSize(100 * 1024 * 1024).build();
            com.nostra13.universalimageloader.core.ImageLoader.getInstance().init(config);
            imgLoader.loadImage(url,new SimpleImageLoadingListener(){
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    super.onLoadingComplete(imageUri, view, loadedImage);
                    _imageViewPhoto.setImageBitmap(loadedImage);
                }
            });
        }
    }
}
