package com.example.individualassignment_tengkumuhammadzulhakim;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class BillListActivity extends AppCompatActivity {
    ListView listView;
    TextView tvEmpty;
    DataHelper dbHelper;
    ArrayList<Long> ids = new ArrayList<>();
    ArrayList<String> displayList = new ArrayList<>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_list);
        setTitle("Saved Bills");
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        listView = findViewById(R.id.listViewBills);
        tvEmpty = findViewById(R.id.tvEmpty);
        listView.setEmptyView(tvEmpty);
        dbHelper = new DataHelper(this);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Intent i = new Intent(this, BillDetailActivity.class);
            i.putExtra("bill_id", ids.get(position));
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadList();
    }

    private void loadList() {
        ids.clear();
        displayList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Display Month and Final cost only
        Cursor c = db.rawQuery("SELECT id, month, finalCost FROM bills ORDER BY id DESC", null);
        while (c.moveToNext()) {
            ids.add(c.getLong(0));
            displayList.add(c.getString(1) + "   —   " + TariffCalculator.formatRM(c.getDouble(2)));
        }
        c.close();
        adapter.notifyDataSetChanged();
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
