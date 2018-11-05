package com.futurice.android.reservator;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class LandingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        Intent i = getIntent();
        TextView helloTextView = (TextView) findViewById(R.id.helloTextView);
        helloTextView.setText("Hello, " + i.getStringExtra("username"));
    }
}
