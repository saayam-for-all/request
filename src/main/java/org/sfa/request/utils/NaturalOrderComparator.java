package org.sfa.request.utils;

import java.util.Comparator;

public class NaturalOrderComparator implements Comparator<String> {
    @Override
    public int compare(String a, String b) {
        try {
            // Try to split by dots and compare each level as integers
            String[] aParts = a.split("\\.");
            String[] bParts = b.split("\\.");
            int length = Math.max(aParts.length, bParts.length);

            for (int i = 0; i < length; i++) {
                int aVal = i < aParts.length ? Integer.parseInt(aParts[i]) : 0;
                int bVal = i < bParts.length ? Integer.parseInt(bParts[i]) : 0;
                if (aVal != bVal) {
                    return Integer.compare(aVal, bVal);
                }
            }
            return 0;
        } catch (NumberFormatException e) {
            return a.compareTo(b);
        }
    }

}
