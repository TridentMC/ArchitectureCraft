package com.tridevmc.architecture.client.render.model;

/**
 * Interface that defines a model state for use in caching in ArchitectureModelData.
 * <p>
 * Capable of performing transforms, as well as texture applications.
 */
public interface IArchitectureModelState {


    int hashCode();

    boolean equals(Object obj);
}
