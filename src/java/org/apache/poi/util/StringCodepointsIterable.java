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

package org.apache.poi.util;

import java.util.Iterator;

// based on https://gist.github.com/EmmanuelOga/48df70b27ead4d80234b
@Internal
public class StringCodepointsIterable implements Iterable<String> {
    private class StringCodepointsIterator implements Iterator<String> {
        private int index = 0;

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean hasNext() {
            return index < StringCodepointsIterable.this.string.length();
        }

        @Override
        public String next() {
            int codePoint = StringCodepointsIterable.this.string.codePointAt(index);
            index += Character.charCount(codePoint);
            return new String(Character.toChars(codePoint));
        }
    }

    private final String string;

    public StringCodepointsIterable(final String string) {
        this.string = string;
    }

    @Override
    public Iterator<String> iterator() {
        return new StringCodepointsIterator();
    }
}