package daa;

public final class TableEditDistance implements EditDistance {
    private long operations;

    // State: dp[i][j] is the distance between the first i and j characters.
    // Transition: min(dp[i-1][j]+1, dp[i][j-1]+1, dp[i-1][j-1]+mismatch).
    // Base: dp[0][j]=j and dp[i][0]=i.
    @Override
    public int distance(String a, String b) {
        operations = 0;
        int n = a.length();
        int m = b.length();
        int[][] dp = new int[n + 1][m + 1];
        for (int i = 0; i <= n; i++) dp[i][0] = i;
        for (int j = 0; j <= m; j++) dp[0][j] = j;
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                operations++;
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(dp[i - 1][j - 1] + cost,
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
            }
        }
        return dp[n][m];
    }

    @Override
    public long operations() {
        return operations;
    }
}
