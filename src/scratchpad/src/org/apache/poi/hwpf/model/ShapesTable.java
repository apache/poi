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

package org.apache.poi.hwpf.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hwpf.usermodel.Shape;

public final class ShapesTable {
        private List _shapes;
        private List _shapesVisibili;  //holds visible shapes

        public ShapesTable(byte [] tblStream, FileInformationBlock fib) {
                PlexOfCps binTable = new PlexOfCps(tblStream,
                     fib.getFcPlcspaMom(), fib.getLcbPlcspaMom(), 26);

                _shapes = new ArrayList();
                _shapesVisibili = new ArrayList();


                for(int i = 0; i < binTable.length(); i++) {
                        GenericPropertyNode nodo = binTable.getProperty(i);

                        Shape sh = new Shape(nodo);
                        _shapes.add(sh);
                        if(sh.isWithinDocument())
                                _shapesVisibili.add(sh);
                }
        }

        public List getAllShapes() {
                return _shapes;
        }

        public List getVisibleShapes() {
                return _shapesVisibili;
        }
}
