package com.qualcomm.qti.objectdetection;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.efficientdet_lite.R;
import com.google.android.material.button.MaterialButton;

public class CountrySelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_selection);

        MaterialButton btnUs = findViewById(R.id.btn_us);
        MaterialButton btnCanada = findViewById(R.id.btn_canada);
        MaterialButton btnChina = findViewById(R.id.btn_china);

        btnUs.setOnClickListener(v -> startDetection("United States"));
        btnCanada.setOnClickListener(v -> startDetection("Canada"));
        btnChina.setOnClickListener(v -> startDetection("China"));
    }

    private void startDetection(String country) {
        Intent intent = new Intent(CountrySelectionActivity.this, MainActivity.class);
        intent.putExtra("SELECTED_COUNTRY", country);
        startActivity(intent);
    }
}
