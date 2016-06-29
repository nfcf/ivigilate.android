package com.ivigilate.android.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.ivigilate.android.app.R;
import com.ivigilate.android.library.IVigilateManager;

public class SplashActivity extends Activity {
    private final int SPLASH_TIME = 1500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        final IVigilateManager iVigilateManager = IVigilateManager.getInstance(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (iVigilateManager.getServiceEnabled()) {
                    Intent i = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(i);
                } else {
                    Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(i);
                }
                finish();
            }
        }, SPLASH_TIME);
    }
}
