package com.example.assignment;

public class BillRecord {

    public int id;            // unique number the database gives each row
    public int month;         // 0 = January ... 11 = December
    public int units;         // kWh used
    public double rebate;     // rebate percentage
    public double total;      // total charges
    public double finalCost;  // cost after rebate

    // Constructor: runs when we create a new BillRecord, and fills in the values
    public BillRecord(int id, int month, int units, double rebate,
                      double total, double finalCost) {
        this.id = id;
        this.month = month;
        this.units = units;
        this.rebate = rebate;
        this.total = total;
        this.finalCost = finalCost;
    }

    // Turns the month number into its name, e.g. 0 becomes "January"
    public String getMonthName() {
        return BillCalculatorActivity.MONTHS[month];
    }
}