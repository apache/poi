package org.apache.poi.openxml4j.opc.internal;

import java.io.IOException;

/**
 * Thrown if the zip file is invalid.
 *
 * @since 5.3.1
 */
public class InvalidZipException extends IOException {
    public InvalidZipException(String message) {
        super(message);
    }
}
