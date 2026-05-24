package com.graves;

import java.util.UUID;

public final class GraveCollectionRules {

    private GraveCollectionRules() {
    }

    public static boolean canCollect(UUID playerId, Grave grave) {
        return !grave.isCollected() && grave.ownerId().equals(playerId);
    }
}
