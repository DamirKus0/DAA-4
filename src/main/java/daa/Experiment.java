package daa;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;

public final class Experiment {
    private static volatile int sink;

    private Experiment() { }

    public static void main(String[] args) throws IOException {
        Path directory = Path.of("results");
        Files.createDirectories(directory);
        Files.writeString(directory.resolve("environment.txt"),
                "Java: " + System.getProperty("java.runtime.version") + "\n"
                + "VM: " + System.getProperty("java.vm.name") + "\n"
                + "OS: " + System.getProperty("os.name") + " "
                + System.getProperty("os.version") + " " + System.getProperty("os.arch") + "\n"
                + "Available processors: " + Runtime.getRuntime().availableProcessors() + "\n"
                + "Max heap bytes: " + Runtime.getRuntime().maxMemory() + "\n"
                + "Seed: " + InputGenerator.SEED + "\nWarmups per case: 2\nMeasured runs per case: 5\n");
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(directory.resolve("results.csv")))) {
            out.println("algorithm,input,n,m,run,time_ms,operations,result");
            EditDistance[] edits = {
                    new RecursiveEditDistance(), new TableEditDistance(), new TwoRowEditDistance()
            };
            String[] editNames = {"recursive", "table", "two_rows"};
            for (int index = 0; index < edits.length; index++) {
                EditDistance algorithm = edits[index];
                int[] sizes = index == 0 ? new int[]{2, 4, 6, 8, 10, 12}
                        : new int[]{128, 256, 512, 1024, 2048};
                for (boolean disjoint : new boolean[]{false, true}) {
                    for (int n : sizes) {
                        String[] input = InputGenerator.edit(n, disjoint);
                        int expected = new TableEditDistance().distance(input[0], input[1]);
                        measure(out, editNames[index], disjoint ? "disjoint" : "random", n, n,
                                () -> algorithm.distance(input[0], input[1]), algorithm::operations, expected);
                    }
                }
            }
            StringSearch[] searches = {new NaiveSearch(), new KmpSearch()};
            String[] searchNames = {"naive", "kmp"};
            for (boolean lateMismatch : new boolean[]{false, true}) {
                for (int n : new int[]{2048, 4096, 8192, 16384, 32768}) {
                    String[] input = InputGenerator.search(n, lateMismatch);
                    var expected = searches[0].search(input[0], input[1]);
                    if (!expected.equals(searches[1].search(input[0], input[1]))) {
                        throw new IllegalStateException("Search disagreement at n=" + n);
                    }
                    for (int index = 0; index < searches.length; index++) {
                        StringSearch algorithm = searches[index];
                        measure(out, searchNames[index], lateMismatch ? "late_mismatch" : "random",
                                n, input[1].length(), () -> algorithm.search(input[0], input[1]).size(),
                                algorithm::operations, expected.size());
                    }
                }
            }
            if (out.checkError()) throw new IOException("Cannot write results.csv");
        }
        System.out.println("Saved results/results.csv");
    }

    private static void measure(PrintWriter out, String algorithm, String input, int n, int m,
                                IntSupplier action, LongSupplier operations, int expected) {
        for (int warmup = 0; warmup < 2; warmup++) sink = action.getAsInt();
        long firstCount = -1;
        for (int run = 1; run <= 5; run++) {
            long start = System.nanoTime();
            int result = action.getAsInt();
            long elapsed = System.nanoTime() - start;
            long count = operations.getAsLong();
            sink = result;
            if (result != expected || (firstCount >= 0 && firstCount != count)) {
                throw new IllegalStateException("Unstable result or counter: " + algorithm);
            }
            firstCount = count;
            out.printf(Locale.ROOT, "%s,%s,%d,%d,%d,%.6f,%d,%d%n",
                    algorithm, input, n, m, run, elapsed / 1_000_000.0, count, result);
        }
        System.out.printf("%s %s n=%d operations=%d%n", algorithm, input, n, firstCount);
    }
}
