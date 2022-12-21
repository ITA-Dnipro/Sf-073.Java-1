package org.example.model;

import org.example.lib.annotations.Entity;
import org.example.lib.annotations.Id;
import org.example.lib.annotations.OneToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Publisher {
    @Id
    private Long id;
    private String name;

    public Publisher() {
    }

    public Publisher(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // 2nd stage
    @OneToMany(mappedBy = "publisher")
    private List<Book> books = new ArrayList<>();

    public List<Book> getBooks() {
        return books;
    }
}
