package com.sufism.markazetasawwuf;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.gson.JsonParseException;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;
import com.sufism.markazetasawwuf.database.TinyDB;

import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.voghdev.pdfviewpager.library.asset.CopyAsset;
import es.voghdev.pdfviewpager.library.asset.CopyAssetThreadImpl;

public class MyApplication extends Application {
    private static String sessionToken;
    private static MyApplication application;
    private KProgressHUD hud;
    private TinyDB tinyDb;
    private int notifCount;
    final String[] pdfAssets = {Constants.ASRARE_HAQIQI, Constants.RAAZONKRAAZ,Constants.SHAJRA};
    private final int DISPLAY_LENGTH = 1000;
    @Override
    public void onCreate() {
        super.onCreate();
        application = (MyApplication)getApplicationContext();
        tinyDb = new TinyDB(getBaseContext());
        initSampleAssets();

    }
    public TinyDB getTinyDb(){
        return tinyDb;
    }
    public static MyApplication getContext() {
        return application;
    }
    public String getSession(final MTCallback callback) {
        if(isNetworkAvailble(getContext())){
            try{
                QBUser user = new QBUser(Constants.USER, Constants.PASSWORD);
                QBAuth.createSession(user).performAsync(new QBEntityCallback<QBSession>() {
                    @Override
                    public void onSuccess(QBSession qbSession, Bundle bundle) {
                        sessionToken = qbSession.getToken();
                        if(getTinyDb().getBoolean(Constants.TOKEN_SENT_TO_SERVER,false)!= true){
                            subscribeToGCM(sessionToken);
                        }
                        callback.onSuccess(true);

                    }

                    @Override
                    public void onError(QBResponseException e) {
                        sessionToken = null;
                        callback.onSuccess(false);
                    }
                });
            }catch (Exception e){
                callback.onSuccess(false);
            }
        }else {
            callback.onSuccess(false);
            if(getTinyDb().getBoolean(Constants.TOKEN_SENT_TO_SERVER,false)!= true) {
                showCustomProgress(MainActivity.getContext(), R.drawable.error, Constants.NO_INTERNET);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scheduleDismiss();
                    }
                }, DISPLAY_LENGTH);
            }

        }

        return sessionToken;
    }

    private void subscribeToGCM(final String token){
        if(isNetworkAvailble(getApplicationContext())){
//            showProgress(MainActivity.getContext(),Constants.PLEASE_WAIT,"Subscribing for notifications");
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                       InstanceID instanceID = InstanceID.getInstance(MainActivity.getContext());
                        String deviceToken = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                                GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                        JSONObject json = new JSONObject();
                        json.put("notification_channels","gcm");
                        JSONObject pushToken = new JSONObject();
                        pushToken.put("environment","production");
                        pushToken.put("client_identification_sequence",deviceToken);
                        json.put("push_token",pushToken);
                        JSONObject device = new JSONObject();
                        device.put("platform","android");
                        String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                        device.put("udid",deviceId);
                        json.put("device",device);
//                        stopProgress(MainActivity.getContext());
                        sendSubscriptionToServer(json,token);

                    }catch (Exception e) {
//                        stopProgress(MainActivity.getContext());
                        Log.d("GCM", "Failed to complete token refresh", e);
                    }
                }

            });
            thread.start();
        }else{
            showCustomProgress(MainActivity.getContext(),R.drawable.error,Constants.NO_INTERNET);
            scheduleDismiss();
        }
    }
    private void sendSubscriptionToServer(JSONObject params, final String token){
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            final String requestBody = params.toString();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.FCM_SUBSCRIPTION_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    getTinyDb().putBoolean(Constants.TOKEN_SENT_TO_SERVER,true);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("VOLLEY", error.getMessage());
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return requestBody == null ? null : requestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                        // can get more details such as response.headers
                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("QB-Token", token);
                    return params;
                }
            };
            requestQueue.add(stringRequest);
        } catch (JsonParseException e) {
            e.printStackTrace();
        }
    }
    public static String getSessionToken(){
        return sessionToken;
    }
    public static Boolean isNetworkAvailble(Context ctx){
        ConnectivityManager connMgr = (ConnectivityManager)ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public void showProgress(Activity activity, String title, String msg){
        hud = KProgressHUD.create(activity)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel(title)
                .setDetailsLabel(msg)
                .setCancellable(false)
                .setAnimationSpeed(1)
                .setDimAmount(0.5f)
                .show();
    }
    public void stopProgress(Activity activity){
        hud.dismiss();
    }
    public void showCustomProgress(Activity activity, int _res, String msg){
        ImageView imageView = new ImageView(activity);
        imageView.setBackgroundResource(_res);
        hud = KProgressHUD.create(activity)
                .setCustomView(imageView)
                .setLabel(msg)
                .show();
    }
    public void showError(){
        showCustomProgress(MainActivity.getContext(),R.drawable.error,Constants.ERROR_OCCURED);

    }
    public void scheduleDismiss() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                hud.dismiss();
            }
        }, 1000);
    }


    public void showDeterminate(Activity activity, int progress){
        hud = KProgressHUD.create(activity)
                .setStyle(KProgressHUD.Style.PIE_DETERMINATE)
                .setLabel(Constants.PLEASE_WAIT)
                .setMaxProgress(100).setCancellable(false)
                .show();
    }
    public void setProgress(int progress){
        if(progress == 100){
            hud.dismiss();
        }else {
            hud.setProgress(progress);
        }
    }
    public BitmapDrawable writeOnDrawable(int drawableId, String text){
        Bitmap bm = BitmapFactory.decodeResource(getResources(), drawableId).copy(Bitmap.Config.ARGB_8888, true);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        int spSize = 12;
        float scaledSizeInPixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spSize, getResources().getDisplayMetrics());
        paint.setTextSize(scaledSizeInPixels);
        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        Canvas canvas = new Canvas(bm);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        canvas.drawText(text, bm.getWidth()/2 - bounds.width()/2, bm.getHeight()/2 + bounds.height()/2, paint);
        return new BitmapDrawable(getContext().getResources(),bm);
    }
    public List<String> getNewVideosList(){
        if(getTinyDb().getString(Constants.VIDEO_CAT) != ""){
            List<String> list = Arrays.asList(getTinyDb().getString(Constants.VIDEO_CAT).split(","));
            return list;
        }else{
            return null;
        }
    }
    public List<String> getNewTaleemList(){
        if(getTinyDb().getString(Constants.TALEEM_CAT) != ""){
            List<String> list = Arrays.asList(getTinyDb().getString(Constants.TALEEM_CAT).split(","));
            return list;
        }else{
            return null;
        }
    }
    public List<String> getNewAudioList(){
        if(getTinyDb().getString(Constants.AUDIO_CAT) != ""){
            List<String> list = Arrays.asList(getTinyDb().getString(Constants.AUDIO_CAT).split(","));
            return list;
        }else{
            return null;
        }
    }
    public List<String> getNewImageList(){
        if(getTinyDb().getString(Constants.IMAGE_CAT) != ""){
            List<String> list = Arrays.asList(getTinyDb().getString(Constants.IMAGE_CAT).split(","));
            return list;
        }else{
            return null;
        }
    }
    public List<String> getNewBooksList(){
        if(getTinyDb().getString(Constants.BOOKS_CAT) != ""){
            List<String> list = Arrays.asList(getTinyDb().getString(Constants.BOOKS_CAT).split(","));
            return list;
        }else{
            return null;
        }
    }
    public Boolean isUpdateAvailable(){
            return getTinyDb().getBoolean(Constants.UPDATE_CAT,false);
    }
    public String convertListToStringIds(List<String> list){
        StringBuilder commaSepValueBuilder = new StringBuilder();
        //Looping through the list
        for ( int i = 0; i< list.size(); i++){
            //append the value into the builder
            commaSepValueBuilder.append(list.get(i));
            //if the value is not the last element of the list
            //then append the comma(,) as well
            if ( i != list.size()-1){
                commaSepValueBuilder.append(",");
            }
        }
        return commaSepValueBuilder.toString();
    }
    private void initSampleAssets() {
        if(getTinyDb().getBoolean(Constants.ASSETS_COPIED,false) != true){
            CopyAsset copyAsset = new CopyAssetThreadImpl(this, new Handler());
            for (String asset : pdfAssets) {
                copyAsset.copy(asset, new File(getCacheDir(), asset).getAbsolutePath());
            }
            getTinyDb().putBoolean(Constants.ASSETS_COPIED,true);
        }

    }

    public int getNotifCount(){
        return notifCount;
    }
    public void setNotifCount(int count){
        notifCount = count;
    }

    public void setPTFont(TextView txt){
        Typeface font = Typeface.createFromAsset(getContext().getResources().getAssets(), Constants.PT_SANS);
        txt.setTypeface(font);
    }
    public void setDisciplinaFont(TextView txt){
        Typeface font = Typeface.createFromAsset(getContext().getResources().getAssets(), Constants.TIPO);
        txt.setTypeface(font);
    }
    public Typeface getPTFont(){
        return Typeface.createFromAsset(getContext().getResources().getAssets(), Constants.PT_SANS);
    }
    public Typeface getTipoFont(){

        return Typeface.createFromAsset(getContext().getResources().getAssets(), Constants.TIPO);
    }
}

interface MTCallback{
    void onSuccess(Boolean success);
}