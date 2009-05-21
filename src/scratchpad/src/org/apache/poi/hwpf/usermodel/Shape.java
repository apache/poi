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

package org.apache.poi.hwpf.usermodel;

import org.apache.poi.hwpf.model.GenericPropertyNode;
import org.apache.poi.util.LittleEndian;

public final class Shape {
        int _id, _left, _right, _top, _bottom;
        /**
         * true if the Shape bounds are within document (for
         * example, it's false if the image left corner is outside the doc, like for
         * embedded documents)
         */
        boolean _inDoc;

        public Shape(GenericPropertyNode nodo) {
                byte [] contenuto = nodo.getBytes();
                _id = LittleEndian.getInt(contenuto);
                _left = LittleEndian.getInt(contenuto, 4);
                _top = LittleEndian.getInt(contenuto, 8);
                _right = LittleEndian.getInt(contenuto, 12);
                _bottom = LittleEndian.getInt(contenuto, 16);
                _inDoc = (_left >= 0 && _right >= 0 && _top >= 0 && _bottom >=
0);
        }

        public int getId() {
                return _id;
        }

        public int getLeft() {
                return _left;
        }

        public int getRight() {
                return _right;
        }

        public int getTop() {
                return _top;
        }

        public int getBottom() {
                return _bottom;
        }

        public int getWidth() {
                return _right - _left + 1;
        }

        public int getHeight() {
                return _bottom - _top + 1;
        }

        public boolean isWithinDocument() {
                return _inDoc;
        }
}
