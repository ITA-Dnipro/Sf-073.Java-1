package org.example.model;

import lombok.Data;
import org.example.lib.annotations.Entity;
import org.example.lib.annotations.Id;
import org.example.lib.annotations.OneToMany;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
public class Author {
    @Id
    private Long id;
    private String name;

    // 2nd stage
    @OneToMany(mappedBy = "author")
    private List<Book> books = new ArrayList<>();

    public Author() {
    }

    public Author(String name) {
        this.name = name;
    }


    public List<Book> getBooks() {
        return books;
    }
}
