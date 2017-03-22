package com.sufism.markazetasawwuf;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

/**
 * Created by sierrasolutionsmacuser5 on 13/2/17.
 */
public class Splash extends Activity {

    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 2000;
    private final int SPLASH_SLIDE_DELAY = 1500;
    private int count = 0;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.splash);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        splashStartAnimation();

        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(Splash.this,MainActivity.class);
                Splash.this.startActivity(mainIntent);
                Splash.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    private void splashStartAnimation(){
        final Animation anim = AnimationUtils.loadAnimation(this, R.anim.scale);
        final Animation move = AnimationUtils.loadAnimation(this, R.anim.move);
        final TextView thirdString = (TextView) findViewById(R.id.thirdString);
        setFont(thirdString);
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                thirdString.startAnimation(move);
                thirdString.setVisibility(View.VISIBLE);
            }
        }, SPLASH_SLIDE_DELAY);
        final TextView firstString = (TextView) findViewById(R.id.firstString);
        setFont(firstString);
        final TextView secondString = (TextView) findViewById(R.id.secondString);
        setFont(secondString);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                firstString.clearAnimation();
                count++;
                if(count ==1) {
                    secondString.startAnimation(anim);
                    secondString.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        firstString.startAnimation(anim);
    }

    private void setFont(TextView textView){
        MyApplication.getContext().setDisciplinaFont(textView);
    }
}