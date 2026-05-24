package com.graves.persistence;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;

public final class GraveFileStore {

    private static final String FILE_NAME = "graves.json";

    private final JavaPlugin plugin;

    public GraveFileStore(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public Path filePath() {
        return plugin.getDataFolder().toPath().resolve(FILE_NAME);
    }

    public GraveDocument load() {
        Path path = filePath();
        if (!Files.isRegularFile(path)) {
            return GraveJsonCodec.emptyDocument();
        }
        try {
            String json = Files.readString(path, StandardCharsets.UTF_8);
            return GraveJsonCodec.decode(json);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "Could not read " + FILE_NAME, ex);
            return GraveJsonCodec.emptyDocument();
        } catch (IllegalArgumentException ex) {
            plugin.getLogger().log(Level.WARNING, "Could not parse " + FILE_NAME + ": " + ex.getMessage());
            return GraveJsonCodec.emptyDocument();
        }
    }

    public void save(List<GraveEntryDto> graves) {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Could not create plugin data folder");
            return;
        }

        GraveDocument document = new GraveDocument(GraveJsonCodec.CURRENT_VERSION, List.copyOf(graves));
        String json = GraveJsonCodec.encode(document);
        Path path = filePath();
        try {
            Files.writeString(path, json, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.WARNING, "Could not write " + FILE_NAME, ex);
        }
    }
}
