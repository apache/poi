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

/**
 * The record aggregates are not real "records" but collections of records that act as a single record.
 * This is an optimization basically.
 *
 * @see org.apache.poi.hssf.record
 * @see org.apache.poi.hssf.eventmodel
 * @see org.apache.poi.hssf.record.RecordFactory
 */
package org.apache.poi.hssf.record.aggregates;