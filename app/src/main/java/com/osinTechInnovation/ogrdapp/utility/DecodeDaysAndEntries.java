package com.osinTechInnovation.ogrdapp.utility;

public class DecodeDaysAndEntries {
    public int decodeToAmountEntries(String amountEntriesWithDay) {
        String[] s = amountEntriesWithDay.split("_");
        int s1 = Integer.parseInt(s[1]);

        return s1;
    }

    public int decodeDays(String amountEntriesWithDays) {
        String[] s = amountEntriesWithDays.split("_");
        int s1 = Integer.parseInt(s[0]);

        return s1;
    }
}

