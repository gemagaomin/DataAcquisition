package com.gema.soft.dataacquisition.enums;

public enum WorkTypeSocketEnum {
    ANALYSIS_FILE_END("anl"),
    RECORD_FILE_END("rec"),
    INFO_FILE_END("ini");
    private String id;
    WorkTypeSocketEnum(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
