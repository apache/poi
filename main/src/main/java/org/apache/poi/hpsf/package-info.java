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
 * Processes streams in the Horrible Property Set Format (HPSF) in POI filesystems.
 * Microsoft Office documents, i.e. POI filesystems, usually contain meta data like author, title,
 * last saving time etc. These items are called <strong>properties</strong> and stored in
 * <strong>property set streams</strong> along with the document itself.
 * These streams are commonly named {@code \005SummaryInformation} and {@code \005DocumentSummaryInformation}.
 * However, a POI filesystem may contain further property sets of other names or types.
 * <p>
 * In order to extract the properties from a POI filesystem, a property set stream's contents must be parsed into a
 * {@link org.apache.poi.hpsf.PropertySet} instance.  Its subclasses {@link org.apache.poi.hpsf.SummaryInformation}
 * and {@link org.apache.poi.hpsf.DocumentSummaryInformation} deal with the well-known property set streams
 * {@code \005SummaryInformation} and {@code \005DocumentSummaryInformation}.
 * (However, the streams' names are irrelevant. What counts is the property set's first section's format ID - see below.)
 * <p>
 * The factory method {@link org.apache.poi.hpsf.PropertySetFactory#create} creates a PropertySet instance.
 * This method always returns the <strong>most specific property set</strong>:
 * If it identifies the stream data as a Summary Information or as a Document Summary Information it returns an
 * instance of the corresponding class, else the general PropertySet.
 * <p>
 * A PropertySet contains a list of {@link org.apache.poi.hpsf.Section Sections} which can be
 * retrieved with {@link org.apache.poi.hpsf.PropertySet#getSections}. Each Section contains a
 * {@link org.apache.poi.hpsf.Property} array which can be retrieved with
 * {@link org.apache.poi.hpsf.Section#getProperties}. Since the vast majority of PropertySets contains only a single Section,
 * the convenience method {@link org.apache.poi.hpsf.PropertySet#getProperties} returns the properties of a
 * PropertySets Section (throwing a {@link org.apache.poi.hpsf.NoSingleSectionException} if the PropertySet contains
 * more (or less) than exactly one Section).
 * <p>
 * Each Property has an <strong>ID</strong>, a <strong>type</strong>, and a <strong>value</strong> which can be
 * retrieved with {@link org.apache.poi.hpsf.Property#getID}, {@link org.apache.poi.hpsf.Property#getType},
 * and {@link org.apache.poi.hpsf.Property#getValue}, respectively. The value's class depends on the property's type.
 * <!-- FIXME: --> The current implementation does not yet support all property types and restricts the values' classes
 * to String, Integer and Date. A value of a yet unknown type is returned as a byte array containing the values
 * origin bytes from the property set stream.
 * <p>
 * To retrieve the value of a specific Property, use {@link org.apache.poi.hpsf.Section#getProperty} or
 * {@link org.apache.poi.hpsf.Section#getPropertyIntValue}.
 * <p>
 * The SummaryInformation and DocumentSummaryInformation classes provide convenience methods for retrieving well-known
 * properties. For example, an application that wants to retrieve a document's title string just calls
 * {@link org.apache.poi.hpsf.SummaryInformation#getTitle} instead of going through the hassle of first finding out
 * what the title's property ID is and then using this ID to get the property's value.
 *
 * @see <a href="https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-oleps/bf7aeae8-c47a-4939-9f45-700158dac3bc">[MS-OLEPS] Object Linking and Embedding (OLE) Property Set Data Structures</a>
 */
package org.apache.poi.hpsf;