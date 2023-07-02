package com.tridevmc.architecture.common.shape.orientation;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines an object that stores a set of properties used to orient a shape in the world, should be heavily cached to avoid heap bloat.
 */
public record ShapeOrientation(
        ShapeOrientationProperty.Value<?>... properties
) {

    public static final ShapeOrientation IDENTITY = new ShapeOrientation();
    private static final ShapeOrientationCache CACHE = new ShapeOrientationCache();

    private static class ShapeOrientationCache {

        private static class Node {

            private final ShapeOrientation orientation;
            private final Map<ShapeOrientationProperty.Value<?>, Node> children = new HashMap<>();

            public Node(ShapeOrientation orientation) {
                this.orientation = orientation;
            }

            public Node get(ShapeOrientationProperty.Value<?> property) {
                // Get or create a node for the given property.
                return this.children.computeIfAbsent(property, (p) -> new Node(this.orientation.withProperty(p)));
            }

        }

        private final Node root = new Node(ShapeOrientation.IDENTITY);

        public ShapeOrientation get(ShapeOrientationProperty.Value<?>... properties) {
            var node = this.root;
            for (ShapeOrientationProperty.Value<?> property : Arrays.stream(properties).sorted(Comparator.comparingInt(p -> p.property().hashCode())).toList()) {
                node = node.get(property);
            }
            return node.orientation;
        }

        public ShapeOrientation get(ShapeOrientationProperty.Value<?> property) {
            var node = this.root;
            node = node.get(property);
            return node.orientation;
        }

        public ShapeOrientation get(ShapeOrientationProperty.Value<?> property1, ShapeOrientationProperty.Value<?> property2) {
            var node = this.root;
            var prop1 = property1.property().hashCode();
            var prop2 = property2.property().hashCode();
            node = node.get(prop1 < prop2 ? property1 : property2);
            node = node.get(prop1 < prop2 ? property2 : property1);
            return node.orientation;
        }

        public ShapeOrientation get(ShapeOrientationProperty.Value<?> property1, ShapeOrientationProperty.Value<?> property2, ShapeOrientationProperty.Value<?> property3) {
            var node = this.root;
            var prop1 = property1.property().hashCode();
            var prop2 = property2.property().hashCode();
            var prop3 = property3.property().hashCode();
            // My brain hurts.
            node = node.get(prop1 < prop2 ? (prop1 < prop3 ? property1 : property3) : (prop2 < prop3 ? property2 : property3));
            node = node.get(prop1 < prop2 ? (prop2 < prop3 ? property2 : property3) : (prop1 < prop3 ? property1 : property3));
            node = node.get(prop1 < prop2 ? (prop1 < prop3 ? property3 : property1) : (prop2 < prop3 ? property3 : property2));
            return node.orientation;
        }

    }

    /**
     * Gets an orientation with the given properties, creating one if a cached entry cannot be found.
     *
     * @param properties the properties to get an orientation for.
     * @return the orientation with the given properties.
     */
    public static ShapeOrientation forProperties(ShapeOrientationProperty.Value<?>... properties) {
        return CACHE.get(properties);
    }

    /**
     * Gets an orientation with the given property, creating one if a cached entry cannot be found.
     *
     * @param property the property to get an orientation for.
     * @return the orientation with the given property.
     */
    public static ShapeOrientation forProperties(ShapeOrientationProperty.Value<?> property) {
        return CACHE.get(property);
    }

    /**
     * Gets an orientation with the given properties, creating one if a cached entry cannot be found.
     * @param property1 the first property to get an orientation for.
     * @param property2 the second property to get an orientation for.
     * @return the orientation with the given properties.
     */
    public static ShapeOrientation forProperties(ShapeOrientationProperty.Value<?> property1, ShapeOrientationProperty.Value<?> property2) {
        return CACHE.get(property1, property2);
    }

    /**
     * Gets an orientation with the given properties, creating one if a cached entry cannot be found.
     * @param property1 the first property to get an orientation for.
     * @param property2 the second property to get an orientation for.
     * @param property3 the third property to get an orientation for.
     * @return the orientation with the given properties.
     */
    public static ShapeOrientation forProperties(ShapeOrientationProperty.Value<?> property1, ShapeOrientationProperty.Value<?> property2, ShapeOrientationProperty.Value<?> property3) {
        return CACHE.get(property1, property2, property3);
    }

    /**
     * Applies this orientation to a block state.
     *
     * @param state the state to apply this orientation to.
     * @return the state with this orientation applied.
     */
    public BlockState applyToState(BlockState state) {
        for (ShapeOrientationProperty.Value<?> property : this.properties) {
            state = property.applyToState(state);
        }
        return state;
    }

    private ShapeOrientation withProperty(ShapeOrientationProperty.Value<?> property) {
        if (this.properties.length == 0) {
            return new ShapeOrientation(property);
        } else {
            // Check if there is already a property of the same type.
            for (int i = 0; i < this.properties.length; i++) {
                if (this.properties[i].property() == property.property()) {
                    // If there is, replace it.
                    ShapeOrientationProperty.Value<?>[] newProperties = this.properties.clone();
                    newProperties[i] = property;
                    return new ShapeOrientation(newProperties);
                }
            }
            // If there isn't, add it.
            ShapeOrientationProperty.Value<?>[] newProperties = new ShapeOrientationProperty.Value<?>[this.properties.length + 1];
            System.arraycopy(this.properties, 0, newProperties, 0, this.properties.length);
            newProperties[this.properties.length] = property;
            return new ShapeOrientation(newProperties);
        }
    }

    public  <V extends Enum<V> & StringRepresentable> ShapeOrientationProperty.Value<V> getValue(ShapeOrientationProperty<V> property) {
        for (ShapeOrientationProperty.Value<?> prop : this.properties) {
            if (prop.property() == property) {
                return (ShapeOrientationProperty.Value<V>) prop;
            }
        }
        return null;
    }

}
