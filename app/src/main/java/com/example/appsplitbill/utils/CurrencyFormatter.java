package com.example.appsplitbill.utils;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFormatter {
    public static String formatRupiah(double amount) {
        try {
            Locale localeID = new Locale("in", "ID");
            NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);
            String formatted = formatRupiah.format(amount);
            
            // Clean up the format
            formatted = formatted.replace("Rp", "Rp ");
            if (formatted.endsWith(",00")) {
                formatted = formatted.substring(0, formatted.length() - 3);
            }
            return formatted;
        } catch (Exception e) {
            return "Rp " + String.format(Locale.US, "%,.0f", amount).replace(",", ".");
        }
    }
}
