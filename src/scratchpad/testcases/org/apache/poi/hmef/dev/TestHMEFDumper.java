package org.apache.poi.hmef.dev;

import org.apache.poi.POIDataSamples;
import org.junit.Test;

import java.io.File;

public class TestHMEFDumper {
    @Test(expected = IllegalArgumentException.class)
    public void noArguments() throws Exception {
        HMEFDumper.main(new String[] {});
    }

    @Test
    public void main() throws Exception {
        File file = POIDataSamples.getHMEFInstance().getFile("quick-winmail.dat");
        HMEFDumper.main(new String[] {
                file.getAbsolutePath()
        });
    }

    @Test
    public void mainFull() throws Exception {
        File file = POIDataSamples.getHMEFInstance().getFile("quick-winmail.dat");
        HMEFDumper.main(new String[] {
                "--full",
                file.getAbsolutePath()
        });
    }
}