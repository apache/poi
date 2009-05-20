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

package org.apache.poi.hssf.record.formula.functions;

import java.util.Arrays;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 * Library for common statistics functions
 */
final class StatsLib {

    private StatsLib() {
        // no instances of this class
    }


    /**
     * returns the mean of deviations from mean.
     * @param v
     */
    public static double avedev(double[] v) {
        double r = 0;
        double m = 0;
        double s = 0;
        for (int i=0, iSize=v.length; i<iSize; i++) {
            s += v[i];
        }
        m = s / v.length;
        s = 0;
        for (int i=0, iSize=v.length; i<iSize; i++) {
            s += Math.abs(v[i]-m);
        }
        r = s / v.length;
        return r;
    }

    public static double stdev(double[] v) {
        double r = Double.NaN;
        if (v!=null && v.length > 1) {
            r = Math.sqrt( devsq(v) / (v.length - 1) );
        }
        return r;
    }


    public static double median(double[] v) {
        double r = Double.NaN;

        if (v!=null && v.length >= 1) {
            int n = v.length;
            Arrays.sort(v);
            r = (n % 2 == 0)
                ? (v[n / 2] + v[n / 2 - 1]) / 2
                : v[n / 2];
        }

        return r;
    }


    public static double devsq(double[] v) {
        double r = Double.NaN;
        if (v!=null && v.length >= 1) {
            double m = 0;
            double s = 0;
            int n = v.length;
            for (int i=0; i<n; i++) {
                s += v[i];
            }
            m = s / n;
            s = 0;
            for (int i=0; i<n; i++) {
                s += (v[i]- m) * (v[i] - m);
            }

            r = (n == 1)
                    ? 0
                    : s;
        }
        return r;
    }

    /**
     * returns the kth largest element in the array. Duplicates
     * are considered as distinct values. Hence, eg.
     * for array {1,2,4,3,3} & k=2, returned value is 3.
     * <br/>
     * k <= 0 & k >= v.length and null or empty arrays
     * will result in return value Double.NaN
     */
    public static double kthLargest(double[] v, int k) {
        double r = Double.NaN;
        int index = k-1; // since arrays are 0-based
        if (v!=null && v.length > index && index >= 0) {
            Arrays.sort(v);
            r = v[v.length-index-1];
        }
        return r;
    }

    /**
     * returns the kth smallest element in the array. Duplicates
     * are considered as distinct values. Hence, eg.
     * for array {1,1,2,4,3,3} & k=2, returned value is 1.
     * <br/>
     * k <= 0 & k >= v.length or null array or empty array
     * will result in return value Double.NaN
     * @param v
     * @param k
     */
    public static double kthSmallest(double[] v, int k) {
        double r = Double.NaN;
        int index = k-1; // since arrays are 0-based
        if (v!=null && v.length > index && index >= 0) {
            Arrays.sort(v);
            r = v[index];
        }
        return r;
    }
}
