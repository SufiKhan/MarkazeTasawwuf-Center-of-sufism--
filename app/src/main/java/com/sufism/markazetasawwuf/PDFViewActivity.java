package com.sufism.markazetasawwuf;

import android.app.Activity;
import android.os.Bundle;

import java.io.File;
import java.util.ArrayList;

import es.voghdev.pdfviewpager.library.PDFViewPager;

public class PDFViewActivity extends Activity {

    private PDFViewPager pdfViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle;
        if (savedInstanceState == null) {
            bundle = getIntent().getExtras();
            if (bundle!=null) {
                if(bundle.getInt(Constants.POSITION) > 1){
                    pdfViewPager = new PDFViewPager(this, bundle.getString(Constants.FILE));
                    setContentView(pdfViewPager);
                }else{
                    ArrayList<String> booklist = new ArrayList<>();
                    booklist.add(Constants.ASRARE_HAQIQI);
                    booklist.add(Constants.RAAZONKRAAZ);
                    int position = bundle.getInt(Constants.POSITION);
                    pdfViewPager = new PDFViewPager(this, booklist.get(position));
                    setContentView(pdfViewPager);
                }

            }else{
                pdfViewPager = new PDFViewPager(this, Constants.SHAJRA);
                setContentView(pdfViewPager);
            }
        }
    }
}
