package org.apache.poi.hsmf;

import org.apache.poi.POIDataSamples;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestFileRead {
    @Test
    void bug66335() throws IOException {
        try (MAPIMessage mapiMessage = new MAPIMessage(
                POIDataSamples.getHSMFInstance().getFile("bug66335.msg"))) {
            // 25 is not the right number of properties but it is what the existing code finds
            assertEquals(25, mapiMessage.getMainChunks().getProperties().size());
        }
    }
}
