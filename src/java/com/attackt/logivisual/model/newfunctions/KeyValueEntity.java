package com.attackt.logivisual.model.newfunctions;

/**
 * 存储键值对
 */
public class KeyValueEntity {
    String key;
    CellIndex value;

    public KeyValueEntity() {
    }

    public KeyValueEntity(String key, CellIndex value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public CellIndex getValue() {
        return value;
    }

    public void setValue(CellIndex value) {
        this.value = value;
    }
}
