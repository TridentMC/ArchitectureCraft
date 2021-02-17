package com.tridevmc.architecture.common.shape;

public enum EnumShapeCategory {
    ROOFING("architecturecraft.shape.category.roofing"),
    ROUNDED("architecturecraft.shape.category.rounded"),
    CLASSICAL("architecturecraft.shape.category.classical"),
    WINDOW("architecturecraft.shape.category.window"),
    ARCHES("architecturecraft.shape.category.arches"),
    RAILINGS("architecturecraft.shape.category.railings"),
    OTHER("architecturecraft.shape.category.other");

    private final String translationKey;

    EnumShapeCategory(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return this.translationKey;
    }
}
