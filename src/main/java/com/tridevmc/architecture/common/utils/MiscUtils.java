/*
 * MIT License
 *
 * Copyright (c) 2017 Benjamin K
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.tridevmc.architecture.common.utils;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public class MiscUtils {

    public static Direction[] facings = Direction.values();

    public static int min(int x, int y) {
        return Math.min(x, y);
    }

    public static int max(int x, int y) {
        return Math.max(x, y);
    }

    public static double min(double x, double y) {
        return Math.min(x, y);
    }

    public static double max(double x, double y) {
        return Math.max(x, y);
    }

    public static int ifloor(double x) {
        return (int) Math.floor(x);
    }

    public static int iround(double x) {
        return (int) Math.round(x);
    }

    public static int iceil(double x) {
        return (int) Math.ceil(x);
    }

    public static Object[] arrayOf(Collection c) {
        int n = c.size();
        Object[] result = new Object[n];
        int i = 0;
        for (Object item : c)
            result[i++] = item;
        return result;
    }

    public static int packedColor(double red, double green, double blue) {
        return ((int) (red * 255) << 16) | ((int) (green * 255) << 8) | (int) (blue * 255);
    }

    public static int turnToFace(Direction local, Direction global) {
        return (turnToFaceEast(local) - turnToFaceEast(global)) & 3;
    }

    public static int turnToFaceEast(Direction f) {
        return switch (f) {
            case SOUTH -> 1;
            case WEST -> 2;
            case NORTH -> 3;
            default -> 0;
        };
    }

    /**
     * Gets the minimum and maximum values of the given list, or null if the list is empty.
     *
     * @param elements   the list to get the min and max of.
     * @param comparator the comparator to use to compare the elements.
     * @param <T>        the type of the elements in the list.
     * @return the minimum and maximum values of the given list, or null if the list is empty.
     */
    @Nullable
    public static <T> Pair<T, T> getEdges(List<T> elements, Comparator<T> comparator) {
        return getEdges(elements.stream(), comparator);
    }

    /**
     * Gets the minimum and maximum values of the given stream, or null if the stream is empty.
     *
     * @param elements   the stream to get the min and max of.
     * @param comparator the comparator to use to compare the elements.
     * @param <T>        the type of the elements in the stream.
     * @return the minimum and maximum values of the given stream, or null if the stream is empty.
     */
    @Nullable
    public static <T> Pair<T, T> getEdges(Stream<T> elements, Comparator<T> comparator) {
        List<T> sorted = elements.sorted(comparator).toList();
        if (sorted.isEmpty())
            return null;
        return Pair.of(sorted.get(0), sorted.get(sorted.size() - 1));
    }

    /**
     * Gets the minimum and maximum values of the given stream, or null if the stream is empty.
     *
     * @param elements the stream to get the min and max of.
     * @return the minimum and maximum values of the given stream, or null if the stream is empty.
     */
    @Nullable
    public static DoubleDoubleImmutablePair getEdges(DoubleStream elements) {
        var sorted = elements.sorted().toArray();
        if (sorted.length == 0)
            return null;
        return new DoubleDoubleImmutablePair(sorted[0], sorted[sorted.length - 1]);
    }

    public static MinecraftServer getMinecraftServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

}
