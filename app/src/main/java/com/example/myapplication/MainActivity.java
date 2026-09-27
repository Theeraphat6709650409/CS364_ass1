package com.example.myapplication;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    DecimalFormat formatter = new DecimalFormat("#,##0.00");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText etWeight = findViewById(R.id.etWeight);
        EditText etHeight = findViewById(R.id.etHeight);
        TextView tvBmiResult = findViewById(R.id.tvBmiResult);
        TextView tvCategoryResult = findViewById(R.id.tvCategoryResult);
        Button btnCalculate = findViewById(R.id.btnCalculate);

        etWeight.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});
        etHeight.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});

        btnCalculate.setOnClickListener(v -> {
            String weightStr = etWeight.getText().toString().trim();
            String heightStr = etHeight.getText().toString().trim();

            if (weightStr.isEmpty() || heightStr.isEmpty()) {
                Toast.makeText(this, R.string.msg_fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            double weight = Double.parseDouble(weightStr);
            double heightCm = Double.parseDouble(heightStr);

            if (heightCm <= 0 || weight <= 0) {
                Toast.makeText(this, R.string.msg_invalid_input, Toast.LENGTH_SHORT).show();
                return;
            }

            double heightM = heightCm / 100.0;
            double rawBmi = weight / (heightM * heightM);
            double bmi = Math.round(rawBmi * 10.0) / 10.0;
            String formattedBmi = formatter.format(bmi);
            tvBmiResult.setText(formattedBmi);

            int categoryResId;
            if (bmi < 16.0) {
                categoryResId = R.string.bmi_cat_severe_thinness;
            } else if (bmi < 17.0) {
                categoryResId = R.string.bmi_cat_moderate_thinness;
            } else if (bmi < 18.5) {
                categoryResId = R.string.bmi_cat_mild_thinness;
            } else if (bmi < 25.0) {
                categoryResId = R.string.bmi_cat_normal;
            } else if (bmi < 30.0) {
                categoryResId = R.string.bmi_cat_overweight;
            } else if (bmi < 35.0) {
                categoryResId = R.string.bmi_cat_obese_class_1;
            } else if (bmi < 40.0) {
                categoryResId = R.string.bmi_cat_obese_class_2;
            } else {
                categoryResId = R.string.bmi_cat_obese_class_3;
            }

            tvCategoryResult.setText(categoryResId);
        });
    }
    private float currentFontScale = 1.0f;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // ตรวจสอบว่าขนาด fontScale ของเครื่องเปลี่ยน
        if (newConfig.fontScale != currentFontScale) {
            float systemFontScale = newConfig.fontScale;

            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_font_changed_title)
                    .setMessage(R.string.dialog_font_changed_msg)
                    .setCancelable(false)
                    // ปรับตามเครื่อง
                    .setPositiveButton(R.string.btn_use_system, (dialog, which) -> {
                        currentFontScale = systemFontScale;
                        applyFontScale(systemFontScale);
                    })
                    // คงค่าแอพเดิมไว้
                    .setNegativeButton(R.string.btn_keep_original, (dialog, which) -> {
                        currentFontScale = 1.0f;
                        applyFontScale(1.0f);
                    })
                    .show();
        }
    }
    private void applyFontScale(float scale) {
        Configuration config = getResources().getConfiguration();
        config.fontScale = scale;
        android.util.DisplayMetrics metrics = getResources().getDisplayMetrics();
        metrics.scaledDensity = config.fontScale * metrics.density;
        getBaseContext().getResources().updateConfiguration(config, metrics);
        recreate();
    }
}

class DecimalDigitsInputFilter implements InputFilter {
    private Pattern mPattern;

    DecimalDigitsInputFilter(int digits, int digitsAfterZero) {
        mPattern = Pattern.compile("[0-9]{0," + (digits - 1) + "}+((\\.[0-9]{0," + (digitsAfterZero - 1) + "})?)||(\\.)?");
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String replacement = source.subSequence(start, end).toString();
        String newVal = dest.subSequence(0, dstart).toString() + replacement + dest.subSequence(dend, dest.length()).toString();

        Matcher matcher = mPattern.matcher(newVal);
        if (!matcher.matches()) {
            return "";
        }
        return null;
    }
}