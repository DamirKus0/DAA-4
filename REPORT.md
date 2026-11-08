# Experiment Design Card

Written before the first experiment run; the initial Git commit preserves this plan.

| Field | Edit Distance | String search |
| --- | --- | --- |
| Question | How do recursion and DP scale, and do two rows preserve the table's work count? | Does KMP avoid Naive's repeated comparisons on a late mismatch? |
| Sizes | Recursive: 2, 4, 6, 8, 10, 12; table and two rows: 128, 256, 512, 1024, 2048. Both strings have length n. Small recursive sizes limit exponential work; DP sizes expose quadratic growth. | Text n = 2048, 4096, 8192, 16384, 32768; pattern m = n/64. Growing both lengths separates quadratic Naive from linear KMP. |
| Inputs | Random strings over abcd show typical mismatches. Disjoint a^n and b^n force all three recursive branches and every DP cell. | Random text and pattern over abcd, with the pattern inserted at n/4, n/2 and 3n/4, show ordinary search with known matches. Late mismatch: text a^n, pattern a^(m-1)b, forces long failed comparisons at every Naive start. |
| Metrics | Count recursive calls (including bases), or interior DP cell updates, inside each implementation. These are different work units, so compare their growth rather than treating a call as identical to a cell. Record time_ms for each of five measured runs and plot the median. | Count character comparisons, including KMP LPS construction. This captures repeated work and preprocessing. Record five runs and plot median time_ms. |
| Prediction | Disjoint recursion grows exponentially; random recursion is smaller. Both DP versions perform n^2 updates; two rows use linear auxiliary space. | Late-mismatch Naive grows as n*m; KMP grows as n+m. Random inputs need much less repeated work; KMP may have overhead at small sizes. |
| Plots | One work-count chart with log axes; one median-time chart with log axes. | One comparison-count chart with log axes; one median-time chart with log axes. Each distinguishes input and algorithm. |

Protocol: custom input generator backed by java.util.Random, seed 20261108;
fresh deterministic seed per family, input and size. Two unrecorded warm-up runs
per case, then five measured runs of the same input. Use System.nanoTime and
write one CSV row per measured run. Inputs, answers and counters must repeat;
wall-clock times naturally vary. Run sequentially and keep all measured runs.

The planned recursive limit is n=12, not an estimate of the largest feasible n.
Report the measured cost there and explain the practical exponential limit.
