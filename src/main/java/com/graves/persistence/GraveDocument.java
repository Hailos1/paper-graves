package com.graves.persistence;

import java.util.List;

public record GraveDocument(int version, List<GraveEntryDto> graves) {
}
