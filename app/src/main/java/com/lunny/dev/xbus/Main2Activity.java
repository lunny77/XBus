package com.lunny.dev.xbus;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.lunny.xbus.XBus;

public class Main2Activity extends AppCompatActivity implements Runnable {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        findViewById(R.id.button_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(Main2Activity.this).start();
            }
        });
    }

    @Override
    public void run() {
        XBus.getInstance().post("message from main 2 activity");
        finish();
    }
}
