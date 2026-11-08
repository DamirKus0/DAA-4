package daa;

import java.util.ArrayList;
import java.util.List;

public final class KmpSearch implements StringSearch {
    private long operations;

    public int[] lps(String pattern) {
        operations = 0;
        int[] lps = new int[pattern.length()];
        int i = 1;
        int length = 0;
        while (i < pattern.length()) {
            operations++;
            if (pattern.charAt(i) == pattern.charAt(length)) {
                lps[i++] = ++length;
            } else if (length > 0) {
                length = lps[length - 1];
            } else {
                lps[i++] = 0;
            }
        }
        return lps;
    }

    @Override
    public List<Integer> search(String text, String pattern) {
        operations = 0;
        if (pattern.isEmpty()) throw new IllegalArgumentException("Empty pattern");
        List<Integer> matches = new ArrayList<>();
        if (pattern.length() > text.length()) return matches;
        int[] lps = lps(pattern);
        int i = 0;
        int j = 0;
        while (i < text.length()) {
            operations++;
            if (text.charAt(i) == pattern.charAt(j)) {
                i++;
                j++;
                if (j == pattern.length()) {
                    matches.add(i - j);
                    j = lps[j - 1];
                }
            } else if (j > 0) {
                j = lps[j - 1];
            } else {
                i++;
            }
        }
        return matches;
    }

    @Override
    public long operations() {
        return operations;
    }
}
