package org.example.model;

import org.example.lib.annotations.Column;
import org.example.lib.annotations.Entity;
import org.example.lib.annotations.Id;
import org.example.lib.annotations.Table;

import java.io.Serializable;

@Entity
@Table("testUUID")
public class TestDBWithUUID implements TestClass, Serializable {
    @Id(Id.IDType.UUID)
    @Column("id")
    private Long idTable;
    private String name;

    public TestDBWithUUID(String name) {
        this.name = name;
    }

    public TestDBWithUUID() {
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
