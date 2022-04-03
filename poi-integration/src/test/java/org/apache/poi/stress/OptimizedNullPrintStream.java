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
package org.apache.poi.stress;

import java.io.IOException;

import org.apache.commons.io.output.NullPrintStream;

/**
 * A slightly improved version of {@link org.apache.commons.io.output.NullPrintStream}
 * which overrides some more methods to avoid doing string-conversion or
 * utf-8 decoding when the actual printing is not done anyway
 */
@SuppressWarnings("NullableProblems")
class OptimizedNullPrintStream extends NullPrintStream {

    @Override
    public void println(String x) {
        // empty as NullPrintStream still performs UTF-8 conversion
        // and other stuff which takes considerable time!
    }

    @Override
    public void print(String s) {
        // empty as NullPrintStream still performs UTF-8 conversion
        // and other stuff which takes considerable time!
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        // empty as NullPrintStream still performs UTF-8 conversion
        // and other stuff which takes considerable time!
    }

    @Override
    public void write(byte[] b) throws IOException {
        // empty as NullPrintStream still performs UTF-8 conversion
        // and other stuff which takes considerable time!
    }

    @Override
    public void write(int b) {
        // empty as NullPrintStream still performs UTF-8 conversion
        // and other stuff which takes considerable time!
    }
}
