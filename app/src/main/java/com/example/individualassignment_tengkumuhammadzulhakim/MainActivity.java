package com.example.individualassignment_tengkumuhammadzulhakim;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class MainActivity extends AppCompatActivity {

    public static final String[] MONTHS = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};

    Spinner spinnerMonth;
    TextInputLayout tilUnits;
    TextInputEditText etUnits;
    SeekBar sbRebate;
    TextView tvRebateLabel, tvResultSummary, tvTotalCharges, tvFinalCost;
    LinearLayout resultCard;
    Button btnCalculate, btnClear, btnViewList, btnAbout;
    DataHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.app_name);

        spinnerMonth = findViewById(R.id.spinnerMonth);
        tilUnits = findViewById(R.id.tilUnits);
        etUnits = findViewById(R.id.etUnits);
        sbRebate = findViewById(R.id.sbRebate);
        tvRebateLabel = findViewById(R.id.tvRebateLabel);
        resultCard = findViewById(R.id.resultCard);
        tvResultSummary = findViewById(R.id.tvResultSummary);
        tvTotalCharges = findViewById(R.id.tvTotalCharges);
        tvFinalCost = findViewById(R.id.tvFinalCost);
        btnCalculate = findViewById(R.id.btnCalculate);
        btnClear = findViewById(R.id.btnClear);
        btnViewList = findViewById(R.id.btnViewList);
        btnAbout = findViewById(R.id.btnAbout);

        dbHelper = new DataHelper(this);

        ArrayAdapter<String> adp = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MONTHS);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adp);

        tvRebateLabel.setText(getString(R.string.label_rebate, 0));
        sbRebate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int p, boolean fromUser) {
                tvRebateLabel.setText(getString(R.string.label_rebate, p));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Remove the error message as soon as the user starts correcting the input
        etUnits.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { tilUnits.setError(null); }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnCalculate.setOnClickListener(v -> calculateAndSave());
        btnClear.setOnClickListener(v -> clearForm());
        btnViewList.setOnClickListener(v -> startActivity(new Intent(this, BillListActivity.class)));
        btnAbout.setOnClickListener(v -> startActivity(new Intent(this, AboutActivity.class)));
    }

    /** Validates the units input. Returns the value, or -1 if invalid (error is shown on the field). */
    static double parseUnits(String unitsStr, TextInputLayout til, Context ctx) {
        if (unitsStr.isEmpty()) {
            til.setError(ctx.getString(R.string.err_units_empty));
            return -1;
        }
        double units;
        try {
            units = Double.parseDouble(unitsStr);
        } catch (NumberFormatException e) {
            til.setError(ctx.getString(R.string.err_units_invalid));
            return -1;
        }
        if (units < 1 || units > 1000) {
            til.setError(ctx.getString(R.string.err_units_range));
            return -1;
        }
        til.setError(null);
        return units;
    }

    private void calculateAndSave() {
        String month = spinnerMonth.getSelectedItem().toString();
        String unitsStr = etUnits.getText() == null ? "" : etUnits.getText().toString().trim();

        double units = parseUnits(unitsStr, tilUnits, this);
        if (units < 0) {
            etUnits.requestFocus();
            return;
        }

        int rebate = sbRebate.getProgress();
        double total = TariffCalculator.calculateTotal(units);
        double finalCost = TariffCalculator.applyRebate(total, rebate);

        tvResultSummary.setText(month + " · " + TariffCalculator.formatUnits(units) + " kWh · Rebate " + rebate + "%");
        tvTotalCharges.setText("Total Charges: " + TariffCalculator.formatRM(total));
        tvFinalCost.setText("Final Cost: " + TariffCalculator.formatRM(finalCost));
        resultCard.setVisibility(View.VISIBLE);
        hideKeyboard();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("month", month);
        cv.put("units", units);
        cv.put("totalCharges", total);
        cv.put("rebate", rebate);
        cv.put("finalCost", finalCost);
        long id = db.insert("bills", null, cv);

        if (id == -1) {
            Toast.makeText(this, "Failed to save the bill. Please try again.", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Bill saved (" + month + "). Tap \"View Saved Bills\" to see it.", Toast.LENGTH_LONG).show();
        }
    }

    private void clearForm() {
        spinnerMonth.setSelection(0);
        etUnits.setText("");
        tilUnits.setError(null);
        sbRebate.setProgress(0);
        resultCard.setVisibility(View.GONE);
        Toast.makeText(this, "Form cleared", Toast.LENGTH_SHORT).show();
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
