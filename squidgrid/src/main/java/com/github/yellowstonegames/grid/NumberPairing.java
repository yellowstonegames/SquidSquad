package com.github.yellowstonegames.grid;

/**
 * Utility functions for pairing (and tripling) functions, which take two (or three) numbers, typically {@code int}s,
 * and return an int with magnitude dependent on the input number with maximum magnitude. The results can be organized
 * into "shells" (also called "gnomons") based on the scale of the inputs; if any input is very large, the shell will
 * be very far from the origin, but if all inputs are small, the shell will be closer to the origin. All pairing
 * functions map each unique pair of valid inputs to one valid output, where valid inputs must be in a particular range.
 * This also permits an inverse to exist for any pairing function, which takes one number and produces two results.
 * Here, the inverses mutate a {@link Point2Int} or {@link Point3Int} to avoid allocating a new object every time.
 * <br>
 * The classic example of a pairing function is the Cantor pairing function, which assigns increasing results to pairs
 * of ints, with the "shell" based on the sum of the two inputs. Another, lesser-known example is the Rosenberg-Strong
 * pairing function, which has its "shell" based on the greater of the two inputs. The Cantor pairing function forms
 * right-triangle shapes for its shells, whereas the Rosenberg-Strong pairing function forms squares.
 */
public final class NumberPairing {
    /**
     * No need to instantiate.
     */
    private NumberPairing() {
    }

    /**
     * 2D <a href="https://en.wikipedia.org/wiki/Pairing_function">Cantor pairing function</a>.
     * This is a way of getting a unique int result for small enough x and y values, where "small enough" can safely be
     * considered "between 0 and 32766." This can overflow if the sum of x and y is greater than 65533, so it can't
     * reasonably deal with all int inputs.
     *
     * @param x non-negative horizontal input, between 0 and 32766, inclusive
     * @param y non-negative vertical input, between 0 and 32766, inclusive
     * @return pair of x and y as one int via the Cantor pairing function
     */
    public static int cantor(int x, int y) {
        return y + ((x + y) * (x + y + 1) >>> 1);
    }

    /**
     * Inverse of the 2D <a href="https://en.wikipedia.org/wiki/Pairing_function">Cantor pairing function</a>.
     *
     * @param p a point that will have its contents overwritten with the decoded x and y of the given distance
     * @param d pair of x and y via the Cantor function, from the origin, between 0 and 2147385344, inclusive
     * @return {@code p}, after modifications
     */
    public static Point2Int cantorInverse(Point2Int p, int d) {
        final int w = (int) (Math.sqrt(8.0 * d + 1) - 1) >>> 1;
        final int t = (w | 1) * (w + 1 >>> 1); /* triangular number with lower likelihood of overflowing */
        final int y = d - t;
        return p.set(w - y, y);
    }
    /**
     * 2D Rosenberg-Strong pairing function.
     * <a href="https://hbfs.wordpress.com/2018/08/07/moeud-deux/">See Steven Pigeon's blog</a>.
     *
     * @param x non-negative horizontal input, between 0 and 46339, inclusive
     * @param y non-negative vertical input, between 0 and 46339, inclusive
     * @return pair of x and y as one int via the Rosenberg-Strong function
     */
    public static int rosenbergStrong(final int x, final int y){
        final int m = Math.max(x, y);
        return m * m + m + y - x;
    }


    /**
     * Inverse of the 2D Rosenberg-Strong pairing function.
     * <a href="https://hbfs.wordpress.com/2018/08/07/moeud-deux/">See Steven Pigeon's blog</a>.
     *
     * @param p a point that will have its contents overwritten with the decoded x and y of the given distance
     * @param d pair of x and y via the Rosenberg-Strong function, from the origin, between 0 and 2147395599, inclusive
     * @return {@code p}, after modifications
     */
    public static Point2Int rosenbergStrongInverse(Point2Int p, int d) {
        final int g = (int) Math.sqrt(d);
        final int r = d - g * g;
        int x, y;
        if (r <= g) {
            x = g;
            y = r;
        } else {
            x = g + g - r;
            y = g;
        }
        return p.set(x, y);
    }

}
