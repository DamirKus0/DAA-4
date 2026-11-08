# DAA Assignment 4

Damir Kusmagambetov, SE-2525.

Java 17+ and Maven 3. No runtime dependencies; JUnit 5 is used only for tests.

Build and run all tests:

```sh
mvn clean verify
```

Run the experiment after building:

```sh
java -cp target/classes daa.Experiment
```

Rebuild all four PNG plots using standard Java graphics:

```sh
java -Djava.awt.headless=true -cp target/classes daa.PlotResults
```

The experiment overwrites `results/results.csv` and `results/environment.txt`.
Seed `20261108` fixes inputs, answers and counters; elapsed times vary.
Each case has two unrecorded warm-ups and five measured runs. Plots use their
median; the CSV keeps every run. See [REPORT.md](REPORT.md) for the design,
results and limitations, or [the four-page PDF](output/pdf/REPORT.pdf).
Rerunning changes the measured data, so update report numbers if needed.

CSV columns: `algorithm,input,n,m,run,time_ms,operations,result`.
`operations` counts recursive calls, interior DP cell updates, or character
comparisons (including KMP preprocessing). `result` is the edit distance or
number of matches. Counters reset on every algorithm call.

Implementations are in `src/main/java/daa`, tests in `src/test/java/daa`.
The two families share `EditDistance` and `StringSearch` interfaces. KMP exposes
`public int[] lps(String pattern)`. Search indices start at zero and include
overlaps; empty search patterns throw `IllegalArgumentException` and overlong
patterns return an empty list. Strings are case-sensitive Java UTF-16 `char`
sequences, without normalization.

Repository: [DamirKus0/DAA-4](https://github.com/DamirKus0/DAA-4), branch `main`,
final tag `v1.0`. The submission ZIP includes source, tests, results, plots,
both report formats and this README; upload it to Moodle.

AI-use acknowledgement: OpenAI Codex assisted with implementation, tests,
experiment design, measurements, plots and report preparation. All reported
measurements came from actual local runs; no measurement values were invented.
