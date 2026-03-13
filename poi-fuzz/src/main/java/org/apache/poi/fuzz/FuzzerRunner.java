package org.apache.poi.fuzz;

import org.apache.poi.fuzz.POIFileHandlerFuzzer;
import java.io.File;
import java.nio.file.Files;

public class FuzzerRunner {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: FuzzerRunner <file-to-fuzz>");
            System.exit(1);
        }
        File f = new File(args[0]);
        if (!f.exists()) {
            System.err.println("File not found: " + args[0]);
            System.exit(1);
        }
        byte[] input = Files.readAllBytes(f.toPath());
        System.out.println("Running fuzzer for file: " + args[0] + " (" + input.length + " bytes)");
        POIFileHandlerFuzzer.fuzzerInitialize();
        POIFileHandlerFuzzer.fuzzerTestOneInput(input);
        System.out.println("Success!");
    }
}
