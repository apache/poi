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

/**
 * A custom implementation of {@link java.util.HashSet} in order to reduce memory consumption.
 *
 * Profiling tests (Oct 2008) have shown that each element {@link FormulaCellCacheEntry} takes
 * around 32 bytes to store in a HashSet, but around 6 bytes to store here.  For Spreadsheets with
 * thousands of formula cells with multiple interdependencies, the savings can be very significant.
 *
 * @author Josh Micich
 */
final class FormulaCellCacheEntrySet {
	private static final FormulaCellCacheEntry[] EMPTY_ARRAY = { };

	private int _size;
	private FormulaCellCacheEntry[] _arr;

	public FormulaCellCacheEntrySet() {
		_arr = EMPTY_ARRAY;
	}

	public FormulaCellCacheEntry[] toArray() {
		int nItems = _size;
		if (nItems < 1) {
			return EMPTY_ARRAY;
		}
		FormulaCellCacheEntry[] result = new FormulaCellCacheEntry[nItems];
		int j=0;
		for(int i=0; i<_arr.length; i++) {
			FormulaCellCacheEntry cce = _arr[i];
			if (cce != null) {
				result[j++] = cce;
			}
		}
		if (j!= nItems) {
			throw new IllegalStateException("size mismatch");
		}
		return result;
	}


	public void add(CellCacheEntry cce) {
		if (_size * 3 >= _arr.length * 2) {
			// re-hash
			FormulaCellCacheEntry[] prevArr = _arr;
			FormulaCellCacheEntry[] newArr = new FormulaCellCacheEntry[4 + _arr.length * 3 / 2]; // grow 50%
			for(int i=0; i<prevArr.length; i++) {
				FormulaCellCacheEntry prevCce = _arr[i];
				if (prevCce != null) {
					addInternal(newArr, prevCce);
				}
			}
			_arr = newArr;
		}
		if (addInternal(_arr, cce)) {
			_size++;
		}
	}


	private static boolean addInternal(CellCacheEntry[] arr, CellCacheEntry cce) {

		int startIx = cce.hashCode() % arr.length;

		for(int i=startIx; i<arr.length; i++) {
			CellCacheEntry item = arr[i];
			if (item == cce) {
				// already present
				return false;
			}
			if (item == null) {
				arr[i] = cce;
				return true;
			}
		}
		for(int i=0; i<startIx; i++) {
			CellCacheEntry item = arr[i];
			if (item == cce) {
				// already present
				return false;
			}
			if (item == null) {
				arr[i] = cce;
				return true;
			}
		}
		throw new IllegalStateException("No empty space found");
	}

	public boolean remove(CellCacheEntry cce) {
		FormulaCellCacheEntry[] arr = _arr;

		if (_size * 3 < _arr.length && _arr.length > 8) {
			// re-hash
			boolean found = false;
			FormulaCellCacheEntry[] prevArr = _arr;
			FormulaCellCacheEntry[] newArr = new FormulaCellCacheEntry[_arr.length / 2]; // shrink 50%
			for(int i=0; i<prevArr.length; i++) {
				FormulaCellCacheEntry prevCce = _arr[i];
				if (prevCce != null) {
					if (prevCce == cce) {
						found=true;
						_size--;
						// skip it
						continue;
					}
					addInternal(newArr, prevCce);
				}
			}
			_arr = newArr;
			return found;
		}
		// else - usual case
		// delete single element (without re-hashing)

		int startIx = cce.hashCode() % arr.length;

		// note - can't exit loops upon finding null because of potential previous deletes
		for(int i=startIx; i<arr.length; i++) {
			FormulaCellCacheEntry item = arr[i];
			if (item == cce) {
				// found it
				arr[i] = null;
				_size--;
				return true;
			}
		}
		for(int i=0; i<startIx; i++) {
			FormulaCellCacheEntry item = arr[i];
			if (item == cce) {
				// found it
				arr[i] = null;
				_size--;
				return true;
			}
		}
		return false;
	}
}
