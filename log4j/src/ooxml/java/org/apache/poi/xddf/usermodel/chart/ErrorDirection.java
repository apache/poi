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

package org.apache.poi.xddf.usermodel.chart;

import java.util.HashMap;

import org.openxmlformats.schemas.drawingml.x2006.chart.STErrDir;

public enum ErrorDirection {
    X(STErrDir.X),
    Y(STErrDir.Y);

    final STErrDir.Enum underlying;

    ErrorDirection(STErrDir.Enum direction) {
        this.underlying = direction;
    }

    private static final HashMap<STErrDir.Enum, ErrorDirection> reverse = new HashMap<>();
    static {
        for (ErrorDirection value : values()) {
            reverse.put(value.underlying, value);
        }
    }

    static ErrorDirection valueOf(STErrDir.Enum direction) {
        return reverse.get(direction);
    }
}
