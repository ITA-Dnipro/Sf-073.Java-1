package org.example.lib.service;

import java.util.function.Supplier;

public class MapperFactory<E> {
    private final Supplier<Mapper<E>> supplier;

    public MapperFactory(Supplier<Mapper<E>> supplier) {
        this.supplier = supplier;
    }

    public Mapper<E> getMapper() {
        return supplier.get();
    }
}

