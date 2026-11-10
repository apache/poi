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
package org.apache.poi.ss.formula.functions;

/**
 * Interface for those functions that evaluate arguments in array mode depending on context.
 * <p>
 * When an operator (e.g. {@code =} or {@code *}) has an area operand and its result is an argument
 * of such a function, the operator is evaluated element-wise over the area instead of being reduced
 * to the single value for the formula's cell. Both {@link Function}s and {@link FreeRefFunction}s
 * (user-defined and "future" functions such as XLOOKUP) may implement this marker.
 */
public interface ArrayMode {

}
