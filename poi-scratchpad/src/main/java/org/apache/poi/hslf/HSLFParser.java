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
package org.apache.poi.hslf;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.util.ExceptionUtil;

/**
 * Methods that wrap {@link HSLFSlideShow} parsing functionality.
 * One difference is that the methods in this class try to
 * throw {@link HSLFReadException} or {@link IOException} instead of {@link RuntimeException}.
 * You can still get an {@link Error}s like an {@link OutOfMemoryError}.
 *
 * @since POI 5.5.0
 */
public final class HSLFParser {

    private HSLFParser() {
        // Prevent instantiation
    }

    /**
     * Parse the given InputStream and return a new {@link HSLFSlideShow} instance.
     *
     * @param stream the data to parse (will be closed after parsing)
     * @return a new {@link HSLFSlideShow} instance
     * @throws HSLFReadException if an error occurs while reading the file
     * @throws IOException if an I/O error occurs while reading the file
     */
    public static HSLFSlideShow parse(InputStream stream) throws HSLFReadException, IOException {
        try {
            return new HSLFSlideShow(stream);
        } catch (IOException e) {
            throw e;
        } catch (Error | RuntimeException e) {
            if (ExceptionUtil.isFatal(e)) {
                throw e;
            }
            throw new HSLFReadException("Exception reading HSLFSlideShow", e);
        } catch (Exception e) {
            throw new HSLFReadException("Exception reading HSLFSlideShow", e);
        }
    }
}
