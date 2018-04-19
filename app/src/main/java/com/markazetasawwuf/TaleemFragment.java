package com.markazetasawwuf;

import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.markazetasawwuf.Adapter.ListviewAdapter;
import com.markazetasawwuf.Adapter.Taleem;
import com.markazetasawwuf.database.DBManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yalantis.com.sidemenu.interfaces.ScreenShotable;


public class TaleemFragment extends Fragment implements ScreenShotable {

    private String token;
    private Bitmap bitmap;
    protected int res;
    private View containerView;
    private ProgressBar progressBar;
    private ListView mainListView ;
    private ArrayAdapter<String> listAdapter ;
    private DBManager dbManager;
    private KProgressHUD hud;
    public static TaleemFragment newInstance(int resId) {
        TaleemFragment frag = new TaleemFragment();
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
        View rootview = inflater.inflate(R.layout.audio, container, false);
        mainListView = (ListView)rootview.findViewById(R.id.audiolistview);
        dbManager = new DBManager(getActivity());
        dbManager.open();
        if(dbManager.fetchTeachings().getCount() == 0){
            if(MyApplication.isNetworkAvailble(getActivity())){
                if(MyApplication.getSessionToken() == null){
                    token =  MyApplication.getContext().getSession(new MTCallback() {
                        @Override
                        public void onSuccess(Boolean success) {
                            getTaleem();
                        }
                    });
                }
                else {
                    token = MyApplication.getSessionToken();
                    getTaleem();
                }
            }else{
                if(dbManager.fetchTeachings().getCount() > 0){
                  getTeachingFromDB();
                }else {
                    MyApplication.getContext().showCustomProgress(getActivity(), R.drawable.error, Constants.NO_INTERNET);
                    MyApplication.getContext().scheduleDismiss();
                }
            }
        }else{
            getTeachingFromDB();
            if(MyApplication.getContext().getNewTaleemList() != null) {
                getNewTaleems(MyApplication.getContext().getNewTaleemList());
            }
        }
        return rootview;
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
                TaleemFragment.this.bitmap = bitmap;
            }
        };
        thread.start();

    }

    @Override
    public Bitmap getBitmap() {

        return bitmap;
    }
    private void getTaleem() {
        MyApplication.getContext().showProgress(getActivity(),Constants.PLEASE_WAIT,Constants.GETTING_TALEEM);
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        StringRequest req = new StringRequest(Request.Method.GET, Constants.DATA+Constants.GET_TALEEM,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject responseObject = new JSONObject(response);
                            parseData(responseObject);
                            removeNotificationForCategory();
                            MyApplication.getContext().stopProgress(getActivity());
                        } catch (JSONException e) {
                            e.printStackTrace();
                            MyApplication.getContext().stopProgress(getActivity());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                MyApplication.getContext().stopProgress(getActivity());
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
            getTeachingFromDB();
        }catch (JSONException e){

        }
    }
    private void insertInDB(JSONObject json){
        try{
            Taleem taleem = new Taleem(json.get("taleem").toString(),json.get("teacher").toString(),0);
            dbManager.insertTeachings(taleem.teaching,taleem.teacher);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void getTeachingFromDB(){
        dbManager.open();
        Cursor _cursor = dbManager.fetchTeachings();
        ArrayList<Taleem> list = new ArrayList<Taleem>();
        while (_cursor.moveToNext())
        {
            Taleem taleem = new Taleem(_cursor.getString(1),_cursor.getString(2),0);
            list.add(taleem);
        }
        _cursor.close();
        Collections.reverse(list);
        loadList(list);
    }
    private void getNewTaleems(List<String> taleemIds){
        String ids = MyApplication.getContext().convertListToStringIds(taleemIds);
        MyApplication.getContext().showProgress(getActivity(), Constants.PLEASE_WAIT, Constants.GETTING_NEW_TALEEM);
        try{
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            String url = Constants.DATA + Constants.TALEEM_CLASS+ids+Constants.GET_BY_IDS;
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
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    MyApplication.getContext().stopProgress(getActivity());
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
            MyApplication.getContext().stopProgress(getActivity());
        }
    }

    private void loadList(final ArrayList<Taleem> list){
        mainListView.setAdapter(new ListviewAdapter(getActivity(),list,false));
        dbManager.close();
        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent details = new Intent(getActivity(),
                        TaleemDetail.class);
                details.putExtra(Constants.POSITION, position);
                details.putExtra(Constants.TABLE_NAME_TEACHING, list);
                getActivity().startActivity(details);
            }
        });
    }
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.taleem).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }
    private  void removeNotificationForCategory(){
        if(MyApplication.getContext().getNewTaleemList() != null) {
            setHasOptionsMenu(true);
            MyApplication.getContext().getTinyDb().remove(Constants.TALEEM_CAT);
            ((MainActivity)getActivity()).updateNotificationList();
        }
    }
}