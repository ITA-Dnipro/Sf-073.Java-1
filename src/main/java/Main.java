import org.example.model.Book;
import org.example.lib.ORManager;
import org.example.model.Publisher;
import java.time.LocalDate;

// 1st part
class Main {
    public static void main(String[] args) {
        String propertiesFilename = "db.properties";
        ORManager ormManager = ORManager.withPropertiesFrom(
                propertiesFilename
        );
        ormManager.register(Book.class, Publisher.class);

        var publisher = new Publisher("MyPub");
        ormManager.persist(publisher);

        var book1 = new Book("Solaris", LocalDate.of(1961, 1, 1));
        ormManager.persist(book1);

        book1.setPublisher(publisher);
        ormManager.merge(book1);

        // ...
    }
}
