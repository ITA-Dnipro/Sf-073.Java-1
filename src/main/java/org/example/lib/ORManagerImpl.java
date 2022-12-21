package org.example.lib;

import lombok.extern.slf4j.Slf4j;
import org.example.lib.annotations.*;

import javax.sql.DataSource;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Stream;

@Slf4j
public class ORManagerImpl implements ORManager {
    private final DataSource dataSource;

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public ORManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void register(Class... entityClasses) {
        for (Class currClass : entityClasses) {
            if (!currClass.isAnnotationPresent(Entity.class)) continue;
            String nameOfTable = currClass.getSimpleName();
            if (currClass.isAnnotationPresent(Table.class)) {
                Table ta = (Table) currClass.getAnnotation(Table.class);
                nameOfTable = ta.value().isEmpty() ? nameOfTable : ta.value();
            }

            Field[] declaredFields = currClass.getDeclaredFields();
            String pkeyName = "";
            ArrayList<Field> columns = new ArrayList<>();
            StringJoiner joiner = new StringJoiner(",");
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(Id.class)) {
                    String typeOfPKEYField = getTypeOfPrimaryKeyFieldSQL(field);
                    pkeyName = field.getName()+" "+typeOfPKEYField+" PRIMARY KEY,";
                } else if (field.isAnnotationPresent(ManyToOne.class)) {
                    //to do
                } else if (field.isAnnotationPresent(OneToMany.class)) {
                    //to do
                } else {
                    String typeOfField = getTypeOfFieldSQL(field);
                    String nameOfField = field.getName();
                    if (field.isAnnotationPresent(Column.class)) {
                        Column fld = field.getAnnotation(Column.class);
                        nameOfField = fld.value().isEmpty() ? nameOfField : fld.value();
                    }
                    String str = nameOfField +
                            " " + typeOfField;

                    joiner.add(str);
                    columns.add(field);
                }
            }

            String sql = "CREATE TABLE IF NOT EXISTS " + nameOfTable +
                    " (" +pkeyName
                    +joiner
                    +")";

            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
            } catch (SQLException se) {
                log.warn("An error while init DB"+se.getMessage());
            }
        }
    }

    private String getTypeOfPrimaryKeyFieldSQL(Field field) {
        String typeSQL = getTypeOfFieldSQL(field);
        if (typeSQL.equals("INTEGER")) {
            Id fld = field.getAnnotation(Id.class);
            IDType currType = fld.value();

            if (field.isAnnotationPresent(Id.class)) {
                Id annFieldType = field.getAnnotation(Id.class);
                currType = annFieldType.value();
            }
            if (currType == IDType.SERIAL) {
                typeSQL = "SERIAL";
            } else typeSQL = "BINARY(16)";
        }
        return typeSQL;
    }

    private String getTypeOfFieldSQL(Field field) {
        var type = field.getType();
        String typeSQL = "";
        if (type == Enum.class) {
            EnumType currType = null;
            try {
                var method = Enumerated.class.getDeclaredMethod("value");
                currType = (EnumType) method.invoke(null);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException se) {
                log.warn("An error while get type of field "+field.getName()+":"+se.getMessage());
            }

            if (field.isAnnotationPresent(Enumerated.class)) {
                Enumerated annFieldType = field.getAnnotation(Enumerated.class);
                currType = annFieldType.value();
            }
            if (currType == EnumType.STRING) {
                return "VARCHAR(255)";
            }

            return "INTEGER";
        } else if (type == int.class) {
            return "INTEGER";
        } else if (type == Integer.class) {
            return "INTEGER";
        } else if (type == long.class) {
            return "BIGINT";
        } else if (type == Long.class) {
            return "BIGINT";
        } else if (type == double.class) {
            return "DOUBLE";
        } else if (type == Double.class) {
            return "DOUBLE";
        } else if (type == boolean.class) {
            return "BIT";
        } else if (type == Boolean.class) {
            return "BIT";
        } else if (type == LocalDate.class) {
            return "DATE";
        } else if (type == LocalTime.class) {
            return "TIME";
        } else if (type == LocalDateTime.class) {
            return "DATE";
        } else if (type == Instant.class) {
            return "TIMESTAMP";
        } else if (type == BigDecimal.class) {
            return "NUMERIC";
        } else if (type == String.class) {
            return "VARCHAR(255)";
        }
        return typeSQL;
    }

    @Override
    public <T> T save(T o) {
        return null;
    }

    @Override
    public void persist(Object o) {

    }

    @Override
    public <T> Optional<T> findById(Serializable id, Class<T> cls) {
        return Optional.empty();
    }

    @Override
    public <T> List<T> findAll(Class<T> cls) {
        return null;
    }

    @Override
    public <T> Iterable<T> findAllAsIterable(Class<T> cls) {
        return null;
    }

    @Override
    public <T> Stream<T> findAllAsStream(Class<T> cls) {
        return null;
    }

    @Override
    public <T> T merge(T o) {
        return null;
    }

    @Override
    public <T> T refresh(T o) {
        return null;
    }

    @Override
    public boolean delete(Object o) {
        return false;
    }
}
