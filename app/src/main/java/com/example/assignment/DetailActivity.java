package com.example.assignment;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.slider.Slider;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    DatabaseHelper dbHelper;
    BillRecord bill;           // the bill we are showing
    boolean editing = false;   // true while the user is in edit mode

    Spinner spDetailMonth;
    TextInputLayout tilDetailUnits;
    TextInputEditText etDetailUnits;
    Slider sliderDetailRebate;
    TextView tvDetailRebate, tvDetailTotal, tvDetailFinal;
    Button btnEdit, btnCancel, btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Show a back arrow in the title bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Connect every box to its item in the layout
        spDetailMonth = findViewById(R.id.spDetailMonth);
        tilDetailUnits = findViewById(R.id.tilDetailUnits);
        etDetailUnits = findViewById(R.id.etDetailUnits);
        sliderDetailRebate = findViewById(R.id.sliderDetailRebate);
        tvDetailRebate = findViewById(R.id.tvDetailRebate);
        tvDetailTotal = findViewById(R.id.tvDetailTotal);
        tvDetailFinal = findViewById(R.id.tvDetailFinal);
        btnEdit = findViewById(R.id.btnEdit);
        btnCancel = findViewById(R.id.btnCancel);
        btnDelete = findViewById(R.id.btnDelete);

        dbHelper = new DatabaseHelper(this);

        // Load the bill whose id was sent from the list screen
        int billId = getIntent().getIntExtra("bill_id", -1);
        bill = dbHelper.getBill(billId);

        // Safety check: if the bill wasn't found, close the screen
        if (bill == null) {
            Toast.makeText(this, "Bill not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fill the month drop-down
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, BillCalculatorActivity.MONTHS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDetailMonth.setAdapter(adapter);

        // Make the rebate text follow the slider
        sliderDetailRebate.addOnChangeListener((slider, value, fromUser) ->
                tvDetailRebate.setText(String.format(Locale.US, "%.1f%%", value)));

        // Button clicks
        btnEdit.setOnClickListener(v -> {
            if (editing) {
                saveChanges();       // the button says "Save changes"
            } else {
                setEditing(true);    // the button says "Edit"
            }
        });

        btnCancel.setOnClickListener(v -> {
            showBill();              // put the original values back
            setEditing(false);
        });

        btnDelete.setOnClickListener(v -> confirmDelete());

        // Show the bill, in normal (not editing) mode
        showBill();
        setEditing(false);
    }

    // Puts the bill's saved information into the fields
    private void showBill() {
        spDetailMonth.setSelection(bill.month);
        etDetailUnits.setText(String.valueOf(bill.units));
        sliderDetailRebate.setValue((float) bill.rebate);
        tvDetailRebate.setText(String.format(Locale.US, "%.1f%%", bill.rebate));
        tvDetailTotal.setText(String.format(Locale.US, "RM %.2f", bill.total));
        tvDetailFinal.setText(String.format(Locale.US, "RM %.2f", bill.finalCost));
        tilDetailUnits.setError(null);
    }

    // Switches between viewing mode (false) and editing mode (true)
    private void setEditing(boolean enable) {
        editing = enable;

        // Fields can only be changed while editing
        spDetailMonth.setEnabled(enable);
        etDetailUnits.setEnabled(enable);
        sliderDetailRebate.setEnabled(enable);

        if (enable) {
            btnEdit.setText(R.string.save_changes);
            btnCancel.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.GONE);
        } else {
            btnEdit.setText(R.string.edit);
            btnCancel.setVisibility(View.GONE);
            btnDelete.setVisibility(View.VISIBLE);
        }
    }

    // Checks the new values, recalculates, and updates the database
    private void saveChanges() {

        // 1. Read the units box (an empty string if there is nothing)
        String text = "";
        if (etDetailUnits.getText() != null) {
            text = etDetailUnits.getText().toString().trim();
        }

        if (text.isEmpty()) {
            tilDetailUnits.setError("Please enter the units used");
            return;
        }

        int units;
        try {
            units = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            tilDetailUnits.setError("Please enter a valid whole number");
            return;
        }

        if (units < 1 || units > 1000) {
            tilDetailUnits.setError("Units must be between 1 and 1000 kWh");
            return;
        }
        tilDetailUnits.setError(null);

        // 2. If the month was changed, make sure that month isn't already used
        int newMonth = spDetailMonth.getSelectedItemPosition();
        int otherId = dbHelper.findIdByMonth(newMonth);

        if (otherId != -1 && otherId != bill.id) {
            Toast.makeText(this, BillCalculatorActivity.MONTHS[newMonth]
                    + " already has a saved bill", Toast.LENGTH_LONG).show();
            return;
        }

        // 3. Recalculate with the new values
        double rebate = sliderDetailRebate.getValue();
        double total = BillCalculatorActivity.calculateTotal(units);
        double finalCost = BillCalculatorActivity.calculateFinal(total, rebate);

        // 4. Update our bill object, then the database
        bill.month = newMonth;
        bill.units = units;
        bill.rebate = rebate;
        bill.total = total;
        bill.finalCost = finalCost;
        dbHelper.updateBill(bill);

        // 5. Refresh the screen and leave edit mode
        showBill();
        setEditing(false);
        Toast.makeText(this, "Changes saved", Toast.LENGTH_SHORT).show();
    }

    // Asks "are you sure?" and deletes the bill if the user agrees
    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message, bill.getMonthName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    dbHelper.deleteBill(bill.id);
                    Toast.makeText(this, "Bill deleted", Toast.LENGTH_SHORT).show();
                    finish();   // close this screen and go back to the list
                })
                .setNegativeButton(R.string.cancel, null)   // null = do nothing
                .show();
    }

    // Makes the back arrow in the title bar work
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}