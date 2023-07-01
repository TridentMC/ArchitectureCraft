package com.tridevmc.architecture.legacy.common.shape;

@Deprecated
public enum LegacyEnumShapeCategory {
    ROOFING("architecturecraft.shape.category.roofing"),
    ROUNDED("architecturecraft.shape.category.rounded"),
    CLASSICAL("architecturecraft.shape.category.classical"),
    WINDOW("architecturecraft.shape.category.window"),
    ARCHES("architecturecraft.shape.category.arches"),
    RAILINGS("architecturecraft.shape.category.railings"),
    OTHER("architecturecraft.shape.category.other");

    private final String translationKey;

    LegacyEnumShapeCategory(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return this.translationKey;
    }
}
