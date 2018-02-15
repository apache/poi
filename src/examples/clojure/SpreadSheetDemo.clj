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
