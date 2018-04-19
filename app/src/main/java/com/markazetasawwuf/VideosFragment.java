package com.markazetasawwuf;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.markazetasawwuf.Adapter.CommonObject;
import com.markazetasawwuf.database.DBManager;
import com.markazetasawwuf.database.DatabaseHelper;
import com.markazetasawwuf.database.TinyDB;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yalantis.com.sidemenu.interfaces.ScreenShotable;


public class VideosFragment extends Fragment implements ScreenShotable{
    private Bitmap bitmap;
    protected int res;
    private View containerView;
    private RecyclerView mainListView ;
    private DBManager dbManager;

    public static VideosFragment newInstance(int resId) {
        VideosFragment frag = new VideosFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Integer.class.getName(), resId);
        frag.setArguments(bundle);
        return frag;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.containerView = view.findViewById(R.id.container);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        res = getArguments().getInt(Integer.class.getName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.video, container, false);
        mainListView = (RecyclerView)rootView.findViewById(R.id.recycler_view);
        mainListView.setHasFixedSize(true);
        mainListView.setLayoutManager(new GridLayoutManager(this.getActivity(), 2));
        dbManager = new DBManager(getActivity());
        updateClass();
        return rootView;
    }
    private void updateClass(){
        dbManager = new DBManager(getActivity());
        dbManager.open();
        if(dbManager.fetchVideos().getCount() == 0){
            if(MyApplication.isNetworkAvailble(getActivity())){
                if(MyApplication.getSessionToken() == null){
                    MyApplication.getContext().getSession(new MTCallback() {
                        @Override
                        public void onSuccess(Boolean success) {
                            // got session
                            getVideosFromServer();
                        }
                    });
                }
                else {
                    //Already session fetched just copied to this class
                    getVideosFromServer();
                }
            }else{
                if(dbManager.fetchVideos().getCount() > 0){
                    getDataFromDB();
                }else {
                    MyApplication.getContext().showCustomProgress(getActivity(), R.drawable.error, Constants.NO_INTERNET);
                    MyApplication.getContext().scheduleDismiss();
                }
            }
        }else{
            //Already data in DB
            getDataFromDB();
            if(MyApplication.getContext().getNewVideosList() != null) {
                getNewVideoList(MyApplication.getContext().getNewVideosList());
            }
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.video).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void takeScreenShot() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Bitmap bitmap = Bitmap.createBitmap(containerView.getWidth(),
                        containerView.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                containerView.draw(canvas);
                VideosFragment.this.bitmap = bitmap;
            }
        };
        thread.start();
    }

    @Override
    public Bitmap getBitmap() {
        return bitmap;
    }

    private void getVideosFromServer() {
        MyApplication.getContext().showProgress(getActivity(), Constants.PLEASE_WAIT, Constants.GETTING_VIDEOS);
        try{
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        StringRequest req = new StringRequest(Request.Method.GET, Constants.DATA + Constants.GET_VIDEOS,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject responseObject = new JSONObject(response);
                            parseData(responseObject);
                            removeNotificationForCategory();
                            MyApplication.getContext().stopProgress(getActivity());
                        } catch (JSONException e) {
                            MyApplication.getContext().stopProgress(getActivity());
                            errorOccured();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                MyApplication.getContext().stopProgress(getActivity());
                errorOccured();
            }
        }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("QB-Token", MyApplication.getSessionToken());
                    return params;
                }
        };

        queue.add(req);
    }catch (Exception e){
            System.out.println("Excep1" + e.getMessage());
            MyApplication.getContext().stopProgress(getActivity());
            errorOccured();
        }
    }

    private void parseData(JSONObject responseObject){
        try{
            if(responseObject.has("items")) {
                JSONArray resultsArray = responseObject.getJSONArray("items");
                dbManager.open();
                for (int i = 0; i < resultsArray.length(); i++) {
                    JSONObject json = resultsArray.getJSONObject(i);
                    insertInDB(json);
                }
                dbManager.close();
            }else {
                dbManager.open();
                insertInDB(responseObject);
                dbManager.close();
            }
            getDataFromDB();
        }catch (JSONException e){

        }
    }
    private void insertInDB(JSONObject json){
        try {
            CommonObject videoObj = new CommonObject();
            videoObj.name = json.getString("name");
            videoObj.videoId = json.getString("videoId");
            videoObj.thumbnailId = json.getString("thumbnailId");
            videoObj.time = json.getString("time");
            dbManager.insertVideo(videoObj.videoId, videoObj.thumbnailId, videoObj.name, videoObj.time);
        }catch (JSONException e){

        }
    }
    private void loadListForVideos(ArrayList<CommonObject> list){
        TinyDB tinyDB = new TinyDB(getActivity());
        if (tinyDB.getBoolean(Constants.FIRST_TIME_VIDEO_FETCH,false) != true){
            tinyDB.putBoolean(Constants.FIRST_TIME_VIDEO_FETCH,true);
        }
        mainListView.setAdapter(new VideoListAdapter(list,this.getActivity()));
    }

    private void getDataFromDB(){
        dbManager.open();
        Cursor cursor = dbManager.fetchVideos();
        ArrayList<CommonObject> _videoList = new ArrayList<CommonObject>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            CommonObject videoObj = new CommonObject();
            videoObj.name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.NAME));
            videoObj.time = cursor.getString(cursor.getColumnIndex(DatabaseHelper.TIME));
            videoObj.videoId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.VIDEO_ID));
            videoObj.thumbnailId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.THUMBNAIL_ID));
            _videoList.add(videoObj);

        }
        Collections.reverse(_videoList);
        if(_videoList.size() != 0){
            loadListForVideos(_videoList);
        }
        loadListForVideos(_videoList);
        dbManager.close();
    }
    // Remove notification from notifiction list
    private  void removeNotificationForCategory(){
        if(MyApplication.getContext().getNewVideosList() != null) {
            setHasOptionsMenu(true);
            MyApplication.getContext().getTinyDb().remove(Constants.VIDEO_CAT);
            ((MainActivity)getActivity()).updateNotificationList();
        }
    }
    //Fetch new list of videos from Notifications
    private void getNewVideoList(List<String> videosIds){
        String ids = MyApplication.getContext().convertListToStringIds(videosIds);
        MyApplication.getContext().showProgress(getActivity(), Constants.PLEASE_WAIT, Constants.GETTING_NEW_VIDEOS);
        try{
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            String url = Constants.DATA + Constants.VIDEOS_CLASS+ids+Constants.GET_BY_IDS;
            StringRequest req = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject responseObject = new JSONObject(response);
                                parseData(responseObject);
                                removeNotificationForCategory();
                                MyApplication.getContext().stopProgress(getActivity());
                            } catch (JSONException e) {
                                MyApplication.getContext().stopProgress(getActivity());
                                errorOccured();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    MyApplication.getContext().stopProgress(getActivity());
                    errorOccured();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("QB-Token", MyApplication.getSessionToken());
                    return params;
                }
            };

            queue.add(req);
        }catch (Exception e){
            System.out.println("Excep1" + e.getMessage());
            MyApplication.getContext().stopProgress(getActivity());
            errorOccured();
        }
    }
    private void errorOccured(){
        MyApplication.getContext().showError();
        MyApplication.getContext().scheduleDismiss();
    }
}