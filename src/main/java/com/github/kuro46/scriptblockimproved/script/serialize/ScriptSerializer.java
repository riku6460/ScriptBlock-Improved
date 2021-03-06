package com.github.kuro46.scriptblockimproved.script.serialize;

import com.github.kuro46.scriptblockimproved.script.ScriptMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.NonNull;

public final class ScriptSerializer {

    private static final String FORMAT_VERSION = "1";
    private static final Lock IO_LOCK = new ReentrantLock();

    private ScriptSerializer() {
        throw new UnsupportedOperationException();
    }

    private static Gson gson() {
        return new GsonBuilder()
            .serializeNulls()
            .create();
    }

    public static void serialize(
            final Path path,
            final ScriptMap scripts,
            final boolean overwrite) throws IOException {
        IO_LOCK.lock();
        try {
            if (!overwrite && Files.exists(path)) {
                throw new IOException("File already exists");
            }

            createFileIfNeeded(path);

            try (final BufferedWriter writer = Files.newBufferedWriter(path)) {
                serialize(writer, scripts);
            }
        } finally {
            IO_LOCK.unlock();
        }
    }

    private static void createFileIfNeeded(final Path path) throws IOException {
        if (Files.exists(path)) {
            return;
        }

        Files.createFile(path);
    }

    public static void serialize(
            @NonNull final Appendable writer,
            @NonNull final ScriptMap scripts) {
        final JsonObject root = new JsonObject();
        root.add("meta", createMeta());
        root.add("scripts", scripts.toJson());
        gson().toJson(root, writer);
    }

    private static JsonObject createMeta() {
        final JsonObject json = new JsonObject();
        json.addProperty("version", FORMAT_VERSION);
        return json;
    }

    public static ScriptMap deserialize(@NonNull final Reader reader)
            throws UnsupportedVersionException {
        final JsonObject root = gson().fromJson(reader, JsonObject.class);
        final Meta meta = Meta.fromJson(root.getAsJsonObject("meta"));
        if (!meta.getVersion().equals(FORMAT_VERSION)) {
            throw new UnsupportedVersionException(meta.getVersion());
        }

        return ScriptMap.fromJson(root.getAsJsonArray("scripts"));
    }
}
