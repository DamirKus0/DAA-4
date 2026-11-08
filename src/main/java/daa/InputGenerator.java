package daa;

import java.util.Random;

public final class InputGenerator {
    public static final long SEED = 20261108;

    private InputGenerator() { }

    public static String[] edit(int n, boolean disjoint) {
        if (disjoint) return new String[]{"a".repeat(n), "b".repeat(n)};
        Random random = new Random(SEED + n);
        return new String[]{word(random, n), word(random, n)};
    }

    public static String[] search(int n, boolean lateMismatch) {
        int m = n / 64;
        if (lateMismatch) return new String[]{"a".repeat(n), "a".repeat(m - 1) + "b"};
        Random random = new Random(SEED + 100000 + n);
        StringBuilder text = new StringBuilder(word(random, n));
        String pattern = word(random, m);
        for (int start : new int[]{n / 4, n / 2, 3 * n / 4}) {
            text.replace(start, start + m, pattern);
        }
        return new String[]{text.toString(), pattern};
    }

    private static String word(Random random, int length) {
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) result.append((char) ('a' + random.nextInt(4)));
        return result.toString();
    }
}
