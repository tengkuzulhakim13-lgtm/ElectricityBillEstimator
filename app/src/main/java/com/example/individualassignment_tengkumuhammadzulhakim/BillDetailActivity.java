package com.example.individualassignment_tengkumuhammadzulhakim;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Arrays;

public class BillDetailActivity extends AppCompatActivity {

    long billId;
    DataHelper dbHelper;
    TextView tvMonth, tvUnits, tvTotal, tvRebate, tvFinal, tvRebateEditLabel;
    LinearLayout viewGroup, editGroup;
    Spinner spinnerMonthEdit;
    TextInputLayout tilUnitsEdit;
    TextInputEditText etUnits;
    SeekBar sbRebateEdit;
    Button btnEditSave, btnDelete, btnBack;
    boolean editing = false;
    String currentMonth;
    double currentUnits, currentRebate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_detail);
        setTitle("Bill Details");
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        billId = getIntent().getLongExtra("bill_id", -1);
        dbHelper = new DataHelper(this);

        tvMonth = findViewById(R.id.tvMonth);
        tvUnits = findViewById(R.id.tvUnits);
        tvTotal = findViewById(R.id.tvTotal);
        tvRebate = findViewById(R.id.tvRebate);
        tvFinal = findViewById(R.id.tvFinal);
        viewGroup = findViewById(R.id.viewGroup);
        editGroup = findViewById(R.id.editGroup);
        spinnerMonthEdit = findViewById(R.id.spinnerMonthEdit);
        tilUnitsEdit = findViewById(R.id.tilUnitsEdit);
        etUnits = findViewById(R.id.etUnitsEdit);
        sbRebateEdit = findViewById(R.id.sbRebateEdit);
        tvRebateEditLabel = findViewById(R.id.tvRebateEditLabel);
        btnEditSave = findViewById(R.id.btnEditSave);
        btnDelete = findViewById(R.id.btnDelete);
        btnBack = findViewById(R.id.btnBack);

        ArrayAdapter<String> adp = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, MainActivity.MONTHS);
        adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonthEdit.setAdapter(adp);

        sbRebateEdit.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean u) {
                tvRebateEditLabel.setText(getString(R.string.label_rebate, p));
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });

        if (!loadDetail()) {
            Toast.makeText(this, "Bill not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnEditSave.setOnClickListener(v -> {
            if (!editing) {
                enterEditMode();
            } else {
                updateRecord();
            }
        });

        btnDelete.setOnClickListener(v -> confirmDelete());
        btnBack.setOnClickListener(v -> {
            if (editing) exitEditMode(); else finish();
        });
    }

    private boolean loadDetail() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT month, units, totalCharges, rebate, finalCost FROM bills WHERE id = ?",
                new String[]{String.valueOf(billId)});
        boolean found = c.moveToFirst();
        if (found) {
            currentMonth = c.getString(0);
            currentUnits = c.getDouble(1);
            currentRebate = c.getDouble(3);
            tvMonth.setText("Month: " + currentMonth);
            tvUnits.setText("Units Used: " + TariffCalculator.formatUnits(currentUnits) + " kWh");
            tvTotal.setText("Total Charges: " + TariffCalculator.formatRM(c.getDouble(2)));
            tvRebate.setText("Rebate: " + (int) currentRebate + "%");
            tvFinal.setText("Final Cost: " + TariffCalculator.formatRM(c.getDouble(4)));
        }
        c.close();
        return found;
    }

    private void enterEditMode() {
        editing = true;
        int idx = Arrays.asList(MainActivity.MONTHS).indexOf(currentMonth);
        spinnerMonthEdit.setSelection(Math.max(idx, 0));
        etUnits.setText(TariffCalculator.formatUnits(currentUnits));
        sbRebateEdit.setProgress((int) currentRebate);
        tvRebateEditLabel.setText(getString(R.string.label_rebate, (int) currentRebate));
        tilUnitsEdit.setError(null);
        viewGroup.setVisibility(View.GONE);
        editGroup.setVisibility(View.VISIBLE);
        btnEditSave.setText("Save Changes");
        btnBack.setText("Cancel");
        btnDelete.setVisibility(View.GONE);
    }

    private void exitEditMode() {
        editing = false;
        viewGroup.setVisibility(View.VISIBLE);
        editGroup.setVisibility(View.GONE);
        btnEditSave.setText("Edit");
        btnBack.setText("Back");
        btnDelete.setVisibility(View.VISIBLE);
    }

    private void updateRecord() {
        String unitsStr = etUnits.getText() == null ? "" : etUnits.getText().toString().trim();
        double newUnits = MainActivity.parseUnits(unitsStr, tilUnitsEdit, this);
        if (newUnits < 0) {
            etUnits.requestFocus();
            return;
        }
        String newMonth = spinnerMonthEdit.getSelectedItem().toString();
        int newRebate = sbRebateEdit.getProgress();

        double newTotal = TariffCalculator.calculateTotal(newUnits);
        double newFinal = TariffCalculator.applyRebate(newTotal, newRebate);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("month", newMonth);
        cv.put("units", newUnits);
        cv.put("totalCharges", newTotal);
        cv.put("rebate", newRebate);
        cv.put("finalCost", newFinal);
        int rows = db.update("bills", cv, "id = ?", new String[]{String.valueOf(billId)});

        exitEditMode();
        loadDetail();
        Toast.makeText(this, rows > 0 ? "Bill updated" : "Update failed", Toast.LENGTH_SHORT).show();
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Bill")
                .setMessage("Are you sure you want to delete the bill for " + currentMonth + "? This cannot be undone.")
                .setPositiveButton("Delete", (d, w) -> {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    db.delete("bills", "id = ?", new String[]{String.valueOf(billId)});
                    Toast.makeText(this, "Bill deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
