package com.example.assignment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    ListView listBills;
    DatabaseHelper dbHelper;
    List<BillRecord> bills;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Show a back arrow in the title bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        listBills = findViewById(R.id.listBills);
        dbHelper = new DatabaseHelper(this);

        // Show the "No saved bills yet" message when the list is empty
        listBills.setEmptyView(findViewById(R.id.tvEmpty));

        // When a row is tapped, open the detail page for that bill
        listBills.setOnItemClickListener((parent, view, position, rowId) -> {
            BillRecord clicked = bills.get(position);

            Intent intent = new Intent(HistoryActivity.this, DetailActivity.class);
            intent.putExtra("bill_id", clicked.id);   // send the id to the next page
            startActivity(intent);
        });
    }

    // onResume runs every time this screen becomes visible again,
    // so the list is refreshed after we come back from the detail page
    @Override
    protected void onResume() {
        super.onResume();
        bills = dbHelper.getAllBills();
        listBills.setAdapter(new BillAdapter(this, bills));
    }

    // Makes the back arrow in the title bar work
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();   // close this screen and go back
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}