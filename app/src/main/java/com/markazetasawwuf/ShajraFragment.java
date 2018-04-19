package com.markazetasawwuf;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.markazetasawwuf.Adapter.ListviewAdapter;
import com.markazetasawwuf.Adapter.Taleem;

import java.util.ArrayList;

import yalantis.com.sidemenu.interfaces.ScreenShotable;

/**
 * Created by HP on 3/8/2017.
 */

public class ShajraFragment  extends Fragment implements ScreenShotable {
    private Bitmap bitmap;
    protected int res;
    private View containerView;
    private ListView mainListView;

    public static ShajraFragment newInstance(int resId) {
        ShajraFragment frag = new ShajraFragment();
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
        mainListView = (ListView) rootView.findViewById(R.id.bookTblView);

        ArrayList<Taleem> list = new ArrayList<>();
        Taleem book1 = new Taleem("Shajra sharif", "Hindi", R.drawable.icn_7);
        list.add(book1);
        loadList(list);
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
                ShajraFragment.this.bitmap = bitmap;
            }
        };
        thread.start();
    }

    @Override
    public Bitmap getBitmap() {
        return bitmap;
    }


    private void loadList(final ArrayList<Taleem> list) {
        mainListView.setAdapter(new ListviewAdapter(getActivity(), list,false));
        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent pdfViewer = new Intent(getActivity(),
                        PDFViewActivity.class);
                getActivity().startActivity(pdfViewer);
            }
        });
    }
}