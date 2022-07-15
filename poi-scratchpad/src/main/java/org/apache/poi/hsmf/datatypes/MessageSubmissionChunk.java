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
package org.apache.poi.hsmf.datatypes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hsmf.datatypes.Types.MAPIType;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LocaleUtil;

/**
 * A Chunk that holds the details given back by the server at submission time.
 * This includes the date the message was given to the server, and an ID that's
 * used if you want to cancel a message or similar
 */
public class MessageSubmissionChunk extends Chunk {
    private static final Logger LOG = LogManager.getLogger(MessageSubmissionChunk.class);
    private String rawId;
    private Calendar date;

    private static final Pattern datePatern = Pattern
        .compile("(\\d\\d)(\\d\\d)(\\d\\d)(\\d\\d)(\\d\\d)(\\d\\d)Z?");

    /**
     * Creates a Byte Chunk.
     */
    public MessageSubmissionChunk(String namePrefix, int chunkId,
            MAPIType type) {
        super(namePrefix, chunkId, type);
    }

    /**
     * Create a Byte Chunk, with the specified type.
     */
    public MessageSubmissionChunk(int chunkId, MAPIType type) {
        super(chunkId, type);
    }

    @Override
    public void readValue(InputStream value) throws IOException {
        // Stored in the file as us-ascii
        byte[] data = IOUtils.toByteArray(value);
        rawId = new String(data, StandardCharsets.US_ASCII);

        // Now process the date
        String[] parts = rawId.split(";");
        for (String part : parts) {
            if (part.startsWith("l=")) {
                // Format of this bit appears to be l=<id>-<time>-<number>
                // ID may contain hyphens.

                String dateS = null;
                final int numberPartBegin = part.lastIndexOf('-');
                if (numberPartBegin != -1) {
                    final int datePartBegin = part.lastIndexOf('-',
                            numberPartBegin - 1);
                    if (datePartBegin != -1 &&
                    // cannot extract date if only one hyphen is in the
                    // string...
                            numberPartBegin > datePartBegin) {
                        dateS = part.substring(datePartBegin + 1,
                                numberPartBegin);
                    }
                }
                if (dateS != null) {
                    // Should be yymmddhhmmssZ
                    Matcher m = datePatern.matcher(dateS);
                    if (m.matches()) {
                        date = LocaleUtil.getLocaleCalendar();

                        // work around issues with dates like 1989, which appear as "89" here
                        int year = Integer.parseInt(m.group(1));
                        date.set(Calendar.YEAR, year + (year > 80 ? 1900 : 2000));

                        // Java is 0 based
                        date.set(Calendar.MONTH, Integer.parseInt(m.group(2)) - 1);
                        date.set(Calendar.DATE, Integer.parseInt(m.group(3)));
                        date.set(Calendar.HOUR_OF_DAY,
                                Integer.parseInt(m.group(4)));
                        date.set(Calendar.MINUTE, Integer.parseInt(m.group(5)));
                        date.set(Calendar.SECOND, Integer.parseInt(m.group(6)));
                        date.clear(Calendar.MILLISECOND);
                    } else {
                        LOG.atWarn().log("Warning - unable to make sense of date {}", dateS);
                    }
                }
            }
        }
    }

    @Override
    public void writeValue(OutputStream out) throws IOException {
        final byte[] data = rawId.getBytes(StandardCharsets.US_ASCII);
        out.write(data);
    }

    /**
     * @return the date that the server accepted the message, as found from the
     *         message ID it generated.
     *
     */
    public Calendar getAcceptedAtTime() {
        return date;
    }

    /**
     * @return the full ID that the server generated when it accepted the
     *         message.
     */
    public String getSubmissionId() {
        return rawId;
    }
}
