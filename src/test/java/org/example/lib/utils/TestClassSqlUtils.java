package org.example.lib.utils;


import org.example.lib.annotations.Column;
import org.example.lib.annotations.Entity;
import org.example.lib.annotations.Id;
import org.example.lib.annotations.Table;

import java.math.BigDecimal;

@Entity
@Table("books")
public class TestClassSqlUtils {
    @Id
    private Long table;
    @Id(value = Id.IDType.UUID)
    private Long tableUUID;
    private Long tableLong;
    private String tableString;
    private int tableInt;
    @Column
    private long title;
    private boolean tableBoolean;
    private byte tableByte;
    private char tableChar;

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
