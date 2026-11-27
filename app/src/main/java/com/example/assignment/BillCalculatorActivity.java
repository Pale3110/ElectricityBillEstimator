package com.example.assignment;

public class BillCalculatorActivity {
    // The 12 month names, shared by the whole app
    public static final String[] MONTHS = {"January", "February", "March", "April",
            "May", "June", "July", "August", "September", "October", "November", "December"};

    //takes units and return total charges
    public static double calculateTotal(int units){
        double total = 0;   //total money so far
        int remaining = units; //remaining = kWh left to be charged

        //first 200kWh-- 0.218
        if(remaining>0){
            int used = Math.min(remaining,200);     //Math.min(a,b)-- picks smaller number of two numbers
            total += used*0.218;
            remaining -= used;
        }
        //next 100kWh-- 0.334
        if(remaining>0){
            int used = Math.min(remaining,100);
            total += used*0.334;
            remaining -= used;
        }
        //next300kWh-- 0.516
        if(remaining>0){
            int used = Math.min(remaining,300);
            total += used*0.516;
            remaining -= used;
        }
        if(remaining>0){
            total += remaining*0.546;
        }

        return total;
    }

    //takes total charges and rebate %. then, return final cost
    public static double calculateFinal(double total, double rebatePercent){
        double rebateAmount = total*rebatePercent / 100;
        return total - rebateAmount;
    }
}

