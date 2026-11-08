package daa;

import java.util.ArrayList;
import java.util.List;

public final class NaiveSearch implements StringSearch {
    private long operations;

    @Override
    public List<Integer> search(String text, String pattern) {
        operations = 0;
        if (pattern.isEmpty()) throw new IllegalArgumentException("Empty pattern");
        List<Integer> matches = new ArrayList<>();
        for (int start = 0; start <= text.length() - pattern.length(); start++) {
            int j = 0;
            while (j < pattern.length()) {
                operations++;
                if (text.charAt(start + j) != pattern.charAt(j)) break;
                j++;
            }
            if (j == pattern.length()) matches.add(start);
        }
        return matches;
    }

    @Override
    public long operations() {
        return operations;
    }
}
