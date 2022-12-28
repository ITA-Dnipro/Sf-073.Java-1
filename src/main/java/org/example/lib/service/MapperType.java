package org.example.lib.service;

import org.example.mapper.BookMapper;
import org.example.mapper.PublisherMapper;
import org.example.model.Book;
import org.example.model.Publisher;

public enum MapperType {
    BOOK {
        @Override
        @SuppressWarnings("unchecked")
        public Mapper<Book> make() {
            return new BookMapper();
        }
    },
    PUBLISHER {
        @Override
        @SuppressWarnings("unchecked")
        public Mapper<Publisher> make() {
            return new PublisherMapper();
        }
    };

    public abstract <T> Mapper<T> make();
}
