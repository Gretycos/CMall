package com.tsong.cmall.config.enums;

/**
 * @Author Tsong
 * @Date 2023/6/27 16:31
 */
public enum DBTypeEnum {
    MASTER(0,"master"), SLAVE(1,"slave");
    private int dbType;
    private String name;

    DBTypeEnum(int dbType, String name) {
        this.dbType = dbType;
        this.name = name;
    }

    public int getDbType() {
        return dbType;
    }

    public void setDbType(int dbType) {
        this.dbType = dbType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
