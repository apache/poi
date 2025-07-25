/*
 * Copyright (C) 2007 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.poi.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * Copied from guava source code v15 (LimitedInputStream)
 * This version is modified to throw an IOException when the limit is reached.
 * Internal use only, do not use in new code.
 * @since POI 5.4.2
 */
@Internal
public final class LimitInputStream extends FilterInputStream {
    private final long limit;
    private long left;
    private long mark = -1;

    public LimitInputStream(final InputStream in, final long limit) {
        super(in);
        if (in == null) {
            throw new NullPointerException("InputStream must not be null");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("limit must be non-negative");
        }
        this.limit = limit;
        left = limit;
    }

    @SuppressForbidden
    @Override
    public int available() throws IOException {
        return (int) Math.min(in.available(), left);
    }

    // it's okay to mark even if mark isn't supported, as reset won't work
    @Override
    public synchronized void mark(int readLimit) {
        in.mark(readLimit);
        mark = left;
    }

    @Override
    public int read() throws IOException {
        if (left == 0) {
            throw new IOException(String.format(Locale.ROOT, "Limit of %d bytes reached", limit));
        }

        int result = in.read();
        if (result != -1) {
            --left;
        }
        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return 0;
        }
        if (left == 0) {
            throw new IOException(String.format(Locale.ROOT, "Limit of %d bytes reached", limit));
        }

        len = (int) Math.min(len, left);
        int result = in.read(b, off, len);
        if (result != -1) {
            left -= result;
        }
        return result;
    }

    @Override
    public synchronized void reset() throws IOException {
        if (!in.markSupported()) {
            throw new IOException("Mark not supported");
        }
        if (mark == -1) {
            throw new IOException("Mark not set");
        }

        in.reset();
        left = mark;
    }

    @Override
    public long skip(long n) throws IOException {
        n = Math.min(n, left);
        long skipped = in.skip(n);
        left -= skipped;
        return skipped;
    }
}
