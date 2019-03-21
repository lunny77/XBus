package com.lunny.dev.xbus;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.lunny.xbus.Subscribe;
import com.lunny.xbus.ThreadMode;
import com.lunny.xbus.XBus;

public class MainActivity extends AppCompatActivity {
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView_main);
        findViewById(R.id.button_next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Main2Activity.class));
            }
        });

        XBus.getInstance().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void handleRequestBack(String message) {
        Log.d("xbus", "" + Thread.currentThread());
        textView.setText(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        XBus.getInstance().unregister(this);
    }
}
