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

package org.apache.poi.crypt.examples;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

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
public class OOXMLPasswordsTry implements Closeable {
    private POIFSFileSystem fs;
    private EncryptionInfo info;
    private Decryptor d;
    
    private OOXMLPasswordsTry(POIFSFileSystem fs) throws IOException {
        info = new EncryptionInfo(fs);
        d = Decryptor.getInstance(info);
        this.fs = fs;
    }
    private OOXMLPasswordsTry(File file) throws IOException {
        this(new POIFSFileSystem(file, true));
    }
    private OOXMLPasswordsTry(InputStream is) throws IOException {
        this(new POIFSFileSystem(is));
    }
    
    public void close() throws IOException {
        fs.close();
    }
    
    public String tryAll(File wordfile) throws IOException, GeneralSecurityException {
        String valid = null;
        // Load
        try (BufferedReader r = new BufferedReader(new FileReader(wordfile))) {
            long start = System.currentTimeMillis();
            int count = 0;

            // Try each password in turn, reporting progress
            String password;
            while ((password = r.readLine()) != null) {
                if (isValid(password)) {
                    valid = password;
                    break;
                }
                count++;

                if (count % 1000 == 0) {
                    int secs = (int) ((System.currentTimeMillis() - start) / 1000);
                    System.out.println("Done " + count + " passwords, " +
                                               secs + " seconds, last password " + password);
                }
            }

        }
        // Tidy and return (null if no match)
        return valid;
    }
    public boolean isValid(String password) throws GeneralSecurityException {
        return d.verifyPassword(password);
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Use:");
            System.err.println("  OOXMLPasswordsTry <file.ooxml> <wordlist>");
            System.exit(1);
        }
        File ooxml = new File(args[0]);
        File words = new File(args[1]);
        
        System.out.println("Trying passwords from " + words + " against " + ooxml);
        System.out.println();

        String password;
        try (OOXMLPasswordsTry pt = new OOXMLPasswordsTry(ooxml)) {
            password = pt.tryAll(words);
        }
        
        System.out.println();
        if (password == null) {
            System.out.println("Error - No password matched");
        } else {
            System.out.println("Password found!");
            System.out.println(password);
        }
    }
}
