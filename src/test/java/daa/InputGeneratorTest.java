package daa;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InputGeneratorTest {
    @Test
    void inputsAreRepeatableRegardlessOfCallOrder() {
        String[] first = InputGenerator.edit(128, false);
        InputGenerator.search(4096, false);
        assertArrayEquals(first, InputGenerator.edit(128, false));
        assertArrayEquals(InputGenerator.search(2048, false), InputGenerator.search(2048, false));
    }

    @Test
    void plantedMatchesAndWorstCasesHaveIntendedStructure() {
        String[] random = InputGenerator.search(2048, false);
        for (int start : new int[]{512, 1024, 1536}) {
            assertTrue(random[0].startsWith(random[1], start));
        }
        String[] worst = InputGenerator.search(2048, true);
        assertEquals("a".repeat(2048), worst[0]);
        assertEquals("a".repeat(31) + "b", worst[1]);
        assertTrue(new KmpSearch().search(worst[0], worst[1]).isEmpty());
        assertArrayEquals(new String[]{"aaaa", "bbbb"}, InputGenerator.edit(4, true));
    }
}
