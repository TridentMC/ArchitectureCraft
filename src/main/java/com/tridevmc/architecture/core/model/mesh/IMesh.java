package com.tridevmc.architecture.core.model.mesh;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.physics.AABB;
import com.tridevmc.architecture.core.physics.IAABBTree;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

/**
 * Common interface used to represent a mesh and provide access to its faces.
 */
public interface IMesh<I, D extends IPolygonData<D>> {

    /**
     * Gets the parts of this mesh.
     *
     * @return The parts of this mesh.
     */
    @NotNull
    ImmutableMap<I, IPart<I, D>> getParts();

    /**
     * Gets the part of this mesh with the given identifier.
     *
     * @param id The identifier of the part to get.
     * @return The part with the given identifier.
     * @throws IllegalArgumentException If no part with the given identifier exists.
     */
    @NotNull
    IPart<I, D> getPart(I id);

    /**
     * Gets the faces of this mesh.
     *
     * @return The faces of this mesh.
     */
    @NotNull
    ImmutableList<IFace<D>> getFaces();

    /**
     * Gets all the polygons of this mesh contained within an AABB tree for fast lookup.
     *
     * @return The AABB tree containing all the polygons of this mesh.
     */
    @NotNull
    IAABBTree<IPolygon<D>> getAABBTree();

    /**
     * Applies the given transformation to this mesh, returning a new mesh with the transformed parts and faces.
     *
     * @param trans        The transformation to apply.
     * @param transformUVs Whether to transform the UVs of the mesh.
     * @return A new mesh with the transformed parts and faces.
     */
    @NotNull
    IMesh<I, D> transform(@NotNull ITrans3 trans, boolean transformUVs);

    /**
     * Applies the given transformation to this mesh, returning a new mesh with the transformed parts and faces.
     *
     * @param trans The transformation to apply.
     * @return A new mesh with the transformed parts and faces.
     */
    @NotNull
    default IMesh<I, D> transform(@NotNull ITrans3 trans) {
        return this.transform(trans, true);
    }

    /**
     * Gets all polygons in the mesh that intersect the given AABB.
     *
     * @param box The AABB to test against.
     * @return A list of all polygons that intersect the given AABB.
     */
    @NotNull
    default List<IPolygon<D>> search(@NotNull AABB box) {
        return this.getAABBTree().search(box);
    }

    /**
     * Gets all polygons in the mesh that intersect the given AABB, uses a stream to avoid calculating all intersections at once.
     *
     * @param box The AABB to test against.
     * @return A stream of all polygons that intersect the given AABB.
     */
    @NotNull
    default Stream<IPolygon<D>> searchStream(@NotNull AABB box) {
        return this.getAABBTree().searchStream(box);
    }

    /**
     * Gets the bounds of this mesh using the AABBTree.
     *
     * @return The bounds of this mesh.
     */
    @NotNull
    default AABB getBounds() {
        return this.getAABBTree().getBounds();
    }

    /**
     * Gets a stream of the parts of this mesh.
     *
     * @return A stream of the parts of this mesh.
     */
    @NotNull
    default Stream<IPart<I, D>> getPartStream() {
        return this.getParts().values().stream();
    }

    /**
     * Gets a stream of the faces of this mesh.
     *
     * @return A stream of the faces of this mesh.
     */
    @NotNull
    default Stream<IFace<D>> getFaceStream() {
        return this.getFaces().stream();
    }

}
