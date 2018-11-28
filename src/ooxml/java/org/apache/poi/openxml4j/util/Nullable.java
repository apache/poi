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

package org.apache.poi.openxml4j.util;

import org.apache.poi.util.Removal;

/**
 * An immutable object that could be defined as null.
 *
 * @author Julien Chable
 * @version 0.9
 * @deprecated No longer used in POI code base, use {@link java.util.Optional} instead
 */
@Removal(version = "4.2")
@Deprecated
public final class Nullable<E> {

	private E value;

	/**
	 * Constructor.
	 */
	public Nullable() {
		// Do nothing
	}

	/**
	 * Constructor.
	 *
	 * @param value
	 *            The value to set to this nullable.
	 */
	public Nullable(E value) {
		this.value = value;
	}

	/**
	 * Get the store value if any.
	 *
	 * @return the store value
	 */
	public E getValue() {
		return value;
	}

	/**
	 * Get the status of this nullable.
	 *
	 * @return <b>true</b> if the nullable store a value (empty string is
	 *         considered to be a value) else <b>false</>.
	 */
	public boolean hasValue() {
		return value != null;
	}

	/**
	 * Set the stored value to <i>null</i>.
	 */
	public void nullify() {
		value = null;
	}
}
