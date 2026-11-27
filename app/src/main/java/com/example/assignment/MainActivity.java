package com.example.assignment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;
import java.util.Locale;

import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    // These are "boxes" that will hold the items on our screen
    Spinner spMonth;
    TextInputLayout tilUnits;
    TextInputEditText etUnits;
    Slider sliderRebate;
    TextView tvRebateValue, tvTotal, tvFinal;
    View cardResult;
    Button btnCalculate, btnSave, btnViewSaved;
    DatabaseHelper dbHelper;

    // onCreate runs once, when the screen opens
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect each box to its item in the layout, using the ids we gave them
        spMonth = findViewById(R.id.spMonth);
        tilUnits = findViewById(R.id.tilUnits);
        etUnits = findViewById(R.id.etUnits);
        sliderRebate = findViewById(R.id.sliderRebate);
        tvRebateValue = findViewById(R.id.tvRebateValue);
        tvTotal = findViewById(R.id.tvTotal);
        tvFinal = findViewById(R.id.tvFinal);
        cardResult = findViewById(R.id.cardResult);
        btnCalculate = findViewById(R.id.btnCalculate);
        btnSave = findViewById(R.id.btnSave);
        btnViewSaved = findViewById(R.id.btnViewSaved);

        dbHelper = new DatabaseHelper(this);   // opens (or creates) the database

        setupMonthSpinner();
        setupSlider();

        // When the Calculate button is clicked, run calculateAndShow()
        btnCalculate.setOnClickListener(v -> calculateAndShow());
        // When the Save button is clicked, run saveBill()
        btnSave.setOnClickListener(v -> saveBill());

        // When "View saved bills" is clicked, open the list screen
        btnViewSaved.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
    }

    // Puts the 12 month names into the drop-down list
    private void setupMonthSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, BillCalculatorActivity.MONTHS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMonth.setAdapter(adapter);
    }

    // Makes the rebate text change while the slider moves
    private void setupSlider() {
        tvRebateValue.setText("0.0%");   // starting text

        sliderRebate.addOnChangeListener((slider, value, fromUser) -> {
            tvRebateValue.setText(String.format(Locale.US, "%.1f%%", value));
        });
    }

    // Checks the input, calculates, and shows the result.
    // Returns true if everything was OK, false if the input was wrong.
    private boolean calculateAndShow() {

        // Hide the old result first
        cardResult.setVisibility(View.GONE);

        // 1. Read what the user typed
        String text = etUnits.getText().toString().trim();

        // 2. Check: is it empty?
        if (text.isEmpty()) {
            tilUnits.setError("Please enter the units used");
            return false;
        }

        // 3. Check: is it a valid whole number?
        int units;
        try {
            units = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            tilUnits.setError("Please enter a valid whole number");
            return false;
        }

        // 4. Check: is it between 1 and 1000?
        if (units < 1 || units > 1000) {
            tilUnits.setError("Units must be between 1 and 1000 kWh");
            return false;
        }

        // Input is good, so remove any old error message
        tilUnits.setError(null);

        // 5. Calculate
        double rebate = sliderRebate.getValue();
        double total = BillCalculatorActivity.calculateTotal(units);
        double finalCost = BillCalculatorActivity.calculateFinal(total, rebate);

        // 6. Show the result, with 2 decimal places
        tvTotal.setText(String.format(Locale.US, "RM %.2f", total));
        tvFinal.setText(String.format(Locale.US, "RM %.2f", finalCost));
        cardResult.setVisibility(View.VISIBLE);

        return true;
    }

    // Saves the current calculation into the database
    private void saveBill() {

        // First make sure the input is valid. If not, stop here.
        boolean inputIsOk = calculateAndShow();
        if (!inputIsOk) {
            return;
        }

        // Collect the values
        int month = spMonth.getSelectedItemPosition();   // 0 = January ... 11 = December
        int units = Integer.parseInt(etUnits.getText().toString().trim());
        double rebate = sliderRebate.getValue();
        double total = BillCalculatorActivity.calculateTotal(units);
        double finalCost = BillCalculatorActivity.calculateFinal(total, rebate);

        // Put them in a BillRecord (id is 0 for now)
        BillRecord bill = new BillRecord(0, month, units, rebate, total, finalCost);

        // Is there already a bill saved for this month?
        int existingId = dbHelper.findIdByMonth(month);

        if (existingId == -1) {
            // No, so add a new row
            dbHelper.insertBill(bill);
            Toast.makeText(this, BillCalculatorActivity.MONTHS[month] + " bill saved",
                    Toast.LENGTH_SHORT).show();
        } else {
            // Yes, so update the old row instead of creating a duplicate
            bill.id = existingId;
            dbHelper.updateBill(bill);
            Toast.makeText(this, BillCalculatorActivity.MONTHS[month] + " bill updated",
                    Toast.LENGTH_SHORT).show();
        }
    }
    // Puts the two icons into the title bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    // Runs when a title bar icon is tapped
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_history) {
            startActivity(new Intent(MainActivity.this, HistoryActivity.class));
            return true;
        } else if (id == R.id.action_about) {
            startActivity(new Intent(MainActivity.this, AboutActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}