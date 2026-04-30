package com.quicinc.objectdetection;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class CountrySelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_selection);

        Button btnUs = findViewById(R.id.btn_us);
        Button btnCanada = findViewById(R.id.btn_canada);
        Button btnChina = findViewById(R.id.btn_china);

        btnUs.setOnClickListener(v -> startDetection("United States"));
        btnCanada.setOnClickListener(v -> startDetection("Canada"));
        btnChina.setOnClickListener(v -> startDetection("China"));
    }

    private void startDetection(String country) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("SELECTED_COUNTRY", country);
        startActivity(intent);
    }
}
