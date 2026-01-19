package com.example.search;

import org.apache.tika.Tika;

import java.io.InputStream;

public class TikaService {

    private static final Tika tika = new Tika();

    public static String extractText(InputStream inputStream) throws Exception {
        return tika.parseToString(inputStream);
    }
}
