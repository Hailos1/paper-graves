package com.graves.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON serialization for grave documents (no Bukkit types).
 */
public final class GraveJsonCodec {

    public static final int CURRENT_VERSION = 1;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private GraveJsonCodec() {
    }

    public static String encode(GraveDocument document) {
        return GSON.toJson(document);
    }

    public static GraveDocument decode(String json) {
        if (json == null || json.isBlank()) {
            return emptyDocument();
        }
        try {
            GraveDocument document = GSON.fromJson(json, GraveDocument.class);
            if (document == null) {
                return emptyDocument();
            }
            List<GraveEntryDto> graves = document.graves() != null ? document.graves() : List.of();
            return new GraveDocument(document.version(), List.copyOf(graves));
        } catch (JsonSyntaxException ex) {
            throw new IllegalArgumentException("Invalid graves.json", ex);
        }
    }

    public static GraveDocument emptyDocument() {
        return new GraveDocument(CURRENT_VERSION, List.of());
    }

    /**
     * Splits entries into active (expires after {@code now}) and expired.
     */
    public static GraveExpirySplit splitByExpiry(List<GraveEntryDto> entries, Instant now) {
        List<GraveEntryDto> active = new ArrayList<>();
        List<GraveEntryDto> expired = new ArrayList<>();
        for (GraveEntryDto entry : entries) {
            if (isExpired(entry, now)) {
                expired.add(entry);
            } else {
                active.add(entry);
            }
        }
        return new GraveExpirySplit(List.copyOf(active), List.copyOf(expired));
    }

    public static boolean isExpired(GraveEntryDto entry, Instant now) {
        Instant expiresAt = Instant.parse(entry.expiresAtUtc());
        return !expiresAt.isAfter(now);
    }

    public record GraveExpirySplit(List<GraveEntryDto> active, List<GraveEntryDto> expired) {
    }
}
