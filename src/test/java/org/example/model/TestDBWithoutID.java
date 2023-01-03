package org.example.model;

import org.example.lib.annotations.Column;
import org.example.lib.annotations.Entity;
import org.example.lib.annotations.Table;

import java.io.Serializable;

@Entity
@Table("testWithoutID")
public class TestDBWithoutID implements TestClass, Serializable {
    @Column("id")
    private Long idTable;
    private String name;

    public TestDBWithoutID() {
    }

    public TestDBWithoutID(String name) {
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
