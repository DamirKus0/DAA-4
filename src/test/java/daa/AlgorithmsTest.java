package daa;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class AlgorithmsTest {
    private final EditDistance[] distances = {
            new RecursiveEditDistance(), new TableEditDistance(), new TwoRowEditDistance()
    };
    private final StringSearch[] searches = {new NaiveSearch(), new KmpSearch()};

    @Test
    void referenceDistances() {
        for (EditDistance algorithm : distances) {
            assertEquals(3, algorithm.distance("kitten", "sitting"));
            assertEquals(2, algorithm.distance("flaw", "lawn"));
            assertEquals(5, algorithm.distance("intention", "execution"));
            assertEquals(3, algorithm.distance("", "abc"));
            assertEquals(0, algorithm.distance("abc", "abc"));
        }
    }

    @Test
    void emptyAndUnequalDistances() {
        for (EditDistance algorithm : distances) {
            assertEquals(0, algorithm.distance("", ""));
            assertEquals(3, algorithm.distance("abc", ""));
            assertEquals(4, algorithm.distance("a", "aaaaa"));
            assertEquals(4, algorithm.distance("aaaaa", "a"));
            assertEquals(2, algorithm.distance("Abylai Han", "Abylai Khan"));
        }
    }

    @Test
    void hundredRandomDistancesAgree() {
        Random random = new Random(20261108);
        for (int test = 0; test < 100; test++) {
            String a = randomString(random, random.nextInt(7));
            String b = randomString(random, random.nextInt(7));
            int expected = distances[0].distance(a, b);
            for (EditDistance algorithm : distances) {
                assertEquals(expected, algorithm.distance(a, b), a + " / " + b);
                assertEquals(expected, algorithm.distance(b, a));
            }
        }
    }

    @Test
    void referenceLps() {
        KmpSearch kmp = new KmpSearch();
        assertArrayEquals(new int[]{0, 1, 2, 0, 1, 2, 3, 3}, kmp.lps("AAACAAAA"));
        assertArrayEquals(new int[]{0, 0, 1, 2, 0, 1, 2, 3, 4}, kmp.lps("ABABCABAB"));
        assertArrayEquals(new int[0], kmp.lps(""));
        assertArrayEquals(new int[]{0}, kmp.lps("A"));
    }

    @Test
    void referenceAndOverlappingMatches() {
        for (StringSearch algorithm : searches) {
            assertEquals(List.of(10), algorithm.search("ABABDABACDABABCABAB", "ABABCABAB"));
            assertEquals(List.of(0, 1, 2, 3), algorithm.search("AAAAA", "AA"));
            assertEquals(List.of(0, 2, 4), algorithm.search("ABABABA", "ABA"));
            assertEquals(List.of(0, 1, 2), algorithm.search("aaa", "a"));
            assertEquals(List.of(0), algorithm.search("abc", "abc"));
        }
    }

    @Test
    void searchEdges() {
        for (StringSearch algorithm : searches) {
            assertThrows(IllegalArgumentException.class, () -> algorithm.search("abc", ""));
            assertThrows(IllegalArgumentException.class, () -> algorithm.search("", ""));
            assertEquals(List.of(), algorithm.search("ab", "abc"));
            assertEquals(List.of(), algorithm.search("", "a"));
            assertEquals(List.of(), algorithm.search("aaaaa", "ab"));
        }
    }

    @Test
    void hundredRandomSearchesAgreeWithIndexOf() {
        Random random = new Random(42);
        for (int test = 0; test < 100; test++) {
            String text = randomString(random, random.nextInt(81));
            String pattern = randomString(random, 1 + random.nextInt(12));
            // Include guaranteed hits as well as absent and overlong patterns.
            if (test % 3 == 0 && !text.isEmpty()) {
                int start = random.nextInt(text.length());
                pattern = text.substring(start, Math.min(text.length(), start + 5));
            }
            List<Integer> expected = new ArrayList<>();
            for (int index = text.indexOf(pattern); index >= 0;
                 index = text.indexOf(pattern, index + 1)) expected.add(index);
            for (StringSearch algorithm : searches) {
                assertEquals(expected, algorithm.search(text, pattern), text + " / " + pattern);
            }
        }
    }

    @Test
    void editCountersReset() {
        for (EditDistance algorithm : distances) {
            algorithm.distance("ab", "cd");
            long first = algorithm.operations();
            algorithm.distance("ab", "cd");
            assertEquals(first, algorithm.operations());
            algorithm.distance("", "abc");
            assertEquals(algorithm instanceof RecursiveEditDistance ? 1 : 0, algorithm.operations());
        }
        distances[0].distance("a", "b");
        assertEquals(4, distances[0].operations());
        for (int i = 1; i < distances.length; i++) {
            distances[i].distance("abc", "de");
            assertEquals(6, distances[i].operations());
        }
    }

    @Test
    void searchCountersIncludePreprocessingAndReset() {
        for (StringSearch algorithm : searches) {
            algorithm.search("aaaa", "aaa");
            assertEquals(6, algorithm.operations());
            algorithm.search("aaaa", "aaa");
            assertEquals(6, algorithm.operations());
            algorithm.search("a", "aa");
            assertEquals(0, algorithm.operations());
        }
    }

    private static String randomString(Random random, int length) {
        StringBuilder result = new StringBuilder(length);
        for (int i = 0; i < length; i++) result.append((char) ('a' + random.nextInt(4)));
        return result.toString();
    }
}
