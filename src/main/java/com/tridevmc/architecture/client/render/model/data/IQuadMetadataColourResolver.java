package com.tridevmc.architecture.client.render.model.data;

@FunctionalInterface
public interface IQuadMetadataColourResolver<T> {
    int getColour(T metadata);
}
