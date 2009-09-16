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

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.record.formula.atp.AnalysisToolPak;
import org.apache.poi.hssf.record.formula.functions.FreeRefFunction;

/**
 * Contains all tool packs. Processing of UDF is through this class.
 * 
 * @author PUdalau
 */
public class MainToolPacksHandler{

    private DefaultToolPack defaultToolPack;

    private List<ToolPack> usedToolPacks = new ArrayList<ToolPack>();

    private static MainToolPacksHandler instance;

    /**
     * @return Unique instance of handler.
     */
    public static MainToolPacksHandler instance() {
        if (instance == null) {
            instance = new MainToolPacksHandler();
        }
        return instance;
    }

    /**
     * @return Default tool pack(which is obligatory exists in handler).
     */
    public DefaultToolPack getDefaultToolPack() {
        return defaultToolPack;
    }

    private MainToolPacksHandler() {
        defaultToolPack = new DefaultToolPack();
        usedToolPacks.add(defaultToolPack);
        usedToolPacks.add(new AnalysisToolPak());
    }

    /**
	 * Checks if such function exists in any registered tool pack. 
	 * @param name Name of function.
	 * @return true if some tool pack contains such function.
     */
    public boolean containsFunction(String name) {
        for (ToolPack pack : usedToolPacks) {
            if (pack.containsFunction(name)) {
                return true;
            }
        }
        return false;
    }

    /**
	 * Returns executor by specified name. Returns <code>null</code> if
	 * function isn't contained by any registered tool pack.
	 * 
	 * @param name
	 *            Name of function.
	 * @return Function executor.
	 */
    public FreeRefFunction findFunction(String name) {
        FreeRefFunction evaluatorForFunction;
        for (ToolPack pack : usedToolPacks) {
            evaluatorForFunction = pack.findFunction(name);
            if (evaluatorForFunction != null) {
                return evaluatorForFunction;
            }
        }
        return null;
    }

    /**
     * Registers new tool pack in handler.
     * @param pack Tool pack to add.
     */
    public void addToolPack(ToolPack pack) {
        usedToolPacks.add(pack);
    }
}
