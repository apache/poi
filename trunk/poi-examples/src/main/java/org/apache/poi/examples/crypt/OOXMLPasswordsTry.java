/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.examples.crypt;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Tries a list of possible passwords for an OOXML protected file
 *
 * Note that this isn't very fast, and is aimed at when you have
 *  just a few passwords to check.
 * For serious processing, you'd be best off grabbing the hash
 *  out with POI or office2john.py, then running that against
 *  "John The Ripper" or GPU enabled version of "hashcat"
 */
public final class OOXMLPasswordsTry {

    private OOXMLPasswordsTry() {}

    @SuppressWarnings({"java:S106","java:S4823"})
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Use:");
            System.err.println("  OOXMLPasswordsTry <file.ooxml> <wordlist>");
            System.exit(1);
        }
        String ooxml = args[0];
        String words = args[1];

        System.out.println("Trying passwords from " + words + " against " + ooxml);
        System.out.println();

        try (POIFSFileSystem fs = new POIFSFileSystem(new File(ooxml), true)) {
            EncryptionInfo info = new EncryptionInfo(fs);
            Decryptor d = Decryptor.getInstance(info);

            final long start = System.currentTimeMillis();
            final int[] count = { 0 };
            Predicate<String> counter = s -> {
                if (++count[0] % 1000 == 0) {
                    int secs = (int) ((System.currentTimeMillis() - start) / 1000);
                    System.out.println("Done " + count[0] + " passwords, " + secs + " seconds, last password " + s);
                }
                return true;
            };

            // Try each password in turn, reporting progress
            try (Stream<String> lines = Files.lines(Paths.get(words))) {
                Optional<String> found = lines.filter(counter).filter(w -> isValid(d, w)).findFirst();
                System.out.println(found.map(s -> "Password found: " + s).orElse("Error - No password matched"));
            }
        }
    }

    private static boolean isValid(Decryptor dec, String password) {
        try {
            return dec.verifyPassword(password);
        } catch (GeneralSecurityException e) {
            return false;
        }
    }
}
