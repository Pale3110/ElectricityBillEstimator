package com.example.assignment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import java.util.List;
import java.util.Locale;

public class BillAdapter extends ArrayAdapter<BillRecord> {

    // Constructor: receives the screen and the list of bills to show
    public BillAdapter(Context context, List<BillRecord> bills) {
        super(context, 0, bills);
    }

    // Android calls this once for every row it needs to draw
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        // If there is no row to reuse yet, build one from our row layout
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.list_item_bill, parent, false);
        }

        // Get the bill that belongs to this row
        BillRecord bill = getItem(position);

        // Find the two TextViews in the row
        TextView tvMonth = convertView.findViewById(R.id.tvItemMonth);
        TextView tvFinal = convertView.findViewById(R.id.tvItemFinal);

        // Fill them in: month and final cost only
        tvMonth.setText(bill.getMonthName());
        tvFinal.setText(String.format(Locale.US, "RM %.2f", bill.finalCost));

        return convertView;
    }
}
