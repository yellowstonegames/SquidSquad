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
     * reasonably deal with all int inputs. This is permitted to have adjacent results "jump" from non-adjacent inputs,
     * but only when the shell changes. The shell is defined as the sum of x and y. Adjacency here permits diagonal
     * movement.
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
     * This is permitted to have adjacent results "jump" from non-adjacent inputs, but only when the shell changes.
     * The shell is defined as the greater of x and y. Adjacency here does not permit diagonal movement.
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

    /**
     * 2D Boustrophedonic ("ox-turning", or "sidewinding") Rosenberg-Strong pairing function.
     * <a href="https://hbfs.wordpress.com/2018/08/07/moeud-deux/">See Steven Pigeon's blog</a>.
     * All adjacent results will come from adjacent inputs, even when the shell changes.
     * The shell is defined as the greater of x and y. Adjacency here does not permit diagonal movement.
     *
     * @param x non-negative horizontal input, between 0 and 46339, inclusive
     * @param y non-negative vertical input, between 0 and 46339, inclusive
     * @return distance on the Boustrophedonic Rosenberg-Strong curve, from the origin
     */
    public static int windingRosenbergStrong(final int x, final int y){
        final int m = Math.max(x, y);
        final int s = -(m & 1);
        return m * m + m + (x - y + s ^ s);
    }

    /**
     * Inverse of the 2D Boustrophedonic ("ox-turning", or "sidewinding") Rosenberg-Strong pairing function.
     * <a href="https://hbfs.wordpress.com/2018/08/07/moeud-deux/">See Steven Pigeon's blog</a>.
     *
     * @param p a point that will have its contents overwritten with the decoded x and y of the given distance
     * @param d the distance on the Boustrophedonic Rosenberg-Strong curve, from the origin, between 0 and 2147395599, inclusive
     * @return {@code p}, after modifications
     */
    public static Point2Int windingRosenbergStrongInverse(Point2Int p, int d){
        final int g = (int)Math.sqrt(d);
        final int r = d - g * g;
        int i, j;
        if(r <= g){
            i = g;
            j = r;
        } else {
            i = g + g - r;
            j = g;
        }
        if((Math.max(i, j) & 1) == 1)
            p.set(i, j);
        else p.set(j, i);
        return p;
    }

    /**
     * 2D Szudzik Elegant pairing function.
     * <a href="https://en.wikipedia.org/wiki/Pairing_function#Restriction_to_natural_numbers">See Wikipedia</a>.
     * This pairing function does not guarantee that results in the same shell will be adjacent for adjacent inputs;
     * that is, it is permitted to "jump" even within a shell.
     *
     * @param x non-negative horizontal input, between 0 and 46339, inclusive
     * @param y non-negative vertical input, between 0 and 46339, inclusive
     * @return pair of x and y as one int via the Szudzik Elegant function
     */
    public static int szudzik(final int x, final int y){
        return x < y ? y * y + x : x * x + x + y;
    }


    /**
     * Inverse of the 2D Szudzik Elegant pairing function.
     * <a href="https://en.wikipedia.org/wiki/Pairing_function#Restriction_to_natural_numbers">See Wikipedia</a>.
     *
     * @param p a point that will have its contents overwritten with the decoded x and y of the given distance
     * @param d pair of x and y via the Szudzik Elegant function, from the origin, between 0 and 2147395599, inclusive
     * @return {@code p}, after modifications
     */
    public static Point2Int szudzikInverse(Point2Int p, int d) {
        final int r = (int) Math.sqrt(d); // root
        final int m = d - r * r;          // distance minus r squared
        if(m < r){
            p.set(m, r);
        } else {
            p.set(r, m - r);
        }
        return p;
    }

    /**
     * 3D Pigeon-Ettinger-Rosenberg-Strong Triple (PERS) function.
     * This gets a single integer that encodes three non-negative ints (between 0 and 1289, inclusive) into one distance
     * along a space-filling curve starting at (0,0,0). The curve is organized into cube-shaped "shells" or "gnomons,"
     * each shell being completely returned before an outer shell can be returned. That is, the distance to a point
     * inside a cube will always be less than the distance to a point that is outside that cube (if both have inputs
     * that are in range). Two different shells will connect at one of two edges, either y=0,z=0 for an even shell
     * connecting to a larger odd shell, or z=0,y=0 for an odd shell connecting to a larger even shell.
     * <br>
     * Notably, the distance will always be less than {@code Math.pow(Math.max(x, Math.max(y, z)) + 1, 3)} for inputs
     * that are in range. All adjacent results will come from adjacent inputs, even when the shell changes.
     * The shell is defined as the greater of x, y, and z. Adjacency here does not permit diagonal movement.
     * <br>
     * This is based on a boustrophedonic Rosenberg-Strong pairing function (see
     * <a href="https://hbfs.wordpress.com/2018/08/07/moeud-deux/">Steven Pigeon's blog</a> for more), but extended
     * to three dimensions. The blog post suggests using the pair of x with the pair of y and z as a triple function,
     * but that doesn't act like the 2D pairing function regarding shells.
     * <br>
     * "We could, of course, devise some complicated function for 3..." -- Steven Pigeon
     * <br>
     * "Yes! It works!" -- Tommy Ettinger
     *
     * @param x 3D coordinate, between 0 and 1289, inclusive
     * @param y 3D coordinate, between 0 and 1289, inclusive
     * @param z 3D coordinate, between 0 and 1289, inclusive
     * @return the distance along a space-filling curve from the origin to the given point.
     */
    public static int pers(final int x, final int y, final int z) {
        final int m = Math.max(x, y); // max of lateral dimensions
        final int g = Math.max(m, z); // gnomon, or shell
        final int u = g + 1;          // up one gnomon
        final int s = -(m + z & 1);   // sign, used to flip direction
        final int w = x - y + s ^ s;  // wind, used in all branches
        if ((g & 1) == 0) {
            // start at the top, wind over the top face, then snake down as z falls. Ends on x,y,z == g,0,0
            if (g == z)
                return (g * g * g) + m * m + m + w;
            return (g * g * u) + m + w + (g + g + 1) * (g - z);
        }
        // start at the bottom, snake up as z rises, then wind over the top face until we reach x,y,z == 0,0,g
        if (g == z) {
            return (u * u * u - 1) - m * m - m + w;
        }
        return (g * g * g) + m + w + (g + g + 1) * z;
    }

    /**
     * Inverse of the 3D Pigeon-Ettinger-Rosenberg-Strong (PERS) Triple function.
     * This takes a single double {@code d} for distance and a {@link Point3Int} {@code p} that will be modified
     * in-place, and sets p to hold the coordinates of the point at distance {@code Math.floor(d)} along the
     * Pigeon-Ettinger-Rosenberg-Strong space-filling curve, starting at the origin (0,0,0).
     * The curve is organized into cube-shaped "shells" or "gnomons," each shell being completely returned before an
     * outer shell can be returned. That is, the distance to a point inside a cube will always be less than the distance
     * to a point that is outside that cube (if both have inputs that are in range). Two different shells will connect
     * at one of two edges, either y=0,z=0 for an even shell connecting to a larger odd shell, or z=0,y=0 for an odd
     * shell connecting to a larger even shell.
     * <br>
     * Notably, the distance will always be less than {@code Math.pow(Math.max(x, Math.max(y, z)) + 1, 3)} for inputs
     * that are between 0 and 2146688999, inclusive.
     * <br>
     * This is based on a boustrophedonic Rosenberg-Strong pairing function (see
     * <a href="https://hbfs.wordpress.com/2018/08/07/moeud-deux/">Steven Pigeon's blog</a> for more), but extended
     * to three dimensions. The blog post suggests using the pair of x with the pair of y and z as a triple function,
     * but that doesn't act like the 2D pairing function regarding shells.
     * <br>
     * "We could, of course, devise some complicated function for 3..." -- Steven Pigeon
     * <br>
     * "Yes! It works!" -- Tommy Ettinger
     *
     * @param p a non-null Point3Int that will be modified in-place
     * @param d the distance along the space-filling curve from the origin, between 0 and 2146688999, inclusive
     * @return {@code p}, containing the point at distance {@code d} along the curve
     */
    public static Point3Int persInverse(Point3Int p, int d){
        final int g = (int)(Math.cbrt(d)); // which gnomon, or shell, we are in
        final int u = g + 1;               // up one gnomon
        final int b = g * g * g;           // base of the current gnomon
        if ((g & 1) == 0) {
            if(d - b < u * u)
            {
                // here we are on the top.
                final int t = (int)Math.sqrt(d - b); // top gnomon
                final int r = d - b - t * t;
                int i, j;
                if(r <= t){
                    i = t;
                    j = r;
                } else {
                    i = t + t - r;
                    j = t;
                }
                if((Math.max(i, j) & 1) == 1)
                    p.set(i, j, g);
                else p.set(j, i, g);
            } else {
                // winding down
                int bend = g + g + 1; // length of one bend; we will have g-many bends
                int remain = d - b - u * u; // remaining after the top has been traversed
                int ratio = remain / bend;
                int k = g - 1 - ratio;
                int i, j;
                int r = remain - ratio * bend;
                if(r <= g){
                    i = g;
                    j = r;
                } else {
                    i = g + g - r;
                    j = g;
                }
                // alternate clockwise and counterclockwise winds
                if((g + k & 1) == 1)
                    p.set(i, j, k);
                else
                    p.set(j, i, k);
            }
        } else {
            int lower = u * u * g;
            if(d >= lower){
                // finishing at the top, wind back to x,y == 0,0
                int top = u * u * u - 1 - d;
                int t = (int) Math.sqrt(top); // top gnomon
                int r = top - t * t;
                int i, j;
                if(r <= t){
                    i = t;
                    j = r;
                } else {
                    i = t + t - r;
                    j = t;
                }
                if((Math.max(i, j) & 1) == 1)
                    p.set(i, j, g);
                else p.set(j, i, g);
            } else {
                // winding up
                int bend = g + g + 1; // length of one bend; we will have g-many bends
                int remain = d - b; // remaining on the total winding section
                int k = remain / bend;
                int i, j;
                int r = remain - k * bend;
                if(r <= g){
                    i = g;
                    j = r;
                } else {
                    i = g + g - r;
                    j = g;
                }
                // alternate clockwise and counterclockwise winds
                if((g + k & 1) == 1)
                    p.set(i, j, k);
                else
                    p.set(j, i, k);
            }
        }
        return p;
    }

}
