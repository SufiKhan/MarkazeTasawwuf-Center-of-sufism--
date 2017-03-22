package com.sufism.markazetasawwuf.Adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sufism.markazetasawwuf.R;
import com.sufism.markazetasawwuf.Service.ImageDisplayerClass;

import java.util.ArrayList;



public class CustomPagerAdapter extends PagerAdapter {

    private Context mContext;
    private ArrayList<CommonObject> photos;
    public CustomPagerAdapter(Context context, ArrayList<CommonObject> photosList) {
        mContext = context;
        photos = photosList;
    }

    @Override
    public Object instantiateItem(ViewGroup collection, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.photosfullscreen, collection, false);
        showFullScreenImage(layout,position,photos);
        collection.addView(layout);
        return layout;
    }
    private void showFullScreenImage(ViewGroup view, int position, ArrayList<CommonObject> photolist){
        ImageView imageView = (ImageView)view.findViewById(R.id.fullscreenimage);
        new ImageDisplayerClass().getImage(imageView,photolist.get(position).photoId,mContext);
    }
    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {

        return photos.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return photos.get(position).toString();
    }

}
