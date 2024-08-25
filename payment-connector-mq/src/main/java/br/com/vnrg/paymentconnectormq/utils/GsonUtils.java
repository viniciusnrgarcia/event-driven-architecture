package br.com.vnrg.paymentconnectormq.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class GsonUtils {

    public static final Gson INSTANCE = createGson();

    // Private constructor to prevent instantiation
    private GsonUtils() {
    }

    // Create Gson instance with custom settings
    private static Gson createGson() {
        return new GsonBuilder()
                .setPrettyPrinting() // For pretty-printed JSON
                .create();
    }

}
