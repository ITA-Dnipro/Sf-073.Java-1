import org.example.lib.utils.Utils;
import org.example.model.Book;
import org.example.lib.ORManager;
import org.example.model.Publisher;
import java.time.LocalDate;

// 1st part
class Main {
    public static void main(String[] args) {
        String propertiesFilename = "db.properties";
        ORManager ormManager = Utils.getORMImplementation(
                propertiesFilename
        );
        ormManager.register(Book.class, Publisher.class);

        var publisher = new Publisher("MyPub");
        ormManager.persist(publisher);

        var book1 = new Book("Solaris", LocalDate.of(1961, 1, 1));
        ormManager.persist(book1);

        book1.setPublisher(publisher);
        ormManager.merge(book1);

        System.out.println(publisher);

        var found = ormManager.findById(1L,Book.class);
        System.out.println(found.toString());


        // ...
    }
}
