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
package org.apache.xmlbeans.test.performance.parsers;

import java.io.OutputStream;
import java.io.IOException;
import java.io.File;

/**
 * @author Cezar Andrei (cezar.andrei at bea.com)
 *         Date: Jul 12, 2005
 */
public class Utils
{
    public static final String P = File.separator;
    public static final String INSTANCES_DIR = org.apache.xmlbeans.test.performance.utils.Constants.XML_DIR + P;

    public static String file1k = INSTANCES_DIR + "purchase-order-1k.xml";
    public static String file10k = INSTANCES_DIR + "purchase-order-10k.xml";
    public static String file100k = INSTANCES_DIR + "purchase-order-100k.xml";
    public static String file1M = INSTANCES_DIR + "purchase-order-1M.xml";
    public static String file10M = INSTANCES_DIR + "purchase-order-10M.xml";

    public static int TIME_THRESHHOLD_MS = 2000; // 2sec threshold time for a test
    public static int COUNT_WARMING = 5;

    public static String printTime(long time, int count)
    {
        if (time==0 || count==0)
            throw new IllegalArgumentException("time=" + time + " count=" + 0);

        String res = printDouble(((double)time)/count) + " ms/case ";

      
        if (((double)time/(count*1000))>1)
            res += printDouble((double)(time/(1000*count))) + " s/case (count: " + count + " in " + time + " ms)";
        else if (((double)time/count)>1)
            res += printDouble((double)time/count) + " ms/case (count: " + count + " in " + time + " ms)";
        else
            res += printDouble((double)(time*1000)/count) + " ns/case (count: " + count + " in " + time + " ms)";

        return res;
    }

    public static String printDouble(double d)
    {
        if (d<0.1)
            return "time " + (double)((int)(d*10000))/10000;

        return "time " + (double)((int)(d*100))/100;
    }

    public static abstract class ParseFile
    {
        private boolean _failed = false;

        public void run(String file)
            throws Exception
        {
            preRun();
            _failed = false;
            try
            {
                long time = System.currentTimeMillis();
                for (int i = 0; i < COUNT_WARMING; i++)
                {
                    execute(file);
                }
                time = System.currentTimeMillis() - time;

                double timeOfOneRun = time / COUNT_WARMING;
                int count = time < 1 ? TIME_THRESHHOLD_MS :
                    timeOfOneRun > TIME_THRESHHOLD_MS ? 1 :
                        1 + (int) (TIME_THRESHHOLD_MS / timeOfOneRun);
               //System.out.println("  time: " + time + " for count_warming: " + COUNT_WARMING + " => count: " + count +
               //   " ver: " + count*timeOfOneRun + " ms");

                time = System.currentTimeMillis();
                for (int i = 0; i < count; i++)
                {
                    execute(file);
                }
                time = System.currentTimeMillis() - time;

                //System.out.println("  TEST: " + this.getClass().getName() + " " + printTime(time, count) + " " + file);
                System.out.println("TEST: " + this.getClass().getName() + " file=" + file + " hash 000000 " + printTime(time, count) + " " );
            }
            catch(Exception e)
            {
                _failed = true;
                System.out.println("  FAILED Test: " + this.getClass().getName() + " " + file);
                e.printStackTrace(System.out);
            }
            finally
            {
                postRun();
            }
        }

        public abstract void execute(String file)
            throws Exception;

        public void preRun() {}
        public void postRun() {}

        public boolean isFailed()
        {
            return _failed;
        }
    }

    public static class NullOutputStream
        extends OutputStream
    {
        public void write(int b)
            throws IOException
        {
            // do nothing
        }
    }
}
