package com.sufism.markazetasawwuf;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;

import com.quickblox.auth.session.QBSettings;

import java.util.ArrayList;
import java.util.List;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import yalantis.com.sidemenu.interfaces.Resourceble;
import yalantis.com.sidemenu.interfaces.ScreenShotable;
import yalantis.com.sidemenu.model.SlideMenuItem;
import yalantis.com.sidemenu.util.ViewAnimator;


public class MainActivity extends ActionBarActivity implements ViewAnimator.ViewAnimatorListener {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private List<SlideMenuItem> list = new ArrayList<>();
    private ContentFragment contentFragment;
    private ViewAnimator viewAnimator;
    private Toolbar toolbar;
    private int res = R.drawable.dashboardbg;
    private LinearLayout linearLayout;
    private static Activity mContext;
    private String selectedMenuTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        selectedMenuTitle = Constants.APP_NAME;
        setContentView(R.layout.activity_main);
        contentFragment = ContentFragment.newInstance(R.drawable.dashboardbg);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, contentFragment)
                .commit();
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        linearLayout = (LinearLayout) findViewById(R.id.left_drawer);
        linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();

            }
        });
        setActionBar();
        createMenuList();
        viewAnimator = new ViewAnimator<>(this, list, contentFragment, drawerLayout, this);
        initQuickBlox();
        updateNotificationList();
    }
    private void initQuickBlox(){
        QBSettings.getInstance().init(getApplicationContext(), Constants.APP_ID, Constants.AUTH_KEY, Constants.AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(Constants.ACCOUNT_KEY);
        MyApplication app = MyApplication.getContext();
        if(MyApplication.getSessionToken() == null) {
            app.getSession(new MTCallback() {
                @Override
                public void onSuccess(Boolean success) {

                }
            });
        }
       //getContext().registerReceiver(pushBroadcastReceiver, new IntentFilter(Constants.NEW_PUSH_ALERT));
    }
    @Override
    public void onResume() {
        super.onResume();
        getContext().registerReceiver(pushBroadcastReceiver, new IntentFilter(Constants.NEW_PUSH_ALERT));
    }
    //Must unregister onPause()
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        getContext().unregisterReceiver(pushBroadcastReceiver);
    }

    //This is the handler that will manager to process the broadcast intent
    private WakefulBroadcastReceiver pushBroadcastReceiver = new WakefulBroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateNotificationList();
            invalidateOptionsMenu();
        }
    };

    //To update count after for new contents which are not downloaded in app.
    public void updateNotificationList(){
        int totalCount = 0;
        if(MyApplication.getContext().getNewVideosList() != null){
            totalCount = MyApplication.getContext().getNewVideosList().size();
            invalidateOptionsMenu();
            updateCountForBadge(totalCount);
        }
        if(MyApplication.getContext().getNewTaleemList() != null){
            totalCount += MyApplication.getContext().getNewTaleemList().size();
            invalidateOptionsMenu();
            updateCountForBadge(totalCount);

        }
        if(MyApplication.getContext().getNewAudioList()!= null){
            totalCount += MyApplication.getContext().getNewAudioList().size();
            updateCountForBadge(totalCount);
        }
        if(MyApplication.getContext().getNewImageList()!= null){
            totalCount += MyApplication.getContext().getNewImageList().size();
            invalidateOptionsMenu();
            updateCountForBadge(totalCount);
        }
        if(MyApplication.getContext().getNewBooksList()!= null){
            totalCount += MyApplication.getContext().getNewBooksList().size();
            invalidateOptionsMenu();
            updateCountForBadge(totalCount);
        }

    }
    private void updateCountForBadge(int totalCount){
        String badgeCount = String.valueOf(totalCount);
        if(totalCount > 9){
            badgeCount = Constants.NINE_PLUS;
        }
        Drawable drawable = MyApplication.getContext().writeOnDrawable(R.drawable.notifbell,badgeCount);
        toolbar.setOverflowIcon(drawable);
        invalidateOptionsMenu();
    }
    public static Activity getContext() {
        return mContext;
    }
    private void createMenuList() {
        SlideMenuItem menuItem0 = new SlideMenuItem(ContentFragment.CLOSE, R.drawable.icn_close);
        list.add(menuItem0);
        SlideMenuItem menuItem = new SlideMenuItem(ContentFragment.HOME, R.drawable.icn_1);
        list.add(menuItem);
        SlideMenuItem menuItem2 = new SlideMenuItem(ContentFragment.BOOKS, R.drawable.icn_2);
        list.add(menuItem2);
        SlideMenuItem menuItem3 = new SlideMenuItem(ContentFragment.VIDEOS, R.drawable.icn_3);
        list.add(menuItem3);
        SlideMenuItem menuItem4 = new SlideMenuItem(ContentFragment.TALEEM, R.drawable.icn_4);
        list.add(menuItem4);
        SlideMenuItem menuItem5 = new SlideMenuItem(ContentFragment.AUDIO, R.drawable.icn_6);
        list.add(menuItem5);
        SlideMenuItem menuItem6 = new SlideMenuItem(ContentFragment.PHOTOS, R.drawable.photos);
        list.add(menuItem6);
        SlideMenuItem menuItem7 = new SlideMenuItem(ContentFragment.SHAJRA, R.drawable.icn_7);
        list.add(menuItem7);
    }

    private void setActionBar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                drawerLayout,         /* DrawerLayout object */
                toolbar,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {
            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                linearLayout.removeAllViews();
                linearLayout.invalidate();
//                toolbar.setTitle(selectedMenuTitle);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                toolbar.setTitle(selectedMenuTitle);
                if (slideOffset > 0.6 && linearLayout.getChildCount() == 0)
                    viewAnimator.showMenuContent();
            }

            @Override

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }


    private ScreenShotable replaceFragment(ScreenShotable screenShotable, int topPosition,Fragment fragment) {
        if(fragment != contentFragment){
            View view = findViewById(R.id.content_frame);
            int finalRadius = Math.max(view.getWidth(), view.getHeight());
            SupportAnimator animator = ViewAnimationUtils.createCircularReveal(view, 0, topPosition, 0, finalRadius);
            animator.setInterpolator(new AccelerateInterpolator());
            animator.setDuration(ViewAnimator.CIRCULAR_REVEAL_ANIMATION_DURATION);
            findViewById(R.id.content_overlay).setBackgroundDrawable(new BitmapDrawable(getResources(), screenShotable.getBitmap()));
            animator.start();
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
        toolbar.setTitle(selectedMenuTitle);
        return screenShotable;
    }



    @Override
    public ScreenShotable onSwitch(Resourceble slideMenuItem, ScreenShotable screenShotable, int position) {
        Fragment fragment = null;
        switch (slideMenuItem.getName()) {
            case ContentFragment.CLOSE:
                return screenShotable;
            case ContentFragment.HOME:
                fragment = contentFragment;
                break;
            case ContentFragment.BOOKS:
                BooksFragment booksFragment = BooksFragment.newInstance(R.layout.ebook);
                fragment = booksFragment;
                break;
            case ContentFragment.VIDEOS:
                VideosFragment vid = VideosFragment.newInstance(R.layout.video);
                fragment = vid;
                break;
            case ContentFragment.TALEEM:
                TaleemFragment taleem = TaleemFragment.newInstance(R.layout.taleem);
                fragment = taleem;
                break;
            case ContentFragment.AUDIO:
                AudioFragment audio = AudioFragment.newInstance(R.layout.audio);
                fragment = audio;
                break;
            case ContentFragment.PHOTOS:
                PhotosGridViewFragment  photos = PhotosGridViewFragment.newInstance(R.layout.photos);
                fragment = photos;
                break;
            case ContentFragment.SHAJRA:
                ShajraFragment  shajra = ShajraFragment.newInstance(R.layout.ebook);
                fragment = shajra;
                break;
        }
        selectedMenuTitle = slideMenuItem.getName();
        return replaceFragment(screenShotable,position,fragment);
    }

    @Override
    public void disableHomeButton() {
        getSupportActionBar().setHomeButtonEnabled(false);
    }

    @Override
    public void enableHomeButton() {
        getSupportActionBar().setHomeButtonEnabled(true);
        drawerLayout.closeDrawers();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.books:
                navigateToScreenFromNotifications(0,2);
                break;
            case R.id.video:
                navigateToScreenFromNotifications(1,3);
                break;
            case R.id.taleem:
                navigateToScreenFromNotifications(2,4);
                break;
            case R.id.audio:
                navigateToScreenFromNotifications(3,5);
                break;
            case R.id.image:
                navigateToScreenFromNotifications(4,6);
                break;
            case R.id.update:
                updateNotificationList();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id="+getPackageName()));
                startActivity(intent);
                break;
        }
        return false;
    }
    public void navigateToScreenFromNotifications(int order,int position){
        onSwitch(list.get(position), new ScreenShotable() {
            @Override
            public void takeScreenShot() {

            }
            @Override
            public Bitmap getBitmap() {
                return null;
            }
        }, order);
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(MyApplication.getContext().getNewBooksList()!= null){
            menu.findItem(R.id.books).setVisible(true);
            menu.findItem(R.id.books).setTitle(String.valueOf(MyApplication.getContext().getNewBooksList().size())+" new book added");
        }else{
             menu.findItem(R.id.image).setVisible(false);
        }
       if(MyApplication.getContext().getNewVideosList() != null){
             menu.findItem(R.id.video).setVisible(true);
            menu.findItem(R.id.video).setTitle(String.valueOf(MyApplication.getContext().getNewVideosList().size())+" new video added");
        }else {
            menu.findItem(R.id.video).setVisible(false);
        }
        if(MyApplication.getContext().getNewTaleemList() != null){
            menu.findItem(R.id.taleem).setVisible(true);
            menu.findItem(R.id.taleem).setTitle(String.valueOf(MyApplication.getContext().getNewTaleemList().size())+" new taleem added");
        }else {
             menu.findItem(R.id.taleem).setVisible(false);
        }
        if(MyApplication.getContext().getNewAudioList()!= null){
             menu.findItem(R.id.audio).setVisible(true);
            menu.findItem(R.id.audio).setTitle(String.valueOf(MyApplication.getContext().getNewAudioList().size())+" new mp3 added");
        } else {
            menu.findItem(R.id.audio).setVisible(false);
        }
        if(MyApplication.getContext().getNewImageList()!= null){
             menu.findItem(R.id.image).setVisible(true);
            menu.findItem(R.id.image).setTitle(String.valueOf(MyApplication.getContext().getNewImageList().size())+" new photo added");
        }else{
            menu.findItem(R.id.image).setVisible(false);
        }

        if(MyApplication.getContext().isUpdateAvailable() == true){
             menu.findItem(R.id.update).setVisible(true);
            menu.findItem(R.id.update).setTitle("New version available-Download");
        }else{
             menu.findItem(R.id.update).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void addViewToContainer(View view) {
        linearLayout.addView(view);
    }

    @Override
    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Exit");
        builder.setMessage("Do you want to exit? ");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               MainActivity.super.onBackPressed();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        builder.show();
    }
}
