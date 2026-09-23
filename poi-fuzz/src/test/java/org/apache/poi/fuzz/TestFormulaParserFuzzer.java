package org.apache.poi.fuzz;

import org.junit.jupiter.api.Test;

import com.code_intelligence.jazzer.api.CannedFuzzedDataProvider;

class TestFormulaParserFuzzer {
    @Test
    void test() {
        String base64Bytes =
                "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAADdwQAAAADc3IAEWphdmEubGFuZy5JbnRlZ2VyEuKgpPeBhzgCAAFJAAV2YWx1ZXhyABBqYXZhLmxhbmcuTnVtYmVyhqyVHQuU4IsCAAB4cAAAAAJzcQB+AAIAAAAIdAAIWwxdIUXpiKl4";
        CannedFuzzedDataProvider input = new CannedFuzzedDataProvider(base64Bytes);
        FormulaParserFuzzer.fuzzerTestOneInput(input);
    }

    @Test
    void testNPE() {
        String base64Bytes =
                "rO0ABXNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAADdwQAAAADc3IAEWphdmEubGFuZy5JbnRlZ2VyEuKgpPeBhzgCAAFJAAV2YWx1ZXhyABBqYXZhLmxhbmcuTnVtYmVyhqyVHQuU4IsCAAB4cAAAAABzcQB+AAIAAAAHdAAJWwpdIUlROgoKeA==";
        CannedFuzzedDataProvider input = new CannedFuzzedDataProvider(base64Bytes);
        FormulaParserFuzzer.fuzzerTestOneInput(input);
    }
}