package daa;

public final class TwoRowEditDistance implements EditDistance {
    private long operations;

    // State: previous[j]=D(i-1,j), current[j]=D(i,j), with the shorter string in columns.
    // Transition: min(previous[j]+1, current[j-1]+1, previous[j-1]+mismatch).
    // Base: previous[j]=j for i=0; current[0]=i for each subsequent row.
    @Override
    public int distance(String a, String b) {
        operations = 0;
        if (a.length() < b.length()) {
            String temp = a;
            a = b;
            b = temp;
        }
        int n = a.length();
        int m = b.length();
        int[] previous = new int[m + 1];
        int[] current = new int[m + 1];
        for (int j = 0; j <= m; j++) previous[j] = j;
        for (int i = 1; i <= n; i++) {
            current[0] = i;
            for (int j = 1; j <= m; j++) {
                operations++;
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(previous[j - 1] + cost,
                        Math.min(previous[j] + 1, current[j - 1] + 1));
            }
            int[] temp = previous;
            previous = current;
            current = temp;
        }
        return previous[m];
    }

    @Override
    public long operations() {
        return operations;
    }
}
