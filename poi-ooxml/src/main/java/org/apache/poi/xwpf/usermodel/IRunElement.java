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
package org.apache.poi.xwpf.usermodel;

import org.apache.poi.wp.usermodel.CharacterRun;

/**
 * Common interface for things that can occur
 * where a run (text with common stylings) can,
 * eg {@link XWPFRun} or {@link XWPFSDTRun}.
 * TODO More methods to follow shortly!
 * <p>
 * TODO Make this based on {@link CharacterRun}
 */
public interface IRunElement {}