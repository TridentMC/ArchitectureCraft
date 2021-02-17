package com.tridevmc.architecture.common.utils;

import com.google.common.collect.Lists;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class AABBTree<T> {

    private class Node {
        private Node left;
        private Node right;
        private AxisAlignedBB value;
        private T item;

        public Node(AxisAlignedBB value, T item) {
            this.left = this.getLeft();
            this.right = this.getRight();
            this.value = value;
            this.item = item;
        }

        public boolean hasChildren() {
            return this.getLeft() != null && this.getRight() != null;
        }

        public AxisAlignedBB getValue() {
            return this.value;
        }

        public Node getLeft() {
            return this.left;
        }

        public Node getRight() {
            return this.right;
        }

        public List<Node> addNode(AxisAlignedBB box, T item) {
            AxisAlignedBB oldValue = this.getValue();
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

        private double calculateVolume(AxisAlignedBB other) {
            AxisAlignedBB intersection = this.getValue().intersect(other);
            return Math.max(0, intersection.getXSize() * intersection.getYSize() * intersection.getZSize());
        }
    }

    private Node theNode;

    public AABBTree(Collection<T> items, Function<T, AxisAlignedBB> boxGetter){
        items.forEach(t -> {
            AxisAlignedBB box = boxGetter.apply(t);
            if(AABBTree.this.theNode == null){
                AABBTree.this.theNode = new Node(box, t);
            } else {
                AABBTree.this.theNode.addNode(box, t);
            }
        });
    }

    public AABBTree(AxisAlignedBB startBox, T item) {
        this.theNode = new Node(startBox, item);
    }

    public void add(AxisAlignedBB box, T item) {
        this.theNode.addNode(box, item);
    }

    public List<T> search(AxisAlignedBB box) {
        List<T> out = Lists.newArrayList();
        List<Node> queue = Lists.newArrayList(this.theNode);

        while (!queue.isEmpty()) {
            Node node = queue.remove(0);
            if (node.value.intersects(box)) {
                if (node.item == null) {
                    queue.add(node.left);
                    queue.add(node.right);
                } else {
                    out.add(node.item);
                }
            }
        }
        return out;
    }

}
