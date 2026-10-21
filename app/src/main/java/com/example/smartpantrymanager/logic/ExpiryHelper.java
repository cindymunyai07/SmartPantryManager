package com.example.smartpantrymanager.logic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Works out how many days remain until an ingredient's expiry date. */
public class ExpiryHelper {

    private ExpiryHelper() { }

    /** Number of days from today until the given yyyy-MM-dd date (negative if past). */
    public static int daysUntil(String expiry) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            format.setLenient(false);
            Date expiryDate = format.parse(expiry);

            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);

            long diff = expiryDate.getTime() - today.getTimeInMillis();
            return (int) Math.round(diff / 86400000.0);
        } catch (ParseException e) {
            return Integer.MAX_VALUE;   // unreadable date: treat as "not soon"
        }
    }
}