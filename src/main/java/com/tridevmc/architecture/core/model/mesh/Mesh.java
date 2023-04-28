package com.tridevmc.architecture.core.model.mesh;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.physics.AABBTree;
import com.tridevmc.architecture.core.physics.IAABBTree;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Default implementation of {@link IMesh}.
 */
public class Mesh<I, D extends IPolygonData<D>> implements IMesh<I, D> {

    private final ImmutableMap<I, IPart<I, D>> parts;
    private final ImmutableList<IFace<D>> faces;
    private final AABBTree<IPolygon<D>> aabbTree;

    /**
     * Creates a new mesh with the given faces.
     *
     * @param faces The faces of the mesh.
     */
    public Mesh(@NotNull ImmutableMap<I, IPart<I, D>> parts, @NotNull ImmutableList<IFace<D>> faces) {
        this.parts = parts;
        this.faces = faces;
        this.aabbTree = new AABBTree<>(
                this.getFaceStream().flatMap(IFace::getPolygonStream).toList(),
                IPolygon::getAABB
        );
    }

    @Override
    public @NotNull ImmutableMap<I, IPart<I, D>> getParts() {
        return this.parts;
    }

    @Override
    public @NotNull IPart<I, D> getPart(I id) {
        return this.parts.get(id);
    }

    @Override
    public @NotNull ImmutableList<IFace<D>> getFaces() {
        return this.faces;
    }

    @Override
    public @NotNull IAABBTree<IPolygon<D>> getAABBTree() {
        return this.aabbTree;
    }

    @Override
    @NotNull
    public IMesh<I, D> transform(@NotNull ITrans3 trans, boolean transformUVs) {
        var builder = new Builder<I, D>();
        for (var p : this.getParts().values()) {
            builder.addPart(p.transform(trans, transformUVs));
        }
        return builder.build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Mesh<?, ?> mesh)) return false;
        // We can just compare the parts, as the faces are derived from the parts and the aabb tree is derived from the faces.
        return Objects.equals(this.getParts(), mesh.getParts());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getParts());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("parts", this.parts)
                .add("faces", this.faces)
                .add("aabbTree", this.aabbTree)
                .toString();
    }

    /**
     * Builder for {@link Mesh} instances.
     *
     * @param <D> The type of data stored on the polygons of the mesh.
     */
    public static class Builder<I, D extends IPolygonData<D>> {

        private final Map<I, IPart<I, D>> parts = new HashMap<>();
        private final List<IFace<D>> faces = new ArrayList<>();

        /**
         * Adds a part to the mesh.
         *
         * @param part The part to add.
         * @return This builder.
         */
        public Builder<I, D> addPart(IPart<I, D> part) {
            this.parts.put(part.getId(), part);
            // We also need to add the faces of the part to the mesh.
            this.faces.addAll(part.getFaces());
            return this;
        }

        /**
         * Builds a new {@link Mesh} instance.
         *
         * @return The new mesh.
         */
        public Mesh<I, D> build() {
            return new Mesh<>(ImmutableMap.copyOf(this.parts), ImmutableList.copyOf(this.faces));
        }

    }

}
