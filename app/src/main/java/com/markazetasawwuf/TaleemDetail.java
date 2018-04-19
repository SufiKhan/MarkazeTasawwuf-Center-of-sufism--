package com.markazetasawwuf;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.markazetasawwuf.Adapter.Taleem;
import com.markazetasawwuf.database.TinyDB;

import java.util.ArrayList;


/**
 * Created by sierrasolutionsmacuser5 on 23/1/17.
 */
public class TaleemDetail extends Activity
{

    private TextView taleem;
    private TextView teacher;
    private TextView count;
    private ArrayList<Taleem> _list;
    private int mCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.taleemdetail);

        taleem = (TextView) findViewById(R.id.taleem);
        teacher = (TextView) findViewById(R.id.teacher);
        count=(TextView) findViewById(R.id.countdown);
        MyApplication.getContext().setPTFont(taleem);
        MyApplication.getContext().setPTFont(teacher);
        MyApplication.getContext().setPTFont(count);
        TinyDB tiny = new TinyDB(getBaseContext());
        Bundle bundle;
        if (savedInstanceState == null) {
            bundle = getIntent().getExtras();
            if (bundle!=null) {
                _list = bundle.getParcelableArrayList(Constants.TABLE_NAME_TEACHING);
                mCounter = bundle.getInt(Constants.POSITION);
            }
        }
        updateText();
        Button nextButton = (Button) findViewById(R.id.next);
        nextButton.setTypeface(MyApplication.getContext().getTipoFont());
        nextButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0)
            {
                if (mCounter<_list.size()-1) {
                    mCounter++;
                    updateText();
                }
            }
        });


        Button prevButton = (Button) findViewById(R.id.prev);
        prevButton.setTypeface(MyApplication.getContext().getTipoFont());
        prevButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0)
            {
                if (mCounter>0) {
                    mCounter--;
                    updateText();
                }


            }
        });
    }

    private void updateText()
    {
        Taleem _taleemObj = _list.get(mCounter);
        teacher.setText(_taleemObj.teacher.toUpperCase());
        taleem.setText(_taleemObj.teaching);
        count.setText(mCounter+1+" of "+_list.size());
    }
}
