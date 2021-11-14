package org.apache.poi.util;

import java.security.SecureRandom;

/*
If it is important that the generated Random numbers not be guessable,
you MUST NOT create a new Random for each random number; the values are too easily guessable.

You should strongly consider using a java.security.SecureRandom instead
(and avoid allocating a new SecureRandom for each random number needed).
*/

public class RandomSingleton {
    private static final SecureRandom INSTANCE = new SecureRandom();

    public static SecureRandom getInstance() {
        return INSTANCE;
    }
}
