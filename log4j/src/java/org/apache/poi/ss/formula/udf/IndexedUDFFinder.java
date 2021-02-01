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
package org.apache.poi.ss.formula.udf;

import org.apache.poi.ss.formula.functions.FreeRefFunction;
import org.apache.poi.util.Internal;

import java.util.HashMap;

/**
 * A UDFFinder that can retrieve functions both by name and by fake index.
 *
 * @author Yegor Kozlov
 */
@Internal
public class IndexedUDFFinder extends AggregatingUDFFinder {
    private final HashMap<Integer, String> _funcMap;

    public IndexedUDFFinder(UDFFinder... usedToolPacks) {
        super(usedToolPacks);
        _funcMap = new HashMap<>();
    }

    @Override
    public FreeRefFunction findFunction(String name) {
        FreeRefFunction func = super.findFunction(name);
        if (func != null) {
            int idx = getFunctionIndex(name);
            _funcMap.put(idx, name);
        }
        return func;
    }

    public String getFunctionName(int idx) {
        return _funcMap.get(idx);
    }

    public int getFunctionIndex(String name) {
        return name.hashCode();
    }
}
