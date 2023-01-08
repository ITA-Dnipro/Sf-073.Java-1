package org.example.model;

import org.example.lib.annotations.Column;
import org.example.lib.annotations.Entity;
import org.example.lib.annotations.Id;

import java.io.Serializable;

@Entity
public class TestDBWithIDWithoutTable implements TestClass, Serializable {
    @Id
    @Column("id")
    private Long idTable;
    private String name;

    public TestDBWithIDWithoutTable() {
    }

    TestDBWithIDWithoutTable(String name) {
        this.name = name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }
}
