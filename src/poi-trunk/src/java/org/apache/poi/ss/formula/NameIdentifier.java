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

package org.apache.poi.ss.formula;

public class NameIdentifier {
    private final String _name;
    private final boolean _isQuoted;

    public NameIdentifier(String name, boolean isQuoted) {
        _name = name;
        _isQuoted = isQuoted;
    }
    public String getName() {
        return _name;
    }
    public boolean isQuoted() {
        return _isQuoted;
    }
    public String toString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append(getClass().getName());
        sb.append(" [");
        if (_isQuoted) {
            sb.append("'").append(_name).append("'");
        } else {
            sb.append(_name);
        }
        sb.append("]");
        return sb.toString();
    }
}