package com.tridevmc.architecture.core.utils;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.stream.DoubleStream;

/**
 * Utility class for extreme values, the name sounds a lot cooler than the contents... :^)
 */
public class ExtremeUtils {

    /**
     * Gets the minimum and maximum values of the given list, or null if the list is empty.
     *
     * @param elements   the list to get the min and max of.
     * @param comparator the comparator to use to compare the elements.
     * @param <T>        the type of the elements in the list.
     * @return the minimum and maximum values of the given list, or null if the list is empty.
     */
    @Nullable
    public static <T> Pair<T, T> getExtremes(List<T> elements, Comparator<T> comparator) {
        if (elements.isEmpty())
            return null;
        var min = elements.get(0);
        var max = elements.get(0);
        for (T element : elements) {
            if (comparator.compare(element, min) < 0)
                min = element;
            if (comparator.compare(element, max) > 0)
                max = element;
        }
        return Pair.of(min, max);
    }

    /**
     * Gets the minimum and maximum values of the given stream, or null if the stream is empty.
     *
     * @param elements the stream to get the min and max of.
     * @return the minimum and maximum values of the given stream, or null if the stream is empty.
     */
    @Nullable
    public static DoubleDoubleImmutablePair getExtremes(DoubleStream elements) {
        final var extremes = new double[]{
                Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY
        };
        elements.forEach(element -> {
            if (Double.compare(element, extremes[0]) < 0)
                extremes[0] = element;
            if (Double.compare(element, extremes[1]) > 0)
                extremes[1] = element;
        });
        if (extremes[0] == Double.POSITIVE_INFINITY)
            return null;
        return new DoubleDoubleImmutablePair(extremes[0], extremes[1]);
    }

    /**
     * Gets the minimum and maximum values of the given array, or null if the array is empty.
     *
     * @param elements the array to get the min and max of.
     * @return the minimum and maximum values of the given array, or null if the array is empty.
     */
    @Nullable
    public static DoubleDoubleImmutablePair getExtremes(double[] elements) {
        if (elements.length == 0)
            return null;
        var min = elements[0];
        var max = elements[0];
        for (double element : elements) {
            if (Double.compare(element, min) < 0)
                min = element;
            if (Double.compare(element, max) > 0)
                max = element;
        }
        return new DoubleDoubleImmutablePair(min, max);
    }

}
