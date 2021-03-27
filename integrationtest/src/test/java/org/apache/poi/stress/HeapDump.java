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
package org.apache.poi.stress;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.sun.management.HotSpotDiagnosticMXBean;
import org.apache.poi.util.SuppressForbidden;

@SuppressForbidden("class only exists for manual tests in XSSFFileHandler")
public class HeapDump {
    // This is the name of the HotSpot Diagnostic MBean
    private static final String HOTSPOT_BEAN_NAME =
            "com.sun.management:type=HotSpotDiagnostic";

    // field to store the hotspot diagnostic MBean
    private static volatile HotSpotDiagnosticMXBean hotspotMBean;

    /**
     * Call this method from your application whenever you
     * want to dump the heap snapshot into a file.
     *
     * @param fileName name of the heap dump file
     * @param live flag that tells whether to dump
     *             only the live objects
     */
    public static void dumpHeap(String fileName, boolean live) throws IOException {
        try {
            if (isIbmVm()) {
                dumpHeapJ9(fileName);
            } else {

                // initialize hotspot diagnostic MBean
                initHotspotMBean();
                dumpHeapHotSpot(fileName, live);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    // initialize the hotspot diagnostic MBean field
    private static void initHotspotMBean() throws IOException {
        if (hotspotMBean == null) {
            synchronized (HeapDump.class) {
                if (hotspotMBean == null) {
                    hotspotMBean = getHotspotMBean();
                }
            }
        }
    }

    // get the hotspot diagnostic MBean from the platform MBean server
    private static HotSpotDiagnosticMXBean getHotspotMBean() throws IOException {
        return ManagementFactory.newPlatformMXBeanProxy(ManagementFactory.getPlatformMBeanServer(),
                HOTSPOT_BEAN_NAME, HotSpotDiagnosticMXBean.class);
    }

    private static boolean isIbmVm() {
        try {
            Class.forName("com.ibm.jvm.Dump");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static void dumpHeapJ9(String fileName) throws ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        Class<?> dump = Class.forName("com.ibm.jvm.Dump");
        Method heapDumpToFile = dump.getMethod("heapDumpToFile", String.class);
        heapDumpToFile.invoke(dump, fileName);
    }

    private static void dumpHeapHotSpot(String fileName, boolean live) throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        Method dumpHeap = hotspotMBean.getClass().getMethod("dumpHeap", String.class, boolean.class);
        dumpHeap.invoke(hotspotMBean, fileName, live);
    }
}
