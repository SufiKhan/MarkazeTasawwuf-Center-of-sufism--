package com.sufism.markazetasawwuf.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.sufism.markazetasawwuf.R;
import com.sufism.markazetasawwuf.Service.ImageDisplayerClass;

import java.util.ArrayList;



public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<CommonObject> photos;
    public ImageAdapter(Context c, ArrayList<CommonObject> photos) {
        mContext = c;
        mInflater = LayoutInflater.from(c);
        this.photos = photos;
    }

    public int getCount() {
        return photos.size();
    }

    public Object getItem(int position) {
        return photos.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.photosrow, null);
            imageView = (ImageView)convertView.findViewById(R.id.image_pic);
            // if it's not recycled, initialize some attributes
        } else {
            imageView = (ImageView) convertView;
        }
        new ImageDisplayerClass().getImage(imageView,photos.get(position).photoId,mContext);
        return imageView;
    }
//    private void getThumbnail(final ImageView _imageViewPhoto,String photoID) {
//        String url = Constants.BLOBURL+photoID+Constants.DOWNLOAD;
//        Map<String, String> params = new HashMap<>();
//        params.put("QB-Token", MyApplication.getSessionToken());
//        ImageLoader imgLoader = ImageLoader.getInstance();
//        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
//                .cacheOnDisk(true)
//                .extraForDownloader(params)
//                .imageScaleType(ImageScaleType.NONE_SAFE)
//                .displayer(new FadeInBitmapDisplayer(300)).build();
//
//        if(imgLoader.isInited()){
//            ImageLoader imageLoader = ImageLoader.getInstance();
//            imageLoader.loadImage(url,new SimpleImageLoadingListener(){
//                @Override
//                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                    super.onLoadingComplete(imageUri, view, loadedImage);
//                    _imageViewPhoto.setImageBitmap(loadedImage);
//                }
//            });
//        }else{
//            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(mContext)
//                    .defaultDisplayImageOptions(defaultOptions)
//                    .memoryCache(new WeakMemoryCache())
//                    .diskCacheSize(100 * 1024 * 1024).build();
//            ImageLoader.getInstance().init(config);
//            imgLoader.loadImage(url,new SimpleImageLoadingListener(){
//                @Override
//                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
//                    super.onLoadingComplete(imageUri, view, loadedImage);
//                    _imageViewPhoto.setImageBitmap(loadedImage);
//                }
//            });
//        }
//    }

}

