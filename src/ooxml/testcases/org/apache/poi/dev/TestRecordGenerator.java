package org.apache.poi.dev;

import org.apache.poi.util.TempFile;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

public class TestRecordGenerator {
    @Ignore("Could not get this to run, probably the dev-application does not work any more at all")
    @Test
    public void testNotEnoughArgs() throws Exception {
        RecordGenerator.main(new String[] {});
    }

    @Ignore("Could not get this to run, probably the dev-application does not work any more at all")
    @Test
    public void testMainRecords() throws Exception {
        File dir = TempFile.createTempDirectory("TestRecordGenerator");

        RecordGenerator.main(new String[] {
                "src/records/definitions/",
                "src/records/styles/",
                dir.getAbsolutePath(),
                dir.getAbsolutePath(),
        });
    }

    @Ignore("Could not get this to run, probably the dev-application does not work any more at all")
    @Test
    public void testMainTypes() throws Exception {
        File dir = TempFile.createTempDirectory("TestRecordGenerator");

        RecordGenerator.main(new String[] {
                "src/types/definitions/",
                "src/types/styles/",
                dir.getAbsolutePath(),
                dir.getAbsolutePath(),
        });
    }
}