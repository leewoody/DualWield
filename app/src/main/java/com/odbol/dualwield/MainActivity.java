package com.odbol.dualwield;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.burns.android.ancssample.R;

public class MainActivity extends AppCompatActivity {

    private TextView statusText;
    private Switch connectionSwitch;
    private Button rePairButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        statusText = findViewById(R.id.status);
        connectionSwitch = findViewById(R.id.connectionSwitch);
        rePairButton = findViewById(R.id.repairButton);


    }
}
