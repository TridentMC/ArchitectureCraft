package com.tridevmc.architecture.core.physics;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

/**
 * An interface for an AABB tree, provides read only access to the tree.
 *
 * @param <T> The type of data stored in the tree.
 */
public interface IAABBTree<T> {

    /**
     * Searches the tree for all elements that intersect with the given box, uses a stream to avoid calculating all intersections at once.
     *
     * @param box The AABB to search for.
     * @return A stream of all elements that intersect the given AABB.
     */
    @NotNull
    Stream<T> searchStream(@NotNull AABB box);

    /**
     * Gets the bounds of the tree.
     *
     * @return The bounds of the tree.
     */
    @NotNull
    AABB getBounds();

    /**
     * Searches the tree for all elements that intersect the given AABB.
     *
     * @param box The AABB to search for.
     * @return A list of all elements that intersect the given AABB.
     */
    @NotNull
    default List<T> search(@NotNull AABB box) {
        return this.searchStream(box).toList();
    }

}
