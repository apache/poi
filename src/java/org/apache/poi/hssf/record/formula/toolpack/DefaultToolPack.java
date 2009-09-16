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

package org.apache.poi.hssf.record.formula.toolpack;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.record.formula.functions.FreeRefFunction;

/**
 * Default tool pack.
 * If you want to add some UDF, but you don't want to create new tool pack, use this.
 * 
 * @author PUdalau
 */
public class DefaultToolPack implements ToolPack {
    private Map<String, FreeRefFunction> functionsByName = new HashMap<String, FreeRefFunction>();

    public void addFunction(String name, FreeRefFunction evaluator) {
        if (evaluator != null){
            functionsByName.put(name, evaluator);
        }
    }

    public boolean containsFunction(String name) {
        return functionsByName.containsKey(name);
    }

    public FreeRefFunction findFunction(String name) {
        return functionsByName.get(name);
    }

    public void removeFunction(String name) {
        functionsByName.remove(name);
    }
}
