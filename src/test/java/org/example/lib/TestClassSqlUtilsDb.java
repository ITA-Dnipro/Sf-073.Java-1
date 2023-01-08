package org.example.lib;

import org.example.lib.annotations.Column;
import org.example.lib.annotations.Entity;
import org.example.lib.annotations.Id;
import org.example.lib.annotations.Table;


@Entity
@Table("books")
public class TestClassSqlUtilsDb {

    @Id
    private Long Id;
    @Column
    private String title;

    public TestClassSqlUtilsDb() {
    }
    public TestClassSqlUtilsDb(Long Id, String title) {
        this.Id = Id;
        this.title = title;
    }

    @Override
    public String toString() {
        return "TestClassSqlUtilsDb{" +
                "Id=" + Id +
                ", title='" + title + '\'' +
                '}';
    }
}
