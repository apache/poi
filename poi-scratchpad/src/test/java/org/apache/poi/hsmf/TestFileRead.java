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
            assertEquals(151, mapiMessage.getMainChunks().getProperties().size());
        }
    }
}
