package org.apache.poi.hdgf.dev;

import org.apache.poi.POIDataSamples;
import org.junit.Test;

import java.io.File;

public class TestVSDDumper {
    @Test
    public void main() throws Exception {
        File file = POIDataSamples.getDiagramInstance().getFile("Test_Visio-Some_Random_Text.vsd");
        VSDDumper.main(new String[] { file.getAbsolutePath() });
    }
}