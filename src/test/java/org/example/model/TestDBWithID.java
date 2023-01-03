package org.example.model;

import org.example.lib.annotations.Column;
import org.example.lib.annotations.Entity;
import org.example.lib.annotations.Id;
import org.example.lib.annotations.Table;

import java.io.Serializable;

@Entity
@Table("testID")
public class TestDBWithID implements TestClass, Serializable {
    @Id
    @Column("id")
    private Long idTable;
    private String name;

    public TestDBWithID() {
    }

    public TestDBWithID(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
