package org.apache.poi.xssf.model;

/**
 * enum to specify shared strings table to use
 */
public enum SharedStringsTableType {
    DEFAULT_SST(SharedStringsTable.class),//in memory shared strings string table
    LOW_FOOTPRINT_MAP_DB_SST(DBMappedSharedStringsTable.class); //streaming version low foot print shared strings table
    /**
     * Defines what object is used to construct instances of this relationship
     */
    private Class<? extends SharedStringsTable> instance;

    private SharedStringsTableType(Class<? extends SharedStringsTable> sharedStringsTableInstance) {
        instance = sharedStringsTableInstance;
    }

    public Class<? extends SharedStringsTable> getInstance() {
        return instance;
    }
}
