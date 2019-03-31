/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.poifs.filesystem;

import static org.apache.poi.poifs.common.POIFSConstants.OOXML_FILE_HEADER;
import static org.apache.poi.poifs.common.POIFSConstants.RAW_XML_FILE_HEADER;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.poifs.storage.HeaderBlockConstants;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LocaleUtil;

/**
 * The file magic number, i.e. the file identification based on the first bytes
 * of the file
 */
public enum FileMagic {
    /** OLE2 / BIFF8+ stream used for Office 97 and higher documents */
    OLE2(HeaderBlockConstants._signature),
    /** OOXML / ZIP stream */
    OOXML(OOXML_FILE_HEADER),
    /** XML file */
    XML(RAW_XML_FILE_HEADER),
    /** BIFF2 raw stream - for Excel 2 */
    BIFF2(new byte[]{
        0x09, 0x00, // sid=0x0009
        0x04, 0x00, // size=0x0004
        0x00, 0x00, // unused
        0x70, 0x00  // 0x70 = multiple values
    }),
    /** BIFF3 raw stream - for Excel 3 */
    BIFF3(new byte[]{
        0x09, 0x02, // sid=0x0209
        0x06, 0x00, // size=0x0006
        0x00, 0x00, // unused
        0x70, 0x00  // 0x70 = multiple values
    }),
    /** BIFF4 raw stream - for Excel 4 */
    BIFF4(new byte[]{
        0x09, 0x04, // sid=0x0409
        0x06, 0x00, // size=0x0006
        0x00, 0x00, // unused
        0x70, 0x00  // 0x70 = multiple values
    },new byte[]{
        0x09, 0x04, // sid=0x0409
        0x06, 0x00, // size=0x0006
        0x00, 0x00, // unused
        0x00, 0x01
    }),
    /** Old MS Write raw stream */
    MSWRITE(
        new byte[]{0x31, (byte)0xbe, 0x00, 0x00 },
        new byte[]{0x32, (byte)0xbe, 0x00, 0x00 }),
    /** RTF document */
    RTF("{\\rtf"),
    /** PDF document */
    PDF("%PDF"),
    /** Some different HTML documents */
    HTML("<!DOCTYP".getBytes(UTF_8),
            "<html".getBytes(UTF_8),
            "\n\r<html".getBytes(UTF_8),
            "\r\n<html".getBytes(UTF_8),
            "\r<html".getBytes(UTF_8),
            "\n<html".getBytes(UTF_8),
            "<HTML".getBytes(UTF_8),
            "\r\n<HTML".getBytes(UTF_8),
            "\n\r<HTML".getBytes(UTF_8),
            "\r<HTML".getBytes(UTF_8),
            "\n<HTML".getBytes(UTF_8)),
    WORD2(new byte[]{ (byte)0xdb, (byte)0xa5, 0x2d, 0x00}),
    // keep UNKNOWN always as last enum!
    /** UNKNOWN magic */
    UNKNOWN(new byte[0]);

    final byte[][] magic;
    
    FileMagic(long magic) {
        this.magic = new byte[1][8];
        LittleEndian.putLong(this.magic[0], 0, magic);
    }
    
    FileMagic(byte[]... magic) {
        this.magic = magic;
    }
    
    FileMagic(String magic) {
        this(magic.getBytes(LocaleUtil.CHARSET_1252));
    }

    public static FileMagic valueOf(byte[] magic) {
        for (FileMagic fm : values()) {
            for (byte[] ma : fm.magic) {
                if (findMagic(ma, magic)) {
                    return fm;
                }
            }
        }
        return UNKNOWN;
    }

    private static boolean findMagic(byte[] expected, byte[] actual) {
        int i=0;
        for (byte expectedByte : expected) {
            byte actualByte = actual[i++];
            if ((actualByte != expectedByte &&
                    (expectedByte != 0x70 || (actualByte != 0x10 && actualByte != 0x20 && actualByte != 0x40)))) {
                return false;
            }
        }
        return true;
    }


    /**
     * Get the file magic of the supplied {@link File}<p>
     *
     * Even if this method returns {@link FileMagic#UNKNOWN} it could potentially mean,
     *  that the ZIP stream has leading junk bytes
     *
     * @param inp a file to be identified
     */
    public static FileMagic valueOf(final File inp) throws IOException {
        try (FileInputStream fis = new FileInputStream(inp)) {
            final byte[] data = IOUtils.toByteArray(fis, 8);
            return FileMagic.valueOf(data);
        }
    }


    /**
     * Get the file magic of the supplied InputStream (which MUST
     *  support mark and reset).<p>
     *
     * If unsure if your InputStream does support mark / reset,
     *  use {@link #prepareToCheckMagic(InputStream)} to wrap it and make
     *  sure to always use that, and not the original!<p>
     *
     * Even if this method returns {@link FileMagic#UNKNOWN} it could potentially mean,
     *  that the ZIP stream has leading junk bytes
     *
     * @param inp An InputStream which supports either mark/reset
     */
    public static FileMagic valueOf(InputStream inp) throws IOException {
        if (!inp.markSupported()) {
            throw new IOException("getFileMagic() only operates on streams which support mark(int)");
        }

        // Grab the first 8 bytes
        byte[] data = IOUtils.peekFirst8Bytes(inp);

        return FileMagic.valueOf(data);
    }


    /**
     * Checks if an {@link InputStream} can be reset (i.e. used for checking the header magic) and wraps it if not
     *
     * @param stream stream to be checked for wrapping
     * @return a mark enabled stream
     */
    public static InputStream prepareToCheckMagic(InputStream stream) {
        if (stream.markSupported()) {
            return stream;
        }
        // we used to process the data via a PushbackInputStream, but user code could provide a too small one
        // so we use a BufferedInputStream instead now
        return new BufferedInputStream(stream);
    }
}