package org.example.lib.utils;


import org.example.lib.annotations.Id;

import java.math.BigDecimal;


public class TestClassSqlUtils {
    @Id
    private Long table;
    @Id(value = Id.IDType.UUID)
    private Long tableUUID;
    private Long tableLong;
    private String tableString;
    private int tableInt;
    private boolean tableBoolean;
    private BigDecimal bigDecimal;


    public TestClassSqlUtils() {
    }

    public TestClassSqlUtils(Long table) {
        this.table = table;
    }
    public TestClassSqlUtils(BigDecimal bigDecimal) {
        this.bigDecimal = bigDecimal;
    }

    @Override
    public String toString() {
        return "TestClassSqlUtils{" +
                "bigDecimal=" + bigDecimal +
                '}';
    }
}
