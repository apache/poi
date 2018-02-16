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

(ns poi.core
    (:gen-class)
    (:use [clojure.java.io :only [input-stream]])
    (:import [org.apache.poi.ss.usermodel WorkbookFactory DataFormatter]))


(defn sheets [wb] (map #(.getSheetAt wb %1) (range 0 (.getNumberOfSheets wb))))

(defn print-all [wb]
  (let [df (DataFormatter.)]
    (doseq [sheet (sheets wb)]
      (doseq [row (seq sheet)]
        (doseq [cell (seq row)]
          (println (.formatAsString (.getAddress cell)) ": " (.formatCellValue df cell)))))))

(defn -main [& args]
  (when-let [name (first args)]
    (let [wb (WorkbookFactory/create (input-stream name))]
      (print-all wb))))
