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

import org.apache.poi.ss.formula.ThreeDEval;
import org.apache.poi.ss.formula.TwoDEval;
import org.apache.poi.ss.formula.eval.RefEval;
import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Common logic for COUNT, COUNTA and COUNTIF
 *
 * @author Josh Micich
 */
final class CountUtils {

	private CountUtils() {
		// no instances of this class
	}

	/**
	 * Common interface for the matching criteria.
	 */
	public interface I_MatchPredicate {
		boolean matches(ValueEval x);
	}
    public interface I_MatchAreaPredicate extends I_MatchPredicate {
        boolean matches(TwoDEval x, int rowIndex, int columnIndex);
    }

    /**
     * @return the number of evaluated cells in the range that match the specified criteria
     */
    public static int countMatchingCellsInArea(ThreeDEval areaEval, I_MatchPredicate criteriaPredicate) {
        int result = 0;

        final int firstSheetIndex = areaEval.getFirstSheetIndex();
        final int lastSheetIndex = areaEval.getLastSheetIndex();
        for (int sIx = firstSheetIndex; sIx <= lastSheetIndex; sIx++) {
            int height = areaEval.getHeight();
            int width = areaEval.getWidth();
            for (int rrIx=0; rrIx<height; rrIx++) {
                for (int rcIx=0; rcIx<width; rcIx++) {
                    ValueEval ve = areaEval.getValue(sIx, rrIx, rcIx);
    
                    if(criteriaPredicate instanceof I_MatchAreaPredicate){
                        I_MatchAreaPredicate areaPredicate = (I_MatchAreaPredicate)criteriaPredicate;
                        if(!areaPredicate.matches(areaEval, rrIx, rcIx)) continue;
                    }
    
                    if(criteriaPredicate.matches(ve)) {
                        result++;
                    }
                }
            }
        }
        return result;
    }
	/**
     * @return the number of evaluated cells in the range that match the specified criteria
	 */
	public static int countMatchingCellsInRef(RefEval refEval, I_MatchPredicate criteriaPredicate) {
	    int result = 0;
	    
        final int firstSheetIndex = refEval.getFirstSheetIndex();
        final int lastSheetIndex = refEval.getLastSheetIndex();
        for (int sIx = firstSheetIndex; sIx <= lastSheetIndex; sIx++) {
	        ValueEval ve = refEval.getInnerValueEval(sIx);
            if(criteriaPredicate.matches(ve)) {
                result++;
            }
	    }
		return result;
	}
	public static int countArg(ValueEval eval, I_MatchPredicate criteriaPredicate) {
		if (eval == null) {
			throw new IllegalArgumentException("eval must not be null");
		}
        if (eval instanceof ThreeDEval) {
            return countMatchingCellsInArea((ThreeDEval) eval, criteriaPredicate);
        }
		if (eval instanceof TwoDEval) {
		    throw new IllegalArgumentException("Count requires 3D Evals, 2D ones aren't supported");
		}
		if (eval instanceof RefEval) {
			return CountUtils.countMatchingCellsInRef((RefEval) eval, criteriaPredicate);
		}
		return criteriaPredicate.matches(eval) ? 1 : 0;
	}
}
