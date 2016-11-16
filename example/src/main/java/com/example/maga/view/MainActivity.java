package com.example.maga.view;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import com.maga.customview.RouletteView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RouletteView rouletteView = (RouletteView) findViewById(R.id.rouletteView);
        for(int i = 0; i < 5; i++){
            rouletteView.addItem(new TextView(this));
        }
    }
}
