package org.example.model;
import org.example.lib.annotations.*;
import java.time.LocalDate;

@Entity
@Table("books")
public class Book {
    @Id
    private Long id;
    private String title;
    @Column("published_at")
    private LocalDate publishedAt;

    public Book() {
    }

    public Book(String title, LocalDate publishedAt) {
        this.title = title;
        this.publishedAt = publishedAt;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDate publishedAt) {
        this.publishedAt = publishedAt;
    }

    // 2nd stage:
    @ManyToOne(columnName = "publisher_id")
    Publisher publisher = null;

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }
}
