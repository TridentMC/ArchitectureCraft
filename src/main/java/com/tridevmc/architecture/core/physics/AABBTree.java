package com.tridevmc.architecture.core.physics;

import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class AABBTree<T> implements IAABBTree<T> {

    private Node theNode;

    public AABBTree(Collection<T> items, Function<T, AABB> boxGetter) {
        items.forEach(t -> {
            var box = boxGetter.apply(t);
            if (AABBTree.this.theNode == null) {
                AABBTree.this.theNode = new Node(box, t);
            } else {
                AABBTree.this.theNode.addNode(box, t);
            }
        });
    }

    public AABBTree(AABB startBox, T item) {
        this.theNode = new Node(startBox, item);
    }

    public Node getRoot() {
        return this.theNode;
    }

    public void add(AABB box, T item) {
        this.theNode.addNode(box, item);
    }

    @Override
    @NotNull
    public Stream<T> searchStream(@NotNull AABB box) {
        List<Node> queue = Lists.newArrayList(this.theNode);
        var iter = new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return !queue.isEmpty();
            }

            @Override
            public T next() {
                var node = queue.remove(0);
                if (box.intersects(node.getValue())) {
                    if (node.item == null) {
                        queue.add(node.left);
                        queue.add(node.right);
                    } else {
                        return node.item;
                    }
                }
                return null;
            }
        };
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter, Spliterator.ORDERED), false)
                .filter(Objects::nonNull);
    }

    @NotNull
    public AABB getBounds() {
        return this.theNode.getValue();
    }

    public class Node {

        private Node left;
        private Node right;
        private AABB value;
        private T item;

        public Node(AABB value, T item) {
            this.left = this.getLeft();
            this.right = this.getRight();
            this.value = value;
            this.item = item;
        }

        public boolean hasChildren() {
            return this.getLeft() != null && this.getRight() != null;
        }

        public AABB getValue() {
            return this.value;
        }

        public Node getLeft() {
            return this.left;
        }

        public Node getRight() {
            return this.right;
        }

        public List<Node> addNode(AABB box, T item) {
            AABB oldValue = this.getValue();
            this.value = this.value.union(box);
            if (!this.hasChildren()) {
                double otherVolume = this.calculateVolume(box);
                double thisVolume = this.calculateVolume(oldValue);
                if (thisVolume > otherVolume) {
                    this.left = new Node(box, item);
                    this.right = new Node(oldValue, this.item);
                } else {
                    this.left = new Node(oldValue, this.item);
                    this.right = new Node(box, item);
                }
                this.item = null;
                return Lists.newArrayList(this.getLeft(), this.getRight());
            } else {
                double leftVolume = this.getLeft().calculateVolume(box);
                double rightVolume = this.getRight().calculateVolume(box);

                if (leftVolume > rightVolume) {
                    return this.getLeft().addNode(box, item);
                } else {
                    return this.getRight().addNode(box, item);
                }
            }
        }

        private double calculateVolume(AABB other) {
            var intersection = this.getValue().union(other);
            return Math.max(0, intersection.getXSize() * intersection.getYSize() * intersection.getZSize());
        }

    }

}
