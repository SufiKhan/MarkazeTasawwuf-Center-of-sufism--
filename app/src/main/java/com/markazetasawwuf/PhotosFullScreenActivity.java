package com.markazetasawwuf;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.markazetasawwuf.Adapter.CommonObject;
import com.markazetasawwuf.Adapter.CustomPagerAdapter;
import com.markazetasawwuf.Service.ZoomOutPageTransformer;
import com.markazetasawwuf.database.DBManager;
import com.markazetasawwuf.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.Collections;


public class PhotosFullScreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewpager);
        Bundle bundle;
        if (savedInstanceState == null) {
            bundle = getIntent().getExtras();
            if (bundle!=null) {
                int position = bundle.getInt(Constants.POSITION);
                getDataFromDB(position);
            }
        }
    }
    private void getDataFromDB(int position){
        DBManager dbManager = new DBManager(this);
        dbManager.open();
        Cursor cursor = dbManager.fetchPhotos();
        ArrayList<CommonObject> photoList = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            CommonObject photoObj = new CommonObject();
            photoObj.photoId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.PHOTO_ID));
            photoList.add(photoObj);

        }
        Collections.reverse(photoList);
        dbManager.close();
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new CustomPagerAdapter(this,photoList));
        viewPager.setPageTransformer(true,new ZoomOutPageTransformer());
        viewPager.setCurrentItem(position);
        Button save = (Button)findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView image = (ImageView) viewPager.findViewById(R.id.fullscreenimage);
                image.setDrawingCacheEnabled(true);
                Bitmap bitmap = image.getDrawingCache();
                MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, Constants.APP_NAME+viewPager.getCurrentItem() , "");
                Toast.makeText(getBaseContext(), "Photo saved to gallery", Toast.LENGTH_SHORT).show();

            }
        });

    }
}
