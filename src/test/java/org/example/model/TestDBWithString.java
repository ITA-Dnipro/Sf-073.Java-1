package org.example.model;

import org.example.lib.annotations.Column;
import org.example.lib.annotations.Entity;
import org.example.lib.annotations.Id;
import org.example.lib.annotations.Table;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table("testUUID")
public class TestDBWithString implements TestClass, Serializable {
    @Id(Id.IDType.UUID)
    @Column("id")
    private String idTable;
    private String name;

    public TestDBWithString(String name) {
        this.name = name;
    }

    public TestDBWithString() {
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
