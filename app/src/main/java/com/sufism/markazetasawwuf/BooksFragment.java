package com.sufism.markazetasawwuf;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sufism.markazetasawwuf.Adapter.CommonObject;
import com.sufism.markazetasawwuf.Adapter.ListviewAdapter;
import com.sufism.markazetasawwuf.Adapter.Taleem;
import com.sufism.markazetasawwuf.Service.DownloadFile;
import com.sufism.markazetasawwuf.database.DBManager;
import com.sufism.markazetasawwuf.database.DatabaseHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yalantis.com.sidemenu.interfaces.ScreenShotable;


/**
 * Created by sierrasolutionsmacuser5 on 23/1/17.
 */
public class BooksFragment extends Fragment implements ScreenShotable{
    private Bitmap bitmap;
    protected int res;
    private View containerView;
    private ListView mainListView ;
    private DBManager dbManager;
    public static BooksFragment newInstance(int resId) {
        BooksFragment frag = new BooksFragment();
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
        View rootView = inflater.inflate(R.layout.ebook, container, false);
        mainListView = (ListView)rootView.findViewById(R.id.bookTblView);

        //CHECK Local storage for books
        dbManager = new DBManager(getActivity());
        dbManager.open();
        if(dbManager.fetchBooks().getCount() == 0){
            if(MyApplication.isNetworkAvailble(getActivity())){
                getDataFromDB();
                if(MyApplication.getSessionToken() == null){
                    MyApplication.getContext().getSession(new MTCallback() {
                        @Override
                        public void onSuccess(Boolean success) {
                            // got session
                            getBooksFromServer();
                        }
                    });
                }
                else {
                    //Already session fetched just copied to this class
                    getBooksFromServer();
                }
            }else{

                    MyApplication.getContext().showCustomProgress(getActivity(), R.drawable.error, Constants.NO_INTERNET);
                    MyApplication.getContext().scheduleDismiss();
                    getDataFromDB();

            }
        }else{
            //Already data in DB
            getDataFromDB();
            if(MyApplication.getContext().getNewBooksList() != null) {
                getNewBooksList(MyApplication.getContext().getNewBooksList());
            }
        }
        return rootView;
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
                BooksFragment.this.bitmap = bitmap;
            }
        };
        thread.start();
    }

    @Override
    public Bitmap getBitmap() {
        return bitmap;
    }

    private void getBooksFromServer(){
        MyApplication.getContext().showProgress(getActivity(), Constants.PLEASE_WAIT, Constants.GETTING_BOOKS);
        try{
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            StringRequest req = new StringRequest(Request.Method.GET, Constants.DATA + Constants.GET_BOOKS,
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
            CommonObject booksObj = new CommonObject();
            booksObj.name = json.getString("name");
            booksObj.bookId = json.getString("bookId");
            booksObj.thumbnailId = json.getString("thumbnailId");
            booksObj.author = json.getString("author");
            dbManager.insertBook(booksObj.bookId,booksObj.thumbnailId,booksObj.name,booksObj.author);
        }catch (JSONException e){

        }
    }

    private void getDataFromDB(){
        final ArrayList<Taleem> list = new ArrayList<>();
        Taleem book1 = new Taleem("Asrare haqiqi(Hindi)","Sarkar Garib Nawaz",R.drawable.book1);
        Taleem book2 = new Taleem("Raazon ke Raaz(Sirrul Asrar)","Sarkar Gaus pak",R.drawable.book2);
        list.add(book1);
        list.add(book2);
        loadList(null,list);
        dbManager.open();
        Cursor cursor = dbManager.fetchBooks();
        ArrayList<CommonObject> booksList = new ArrayList<CommonObject>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            CommonObject booksObj = new CommonObject();
            booksObj.name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.NAME));
            booksObj.bookId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.BOOK_ID));
            booksObj.thumbnailId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.THUMBNAIL_ID));
            booksObj.author = cursor.getString(cursor.getColumnIndex(DatabaseHelper.AUTHOR));
            booksList.add(booksObj);
            Taleem obj = new Taleem(booksObj.name,booksObj.author,Integer.parseInt(booksObj.thumbnailId));
            list.add(obj);
        }
        if(booksList.size() != 0){
            loadList(booksList,list);
            return;
        }
        loadList(booksList,list);
        dbManager.close();
    }
    private void loadList(final ArrayList<CommonObject> booksList,final ArrayList<Taleem> rowList){
        final ListviewAdapter adapter = new ListviewAdapter(getActivity(),rowList,true);
        mainListView.setAdapter(adapter);
        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if(booksList != null && position > 1){
                    //Check if file exists
                    File path = new File(Environment.getExternalStorageDirectory() + File.separator + Constants.APP_NAME);
                    final File file = new File(path, booksList.get(position-2).name);//Common object list is diff with rowList i.e dynamic / static
                    if(file.exists()){
                        openBook(position,file.getAbsolutePath());
                    }else{//Download file
                            DownloadBook(booksList.get(position-2),adapter);
                    }
                }else {
                    openBook(position,null);
                }

            }
        });
    }
    private void DownloadBook(CommonObject book, final BaseAdapter adapter){
        if(MyApplication.isNetworkAvailble(getActivity())){
            final ArrayList<String> params = new ArrayList<String>();
            String url = Constants.BLOBURL+book.bookId+Constants.DOWNLOAD;
            params.add(url);
            params.add(book.name);
            if(MyApplication.getSessionToken() == null) {
                MyApplication.getContext().getSession(new MTCallback() {
                    @Override
                    public void onSuccess(Boolean success) {
                        new DownloadFile(getActivity(),adapter).execute(params);
                    }
                });

            }else{
                new DownloadFile(getActivity(),adapter).execute(params);
            }
        }
        else{
            MyApplication.getContext().showCustomProgress(getActivity(),R.drawable.error,Constants.NO_INTERNET);
            MyApplication.getContext().scheduleDismiss();
        }
    }
    private void openBook(int position,String filepath){
        Intent pdfViewer = new Intent(getActivity(),
                PDFViewActivity.class);
        pdfViewer.putExtra(Constants.POSITION, position);
        pdfViewer.putExtra(Constants.FILE, filepath);
        getActivity().startActivity(pdfViewer);
    }
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.books).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }
    private  void removeNotificationForCategory(){
        if(MyApplication.getContext().getNewBooksList() != null) {
            setHasOptionsMenu(true);
            MyApplication.getContext().getTinyDb().remove(Constants.BOOKS_CAT);
            ((MainActivity)getActivity()).updateNotificationList();
        }
    }
    private void getNewBooksList(List<String> booksIds){
        String ids = MyApplication.getContext().convertListToStringIds(booksIds);
        MyApplication.getContext().showProgress(getActivity(), Constants.PLEASE_WAIT, Constants.GETTING_NEW_BOOKS);
        try{
            RequestQueue queue = Volley.newRequestQueue(getActivity());
            String url = Constants.DATA + Constants.BOOK_CLASS+ids+Constants.GET_BY_IDS;
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
