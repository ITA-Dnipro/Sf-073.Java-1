package org.example.model;
import lombok.Data;
import org.example.lib.annotations.*;
import java.time.LocalDate;

@Data
@Entity
@Table("books")
public class Book {
    @Id
    private Long id;
    private String title;
    @Column("published_at")
    private LocalDate publishedAt;
    // 2nd stage:
    @ManyToOne(columnName = "publisher_id")
    private Publisher publisher = null;

    public Book() {
    }

    public Book(String title, LocalDate publishedAt) {
        this.title = title;
        this.publishedAt = publishedAt;
    }
}
