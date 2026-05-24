package com.graves.persistence;

import java.util.Map;

public record SlottedItemDto(int slot, Map<String, Object> stack) {
}
