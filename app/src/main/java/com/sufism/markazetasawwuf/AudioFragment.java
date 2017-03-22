package com.sufism.markazetasawwuf;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.sufism.markazetasawwuf.Adapter.CommonObject;
import com.sufism.markazetasawwuf.database.DBManager;
import com.sufism.markazetasawwuf.database.DatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yalantis.com.sidemenu.interfaces.ScreenShotable;

/**
 * Created by sierrasolutionsmacuser5 on 15/2/17.
 */

public class AudioFragment extends Fragment implements ScreenShotable{
    private Bitmap bitmap;
    protected int res;
    private View containerView;
    private ListView mainListView ;
    private KProgressHUD hud;
    private DBManager dbManager;

    public static AudioFragment newInstance(int resId) {
        AudioFragment frag = new AudioFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(Integer.class.getName(), resId);
        frag.setArguments(bundle);
        return frag;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.containerView = view.findViewById(R.id.containerAudio);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        res = getArguments().getInt(Integer.class.getName());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.audio, container, false);
        mainListView = (ListView)rootView.findViewById(R.id.audiolistview);
        dbManager = new DBManager(getActivity());
        dbManager.open();
        if(dbManager.fetchAudios().getCount() == 0){
            if(MyApplication.isNetworkAvailble(getActivity())){
                if(MyApplication.getSessionToken() == null){
                     MyApplication.getContext().getSession(new MTCallback() {
                        @Override
                        public void onSuccess(Boolean success) {
                            // got session
                            getAudiosFromServer();
                        }
                    });
                }
                else {
                    //Already session fetched just copied to this class
                    getAudiosFromServer();
                }
            }else{
                if(dbManager.fetchAudios().getCount() > 0){
                    getDataFromDB();
                }else {
                    MyApplication.getContext().showCustomProgress(getActivity(), R.drawable.error, Constants.NO_INTERNET);
                    MyApplication.getContext().scheduleDismiss();
                }
            }
        }else{
            //Already data in DB
            getDataFromDB();
            if(MyApplication.getContext().getNewAudioList() != null) {
                getNewAudiosList(MyApplication.getContext().getNewAudioList());
            }
        }
        return rootView;
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.audio).setVisible(false);
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
                AudioFragment.this.bitmap = bitmap;
            }
        };
        thread.start();
    }

    @Override
    public Bitmap getBitmap() {
        return bitmap;
    }

    private void getAudiosFromServer() {
        MyApplication.getContext().showProgress(getActivity(), Constants.PLEASE_WAIT, Constants.GETTING_AUDIOS);
        try{
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            StringRequest req = new StringRequest(Request.Method.GET, Constants.DATA + Constants.GET_AUDIOS,
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
            CommonObject audioObj = new CommonObject();
            audioObj.name = json.getString("name");
            audioObj.audioId = json.getString("audioId");
            dbManager.insertAudios(audioObj.name,audioObj.audioId);
        }catch (JSONException e){

        }
    }
    private void loadListForAudios(ArrayList<CommonObject> list){
        mainListView.setAdapter(new AudioAdapter(getActivity(),list));
    }

    private void getDataFromDB(){
        dbManager.open();
        Cursor cursor = dbManager.fetchAudios();
        ArrayList<CommonObject> _audioList = new ArrayList<CommonObject>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            CommonObject audioObj = new CommonObject();
            audioObj.name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.NAME));
            audioObj.audioId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.AUDIO_ID));
            _audioList.add(audioObj);

        }
        Collections.reverse(_audioList);
        if(_audioList.size() != 0){
            loadListForAudios(_audioList);
        }
        loadListForAudios(_audioList);
        dbManager.close();
    }
    // Remove notification from notifiction list
    private  void removeNotificationForCategory(){
        if(MyApplication.getContext().getNewAudioList() != null) {
            setHasOptionsMenu(true);
            MyApplication.getContext().getTinyDb().remove(Constants.AUDIO_CAT);
            ((MainActivity)getActivity()).updateNotificationList();
        }
    }
    //Fetch new list of videos from Notifications
    private void getNewAudiosList(List<String> audiosIds){
        String ids = MyApplication.getContext().convertListToStringIds(audiosIds);
        MyApplication.getContext().showProgress(getActivity(), Constants.PLEASE_WAIT, Constants.GETTING_NEW_AUDIOS);
        try{
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            String url = Constants.DATA + Constants.AUDIOS_CLASS+ids+Constants.GET_BY_IDS;
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