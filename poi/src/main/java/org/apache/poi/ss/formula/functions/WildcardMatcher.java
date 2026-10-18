/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.ss.formula.functions;

import java.util.Arrays;

/**
 * Matches text against an Excel wildcard criteria ({@code ?}, {@code *} and the {@code ~} escape).
 * <p>
 * This gives the same result as matching against {@link Countif.StringMatcher#getWildCardPattern(String)},
 * but in O(text length * criteria length) time. The regular expression backtracks, so criteria with
 * several {@code *} could take exponential time.
 * <p>
 * Like that regular expression, it works on code points, folds only ASCII letters when comparing, and
 * {@code ?} and {@code *} do not match line terminators.
 */
final class WildcardMatcher {
    /** token for {@code ?}: exactly one character */
    private static final int ANY_CHAR = -1;
    /** token for {@code *}: zero or more characters */
    private static final int ANY_CHARS = -2;

    /** literal code points (ASCII folded to lower case), {@link #ANY_CHAR} or {@link #ANY_CHARS} */
    private final int[] _tokens;

    private WildcardMatcher(int[] tokens) {
        _tokens = tokens;
    }

    /**
     * Parses Excel wildcard criteria.
     * @return {@code null} if the specified value contains no special wildcard characters
     *  (the same cases where {@link Countif.StringMatcher#getWildCardPattern(String)} returns {@code null}).
     */
    static WildcardMatcher compile(String value) {
        int len = value.length();
        int[] tokens = new int[len];
        int count = 0;
        boolean hasWildCard = false;
        for (int i = 0; i < len; i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '?':
                    hasWildCard = true;
                    tokens[count++] = ANY_CHAR;
                    continue;
                case '*':
                    hasWildCard = true;
                    tokens[count++] = ANY_CHARS;
                    continue;
                case '~':
                    if (i + 1 < len) {
                        char next = value.charAt(i + 1);
                        if (next == '?' || next == '*' || next == '~') {
                            // '~' escapes the wildcards and itself
                            hasWildCard = true;
                            tokens[count++] = next;
                            i++;
                            continue;
                        }
                    }
                    // else just plain '~'
                    tokens[count++] = ch;
                    continue;
            }
            int cp = ch;
            if (Character.isHighSurrogate(ch) && i + 1 < len && Character.isLowSurrogate(value.charAt(i + 1))) {
                cp = Character.toCodePoint(ch, value.charAt(i + 1));
                i++;
            }
            tokens[count++] = toLowerAscii(cp);
        }
        if (!hasWildCard) {
            return null;
        }
        int[] trimmed = new int[count];
        System.arraycopy(tokens, 0, trimmed, 0, count);
        return new WildcardMatcher(trimmed);
    }

    /**
     * @return {@code true} if the whole of {@code text} matches the criteria
     */
    boolean matches(String text) {
        final int[] tokens = _tokens;
        final int n = tokens.length;
        // states[j] is set when the first j tokens match the text read so far
        boolean[] states = new boolean[n + 1];
        boolean[] nextStates = new boolean[n + 1];
        states[0] = true;
        skipAnyChars(states);
        int len = text.length();
        for (int i = 0; i < len; ) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);
            int lowerCp = toLowerAscii(cp);
            boolean lineTerminator = isLineTerminator(cp);
            boolean any = false;
            for (int j = 0; j < n; j++) {
                if (!states[j]) {
                    continue;
                }
                int token = tokens[j];
                if (token == ANY_CHARS) {
                    if (!lineTerminator) {
                        nextStates[j] = true;
                        any = true;
                    }
                } else if (token == ANY_CHAR ? !lineTerminator : token == lowerCp) {
                    nextStates[j + 1] = true;
                    any = true;
                }
            }
            if (!any) {
                return false;
            }
            skipAnyChars(nextStates);
            boolean[] tmp = states;
            states = nextStates;
            nextStates = tmp;
            Arrays.fill(nextStates, false);
        }
        return states[n];
    }

    /** a {@code *} can match nothing, so a state before one implies the state after it */
    private void skipAnyChars(boolean[] states) {
        for (int j = 0; j < _tokens.length; j++) {
            if (states[j] && _tokens[j] == ANY_CHARS) {
                states[j + 1] = true;
            }
        }
    }

    /** the characters that {@code .} in a regular expression does not match */
    private static boolean isLineTerminator(int cp) {
        return cp == '\n' || cp == '\r' || cp == '\u0085' || cp == '\u2028' || cp == '\u2029';
    }

    /** case folding as done by {@link java.util.regex.Pattern#CASE_INSENSITIVE} without UNICODE_CASE */
    private static int toLowerAscii(int cp) {
        return (cp >= 'A' && cp <= 'Z') ? cp + ('a' - 'A') : cp;
    }
}
