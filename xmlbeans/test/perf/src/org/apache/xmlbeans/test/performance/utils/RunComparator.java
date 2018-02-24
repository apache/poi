/*   Copyright 2004 The Apache Software Foundation
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*  limitations under the License.
*/
package org.apache.xmlbeans.test.performance.utils;

import org.apache.xmlbeans.XmlError;
import org.apache.xmlbeans.XmlOptions;
import org.openuri.perf.Custom;
import org.openuri.perf.Result;
import org.openuri.perf.ResultSetDocument;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class RunComparator
{
    // This class compares the xml results file of 2 perf runs and checks differences in the numbers for each test case
    // An optional 'tolerance' % deviation value can be provided which will result in only the cases that are beyond this
    // value (+/-) getting reported

    private ResultSetDocument resultsDocOne;    // XmlObject for the first result set
    private ResultSetDocument resultsDocTwo;    // and the second
    private double tolerance;                   // % deviation in test run numbers

    // The computation of deviation for a given test scase is done as follows:
    // (run1 - run2)/run1 as a %. Negative value implies regression, +ve implies improvement


    public RunComparator(String runResultsXmlFilePath1, String runResultsXmlFilePath2, double tolerance) throws Exception
    {
        System.out.println("XMLBeans RunComparator - comparing Perf Runs...");

        if ((runResultsXmlFilePath1 != null) && (runResultsXmlFilePath2 != null))
        {
            try
            {
                File resultsFileOne = new File(runResultsXmlFilePath1);
                File resultsFileTwo = new File(runResultsXmlFilePath2);

                if (tolerance > 100 || tolerance < 0)
                {
                    throw(new Exception("Invalid input value for 'tolerance'"));
                }
                this.tolerance = tolerance;

                // generate XmlObjects and validate against schema
                XmlOptions validateOptions = new XmlOptions();
                List errors = new ArrayList();
                validateOptions.setErrorListener(errors);

                resultsDocOne = ResultSetDocument.Factory.parse(resultsFileOne);
                if (!resultsDocOne.validate(validateOptions))
                {
                    System.out.println("Result Set XML Document " + runResultsXmlFilePath1 + "is not Valid against schema!");
                    for (Iterator iterator = errors.iterator(); iterator.hasNext();)
                    {
                        XmlError eachErr = (XmlError) iterator.next();
                        //System.out.println("Validation Error:" + eachErr.getMessage() + eachErr.getLine());
                    }
                    //throw new Exception("Invalid Results File " + runResultsXmlFilePath1);
                }

                errors.clear();
                resultsDocTwo = ResultSetDocument.Factory.parse(resultsFileTwo);
                if (!resultsDocTwo.validate(validateOptions))
                {
                    System.out.println("Result Set XML Document " + runResultsXmlFilePath2 + "is not Valid against schema!");
                    for (Iterator iterator = errors.iterator(); iterator.hasNext();)
                    {
                        XmlError eachErr = (XmlError) iterator.next();
                        //System.out.println("Validation Error:" + eachErr.getMessage() + eachErr.getLine());
                    }

                    //throw new Exception("Invalid Results File " + runResultsXmlFilePath2);
                }

            }

            catch (FileNotFoundException fne)
            {
                throw new Exception("Invalid results file(s) specified!");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            throw new Exception("Input File Path(s) in null!");
        }

    } // end constructor


    private void compare()
    {
        // create a hashmap with first run results
        Result[] res1 = resultsDocOne.getResultSet().getResultArray();

        HashMap runOneHashMap = new HashMap();
        for (int i = 0; i < res1.length; i++)
        {
            Result eachResult = res1[i];
            //System.out.println("Name:" + eachResult.getName() + "\tnote:" + eachResult.getNote() + "\tTime:" + eachResult.getTime());

            Custom[] custArr = eachResult.getCustomArray();
            if ((custArr != null) || (custArr.length > 0))
            {
                for (int j=0; j < custArr.length; j++)
                {
                    Custom eachCustomElem = custArr[j];
                    //System.out.println("Cust:" + eachCustomElem.getName() + "\tvalue:" + eachCustomElem.getValue());

                    String hashKey = eachResult.getName() + "_" + eachCustomElem.getName() + "_" + eachCustomElem.getValue();
                    //System.out.println("Key:" + hashKey);
                    //System.out.println("Val:" + eachResult.getTime());

                    // insert each result into the hashmap
                    runOneHashMap.put(hashKey, eachResult);
                }
            }
            else
            {
                // every result should have a custom element but this is not always true, the generated results are invalid sometimes
                System.out.println("Result Set 1, Custom Element Missing!:" + eachResult.getName());
            }
        }

        // now iterate thro second result set and compare
        Result[] res2 = resultsDocTwo.getResultSet().getResultArray();
        for (int i = 0; i < res2.length; i++)
        {
            Result eachResult2 = res2[i];

            Custom[] custArr2 = eachResult2.getCustomArray();
            if ((custArr2 != null) || (custArr2.length > 0))
            {
                for (int j = 0; j < custArr2.length; j++)
                {
                    Custom custom = custArr2[j];

                    String hashKeyToLookFor = eachResult2.getName() + "_" + custom.getName() + "_" + custom.getValue();
                    Result resFromHash = (Result) runOneHashMap.get(hashKeyToLookFor);

                    if(resFromHash != null)
                    {
                        if (resFromHash.getTime() == eachResult2.getTime())
                        {
                            // no-op
                            //System.out.println("MATCH:" + hashKeyToLookFor );
                            //System.out.println("First Result Set:" + runOneTime.longValue());
                            //System.out.println("Second Result Set:" + result2.getTime());
                            //System.out.println("------");
                        }
                        else
                        {
                            // compute diff here and compare with tolerence
                            double diffPercent = ((double) resFromHash.getTime() - (double) eachResult2.getTime()) / (double) resFromHash.getTime();
                            diffPercent *= 100;

                            //**System.out.println("% Change= " + diff);
                            String testDetails = "Test Case Name\t:" + resFromHash.getName() + "\n" +
                                                   "Test Case Spec\t:" + resFromHash.getCustomArray(j).getName() + "=" +
                                                                        resFromHash.getCustomArray(j).getValue() +  "\n" +
                                                   "Tolerance Spec\t:" + tolerance;

                            // regression
                            if (diffPercent < 0)
                            {
                                diffPercent *= -1.00;
                                if (diffPercent >= 0)
                                {
                                    if (diffPercent > tolerance)
                                    {
                                        diffPercent *= -1.00;
                                        System.out.println("Regression Found! \n" + testDetails + "\n" + "% Deviation\t\t:" + diffPercent);
                                        System.out.println("------");
                                    }
                                }

                            }

                            // improvement
                            if (diffPercent >= 0)
                            {
                                if (diffPercent > tolerance)
                                {
                                    //System.out.println("Improvement Found! \n" + testDetails + "\n" + "% Deviation\t\t:" + diffPercent);
                                    //System.out.println("------");
                                }
                            }
                        }
                    } // end of hashtable look up result null check
                } // end for
            }
            else
            {
                // every result should have a custom element
                System.out.println("Result Set 2, Custom Element Missing!::" + eachResult2.getName());
            }


        }


    }


    public static void main(String[] args) throws Exception
    {
        if((args.length > 3) || args.length < 2)
        {
            System.out.println("Invalid Number of arguments to RunComparator utility!");
            System.out.println("Usage: java RunComparator <path to run output file 1> <path to run output file 2> [<tolerance>]");
            System.exit(0);
        }

        // TODO: More checking here and add a driver ant task for this utility
        String runOne = args[0];
        String runTwo = args[1];
        double tolerance = 5.00;    // default value
        if(args[2] != null)
        {
            tolerance = Double.parseDouble(args[2]);
        }


        RunComparator rc = new RunComparator(runOne, runTwo, tolerance);
        rc.compare();
    }

}

