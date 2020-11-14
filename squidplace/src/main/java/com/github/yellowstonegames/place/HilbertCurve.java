package com.github.yellowstonegames.place;

/**
 * Calculates and stores 2D and optionally 3D Hilbert Curves up to a reasonable size.
 * This can be used for various purposes, from creating a path in dungeon generation to
 * optimizing certain kinds of compression that benefit from the qualities of a Hilbert
 * Curve. A Hilbert Curve is, at its simplest, a way of passing through each point on a
 * (typically square) grid without ever crossing its own path or taking a diagonal step.
 * Hilbert Curves are self-similar, like many fractals; if you cut out a quarter of any
 * large Hilbert Curve, the part cut out will be shaped similarly to the original Curve.
 * The biggest area where Hilbert Curves find application is in applications that demand
 * nearby locations in 2D or 3D space to have nearby numerical values (here, the
 * distance traveled along the Hilbert Curve to get to that location) as often as
 * possible. A Hilbert Curve is a path, so a distance down that path is a single number,
 * but because it touches all grid points, that one number can be used to identify any
 * grid point in 2D, 3D, or higher-dimensional space. Unlike some other "space-filling
 * curves," like the more-common and easier-to-calculate Z-order curve, the Hilbert
 * Curve doesn't move diagonally or jump long distances between sequential points, so it
 * very often tends to have very close distances for very nearby 2D points.
 * @author Tommy Ettinger
 */
public class HilbertCurve {
    public static final int DEPTH = 8;
    private static final int BITS = DEPTH << 1;

    public static char[] hilbertX = new char[0x10000], hilbertY = new char[0x10000],
            hilbertDistances = new char[0x10000], mooreX = new char[0x100], mooreY = new char[0x100],
            mooreDistances = new char[0x100];
    public static char[] hilbert3X = new char[0x200], hilbert3Y = new char[0x200],
            hilbert3Z = new char[0x200], hilbert3Distances = new char[0x200];
    private static boolean initialized2D;
    private static boolean initialized3D;
    public static void init2D() {
        if(initialized2D) return;
        for (int x = 0; x < 256; x++) {
            for (int y = 0; y < 256; y++) {
                computeHilbert2D(x, y);
            }
        }

        for (int i = 64; i < 128; i++) {
            mooreX[i - 64] = hilbertX[i];
            mooreY[i - 64] = hilbertY[i];
            mooreDistances[mooreX[i - 64] + (mooreY[i - 64] << 4)] = (char)(i - 64);

            mooreX[i] = hilbertX[i];
            mooreY[i] = (char)(hilbertY[i] + 8);
            mooreDistances[mooreX[i] + (mooreY[i] << 4)] = (char)(i);

            mooreX[i + 64] = (char)(15 - hilbertX[i]);
            mooreY[i + 64] = (char)(15 - hilbertY[i]);
            mooreDistances[mooreX[i + 64] + (mooreY[i + 64] << 4)] = (char)(i + 64);

            mooreX[i + 128] = (char)(15 - hilbertX[i]);
            mooreY[i + 128] = (char)(7 - hilbertY[i]);
            mooreDistances[mooreX[i + 128] + (mooreY[i + 128] << 4)] = (char)(i + 128);
        }
        initialized2D = true;
    }

    public static void init3D() {
        if(initialized3D) return;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                for (int z = 0; z < 8; z++) {
                    computeHilbert3D(x, y, z);
                }
            }
        }
        initialized3D = true;
    }

    private HilbertCurve()
    {
    }

    /**
     * Encode a number n as a Gray code; Gray codes have a relation to the Hilbert curve and may be useful.
     * Source: http://xn--2-umb.com/15/hilbert , http://aggregate.org/MAGIC/#Gray%20Code%20Conversion
     * @param n any int
     * @return the gray code for n
     */
    public static int grayEncode(int n){
        return n ^ (n >> 1);
    }

    /**
     * Decode a number from a Gray code n; Gray codes have a relation to the Hilbert curve and may be useful.
     * Source: http://xn--2-umb.com/15/hilbert , http://aggregate.org/MAGIC/#Gray%20Code%20Conversion
     * @param n a gray code, as produced by grayEncode
     * @return the decoded int
     */
    public static int grayDecode(int n) {
        int p = n;
        while ((n >>= 1) != 0)
            p ^= n;
        return p;
    }

    /**
     * Takes an x, y position and returns the length to travel along the 256x256 Hilbert curve to reach that position.
     * This assumes x and y are between 0 and 255, inclusive.
     * This uses a lookup table for the 256x256 Hilbert Curve, which should make it faster than calculating the
     * distance along the Hilbert Curve repeatedly.
     * Source: http://and-what-happened.blogspot.com/2011/08/fast-2d-and-3d-hilbert-curves-and.html
     * @param x between 0 and 255 inclusive
     * @param y between 0 and 255 inclusive
     * @return the distance to travel along the 256x256 Hilbert Curve to get to the given x, y point.
     */
    public static int posToHilbert( final int x, final int y ) {
        //int dist = posToHilbertNoLUT(x, y);
        //return dist;
        return hilbertDistances[x + (y << 8)] & 0xffff;
    }
    /**
     * Takes an x, y, z position and returns the length to travel along the 8x8x8 Hilbert curve to reach that
     * position. This assumes x, y, and z are between 0 and 7, inclusive.
     * This uses a lookup table for the 8x8x8 Hilbert Curve, which should make it faster than calculating the
     * distance along the Hilbert Curve repeatedly.
     * Source: http://and-what-happened.blogspot.com/2011/08/fast-2d-and-3d-hilbert-curves-and.html
     * @param x between 0 and 7 inclusive
     * @param y between 0 and 7 inclusive
     * @param z between 0 and 7 inclusive
     * @return the distance to travel along the 8x8x8 Hilbert Curve to get to the given x, y, z point.
     */
    public static int posToHilbert3D( final int x, final int y, final int z ) {
        return hilbert3Distances[x | y << 3 | z << 6];
    }
    /**
     * Takes an x, y position and returns the length to travel along the 16x16 Moore curve to reach that position.
     * This assumes x and y are between 0 and 15, inclusive.
     * This uses a lookup table for the 16x16 Moore Curve, which should make it faster than calculating the
     * distance along the Moore Curve repeatedly.
     * @param x between 0 and 15 inclusive
     * @param y between 0 and 15 inclusive
     * @return the distance to travel along the 16x16 Moore Curve to get to the given x, y point.
     */
    public static int posToMoore( final int x, final int y ) {
        return mooreDistances[x + (y << 4)] & 0xff;
    }
    /*
     * Takes an x, y position and returns the length to travel along the 256x256 Hilbert curve to reach that position.
     * This assumes x and y are between 0 and 255, inclusive.
     * Source: http://and-what-happened.blogspot.com/2011/08/fast-2d-and-3d-hilbert-curves-and.html
     * @param x between 0 and 255 inclusive
     * @param y between 0 and 255 inclusive
     * @return the distance to travel along the 256x256 Hilbert Curve to get to the given x, y point.
     */

    private static int posToHilbertNoLUT( final int x, final int y )
    {
        int hilbert = 0, remap = 0xb4, mcode, hcode;
        /*
        while( block > 0 )
        {
            --block;
            mcode = ( ( x >> block ) & 1 ) | ( ( ( y >> ( block ) ) & 1 ) << 1);
            hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
            remap ^= ( 0x82000028 >> ( hcode << 3 ) );
            hilbert = ( ( hilbert << 2 ) + hcode );
        }
         */

        mcode = ( ( x >> 7 ) & 1 ) | ( ( ( y >> ( 7 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( (0) + hcode );

        mcode = ( ( x >> 6 ) & 1 ) | ( ( ( y >> ( 6 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >> 5 ) & 1 ) | ( ( ( y >> ( 5 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >> 4 ) & 1 ) | ( ( ( y >> ( 4 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >> 3 ) & 1 ) | ( ( ( y >> ( 3 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >> 2 ) & 1 ) | ( ( ( y >> ( 2 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >> 1 ) & 1 ) | ( ( ( y >> ( 1 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( x & 1 ) | ( ( y & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );

        hilbert = ( ( hilbert << 2 ) + hcode );

        return hilbert;
    }

    /**
     * Takes a position as a Morton code, with interleaved x and y bits and x in the least significant bit, and returns
     * the length to travel along the 256x256 Hilbert Curve to reach that position.
     * This uses 16 bits of the Morton code and requires that the code is non-negative.
     * Source: http://and-what-happened.blogspot.com/2011/08/fast-2d-and-3d-hilbert-curves-and.html
     * @param morton a Morton code that interleaves two 8-bit unsigned numbers, with x as index1 and y as index2.
     * @return a distance to travel down the Hilbert Curve to reach the location that can be decoded from morton.
     */
    public static int mortonToHilbert( final int morton )
    {
        int hilbert = 0;
        int remap = 0xb4;
        int block = BITS;
        while( block > 0 )
        {
            block -= 2;
            int mcode = ( ( morton >> block ) & 3 );
            int hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
            remap ^= ( 0x82000028 >> ( hcode << 3 ) );
            hilbert = ( ( hilbert << 2 ) + hcode );
        }
        return hilbert;
    }

    /**
     * Takes a distance to travel along the 256x256 Hilbert curve and returns a Morton code representing the position
     * in 2D space that corresponds to that point on the Hilbert Curve; the Morton code will have interleaved x and y
     * bits and x in the least significant bit. This uses a lookup table for the 256x256 Hilbert curve, which should
     * make it faster than calculating the position repeatedly.
     * The parameter hilbert is an int but only 16 unsigned bits are used.
     * @param hilbert a distance to travel down the Hilbert Curve
     * @return a Morton code that stores x and y interleaved; can be converted to a Coord with other methods.
     */

    public static int hilbertToMorton( final int hilbert )
    {
        return mortonEncode(hilbertX[hilbert], hilbertY[hilbert]);
    }

    /**
     * Takes a distance to travel along the 256x256 Hilbert curve and returns a Coord representing the position
     * in 2D space that corresponds to that point on the Hilbert curve. This uses a lookup table for the
     * 256x256 Hilbert curve, which should make it faster than calculating the position repeatedly.
     * The parameter hilbert is an int but only 16 unsigned bits are used.
     * @param hilbert a distance to travel down the Hilbert Curve
     * @return a Coord corresponding to the position in 2D space at the given distance down the Hilbert Curve
     */
    public static Coord hilbertToCoord( final int hilbert )
    {
        return Coord.get(hilbertX[hilbert], hilbertY[hilbert]);
    }

    /**
     * Takes a distance to travel along the 16x16 Hilbert curve and returns a Coord representing the position
     * in 2D space that corresponds to that point on the Hilbert curve. This uses a lookup table for the
     * 16x16 Hilbert curve, which should make it faster than calculating the position repeatedly.
     * The parameter moore is an int but only 8 unsigned bits are used, and since the Moore Curve loops, it is
     * calculated as {@code moore & 255}.
     * @param moore a distance to travel down the Moore Curve
     * @return a Coord corresponding to the position in 2D space at the given distance down the Hilbert Curve
     */
    public static Coord mooreToCoord( final int moore )
    {
        return Coord.get(mooreX[moore & 255], mooreY[moore & 255]);
    }


    /*
     * Takes a distance to travel along the 256x256 Hilbert curve and returns a Morton code representing the position
     * in 2D space that corresponds to that point on the Hilbert curve; the Morton code will have interleaved x and y
     * bits and x in the least significant bit. This variant does not use a lookup table, and is likely slower.
     * The parameter hilbert is an int but only 16 unsigned bits are used.
     * @param hilbert
     * @return
     */
    /*
    public static int hilbertToMortonNoLUT( final int hilbert )
    {
        int morton = 0;
        int remap = 0xb4;
        int block = BITS;
        while( block > 0 )
        {
            block -= 2;
            int hcode = ( ( hilbert >> block ) & 3 );
            int mcode = ( ( remap >> ( hcode << 1 ) ) & 3 );
            remap ^= ( 0x330000cc >> ( hcode << 3 ) );
            morton = ( ( morton << 2 ) + mcode );
        }
        return morton;
    }
    */
    /*
     * Takes a distance to travel along the 256x256 Hilbert curve and returns a Coord representing the position
     * in 2D space that corresponds to that point on the Hilbert curve. This variant does not use a lookup table,
     * and is likely slower.
     * The parameter hilbert is an int but only 16 unsigned bits are used.
     * @param hilbert
     * @return
     */
    /*
    private static Coord hilbertToCoordNoLUT( final int hilbert )
    {
        int x = 0, y = 0;
        int remap = 0xb4;
        int block = BITS;
        while( block > 0 )
        {
            block -= 2;
            int hcode = ( ( hilbert >> block ) & 3 );
            int mcode = ( ( remap >> ( hcode << 1 ) ) & 3 );
            remap ^= ( 0x330000cc >> ( hcode << 3 ) );
            x = (x << 1) + (mcode & 1);
            y = (y << 1) + ((mcode & 2) >> 1);
        }
        return Coord.get(x, y);
    }
    */
    /**
     * Takes a position as a Coord called pt and returns the length to travel along the 256x256 Hilbert curve to reach
     * that position.
     * This assumes pt.x and pt.y are between 0 and 255, inclusive.
     * Source: http://and-what-happened.blogspot.com/2011/08/fast-2d-and-3d-hilbert-curves-and.html
     * @param pt a Coord with values between 0 and 255, inclusive
     * @return a distance from the start of the 256x256 Hilbert curve to get to the position of pt
     */
    public static int coordToHilbert(final Coord pt)
    {
        return posToHilbert(pt.x, pt.y);
    }

    /**
     * Takes a position as a Coord called pt and returns the length to travel along the 16x16 Moore curve to reach
     * that position.
     * This assumes pt.x and pt.y are between 0 and 15, inclusive.
     * @param pt a Coord with values between 0 and 15, inclusive
     * @return a distance from the "start" of the 16x16 Moore curve to get to the position of pt
     */
    public static int coordToMoore(final Coord pt)
    {
        return posToMoore(pt.x, pt.y);
    }

    public static int mortonEncode3D( int index1, int index2, int index3 )
    { // pack 3 5-bit indices into a 15-bit Morton code
        index1 &= 0x0000001f;
        index2 &= 0x0000001f;
        index3 &= 0x0000001f;
        index1 *= 0x01041041;
        index2 *= 0x01041041;
        index3 *= 0x01041041;
        index1 &= 0x10204081;
        index2 &= 0x10204081;
        index3 &= 0x10204081;
        index1 *= 0x00011111;
        index2 *= 0x00011111;
        index3 *= 0x00011111;
        index1 &= 0x12490000;
        index2 &= 0x12490000;
        index3 &= 0x12490000;
        return( ( index1 >> 16 ) | ( index2 >> 15 ) | ( index3 >> 14 ) );
    }

    public static int mortonBitDecode3D( int morton )
    { // unpack 3 5-bit indices from a 15-bit Morton code
        int value1 = morton;
        int value2 = ( value1 >>> 1 );
        int value3 = ( value1 >>> 2 );
        value1 &= 0x00001249;
        value2 &= 0x00001249;
        value3 &= 0x00001249;
        value1 |= ( value1 >>> 2 );
        value2 |= ( value2 >>> 2 );
        value3 |= ( value3 >>> 2 );
        value1 &= 0x000010c3;
        value2 &= 0x000010c3;
        value3 &= 0x000010c3;
        value1 |= ( value1 >>> 4 );
        value2 |= ( value2 >>> 4 );
        value3 |= ( value3 >>> 4 );
        value1 &= 0x0000100f;
        value2 &= 0x0000100f;
        value3 &= 0x0000100f;
        value1 |= ( value1 >>> 8 );
        value2 |= ( value2 >>> 8 );
        value3 |= ( value3 >>> 8 );
        value1 &= 0x0000001f;
        value2 &= 0x0000001f;
        value3 &= 0x0000001f;
        return value1 | (value2 << 5) | (value3 << 10);
    }

    private static void computeHilbert2D(int x, int y)
    {
        int hilbert = 0, remap = 0xb4, mcode, hcode;

        mcode = ( ( x >> 7 ) & 1 ) | ( ( ( y >> ( 7 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( (0) + hcode );

        mcode = ( ( x >> 6 ) & 1 ) | ( ( ( y >> ( 6 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >> 5 ) & 1 ) | ( ( ( y >> ( 5 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >> 4 ) & 1 ) | ( ( ( y >> ( 4 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >> 3 ) & 1 ) | ( ( ( y >> ( 3 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >> 2 ) & 1 ) | ( ( ( y >> ( 2 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( ( x >> 1 ) & 1 ) | ( ( ( y >> ( 1 ) ) & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );
        remap ^= ( 0x82000028 >> ( hcode << 3 ) );
        hilbert = ( ( hilbert << 2 ) + hcode );

        mcode = ( x & 1 ) | ( ( y & 1 ) << 1);
        hcode = ( ( remap >> ( mcode << 1 ) ) & 3 );

        hilbertDistances[x | (y << 8)] = (char) ( hilbert = ( hilbert << 2 ) + hcode );
        hilbertX[hilbert] = (char)x;
        hilbertY[hilbert] = (char)y;
    }

    private static void computeHilbert3D(int x, int y, int z)
    {
        int hilbert = mortonEncode3D(x, y, z);
            int block = 6;
            int hcode = ( ( hilbert >> block ) & 7 );
            int mcode, shift, signs;
            shift = signs = 0;
            while( block > 0 )
            {
                block -= 3;
                hcode <<= 2;
                mcode = ( ( 0x20212021 >> hcode ) & 3 );
                shift = ( ( 0x48 >> ( 7 - shift - mcode ) ) & 3 );
                signs = ( ( signs | ( signs << 3 ) ) >> mcode );
                signs = ( ( signs ^ ( 0x53560300 >> hcode ) ) & 7 );
                mcode = ( ( hilbert >> block ) & 7 );
                hcode = mcode;
                hcode = ( ( ( hcode | ( hcode << 3 ) ) >> shift ) & 7 );
                hcode ^= signs;
                hilbert ^= ( ( mcode ^ hcode ) << block );
            }

        hilbert ^= ( ( hilbert >> 1 ) & 0x92492492 );
        hilbert ^= ( ( hilbert & 0x92492492 ) >> 1 );

        hilbert3X[hilbert] = (char)x;
        hilbert3Y[hilbert] = (char)y;
        hilbert3Z[hilbert] = (char)z;
        hilbert3Distances[x | y << 3 | z << 6] = (char)hilbert;
    }

    /**
     * Gets the x coordinate for a given index into the 16x16x(8*n) Moore curve. Expects indices to touch the following
     * corners of the 16x16x(8*n) cube in this order, using x,y,z syntax:
     * (0,0,0) (0,0,(8*n)) (0,16,(8*n)) (0,16,0) (16,16,0) (16,16,(8*n)) (16,0,(8*n)) (16,0,0)
     * @param index the index into the 3D 16x16x(8*n) Moore Curve, must be less than 0x1000
     * @param n the number of 8-deep layers to use as part of the box shape this travels through
     * @return the x coordinate of the given distance traveled through the 3D 16x16x(8*n) Moore Curve
     */
    public static int getXMoore3D(final int index, final int n) {
        int hilbert = index & 0x1ff;
        int sector = index >> 9;
        if (sector < 2 * n)
            return 7 - hilbert3X[hilbert];
        else
            return 8 + hilbert3X[hilbert];
    }

    /**
     * Gets the y coordinate for a given index into the 16x16x(8*n) Moore curve. Expects indices to touch the following
     * corners of the 16x16x(8*n) cube in this order, using x,y,z syntax:
     * (0,0,0) (0,0,(8*n)) (0,16,(8*n)) (0,16,0) (16,16,0) (16,16,(8*n)) (16,0,(8*n)) (16,0,0)
     * @param index the index into the 3D 16x16x(8*n) Moore Curve, must be less than 0x1000
     * @param n the number of 8-deep layers to use as part of the box shape this travels through
     * @return the y coordinate of the given distance traveled through the 3D 16x16x(8*n) Moore Curve
     */
    public static int getYMoore3D(final int index, final int n)
    {
        int hilbert = index & 0x1ff;
        int sector = index >> 9;
        if (sector < n || sector >= 3 * n)
            return 7 - hilbert3Y[hilbert];
        else
            return 8 + hilbert3Y[hilbert];

    }
    /**
     * Gets the z coordinate for a given index into the 16x16x(8*n) Moore curve. Expects indices to touch the following
     * corners of the 16x16x(8*n) cube in this order, using x,y,z syntax:
     * (0,0,0) (0,0,(8*n)) (0,16,(8*n)) (0,16,0) (16,16,0) (16,16,(8*n)) (16,0,(8*n)) (16,0,0)
     * @param index the index into the 3D 16x16x(8*n) Moore Curve, must be less than 0x1000
     * @param n the number of 8-deep layers to use as part of the box shape this travels through
     * @return the z coordinate of the given distance traveled through the 3D 16x16x(8*n) Moore Curve
     */
    public static int getZMoore3D(final int index, final int n) {
        int hilbert = index & 0x1ff;
        int sector = index >> 9;
        if (((sector / n) & 1) == 0)
            return hilbert3Z[hilbert] + ((sector % n) << 3);
        else
            return (n << 3) - 1 - hilbert3Z[hilbert] - ((sector % n) << 3);
    }



    /**
     * Takes two 8-bit unsigned integers index1 and index2, and returns a Morton code, with interleaved index1 and
     * index2 bits and index1 in the least significant bit. With this method, index1 and index2 can have up to 8 bits.
     * This returns a 16-bit Morton code and WILL encode information in the sign bit if the inputs are large enough.
     * Source: http://and-what-happened.blogspot.com/2011/08/fast-2d-and-3d-hilbert-curves-and.html
     * @param index1 a non-negative integer using at most 8 bits, to be placed in the "x" slots
     * @param index2 a non-negative integer using at most 8 bits, to be placed in the "y" slots
     * @return a Morton code/Z-Code that interleaves the two numbers into one 16-bit char
     */
    public static char zEncode(int index1, int index2)
    { // pack 2 8-bit (unsigned) indices into a 16-bit (signed...) Morton code/Z-Code
        index1 &= 0x000000ff;
        index2 &= 0x000000ff;
        index1 |= ( index1 << 4 );
        index2 |= ( index2 << 4 );
        index1 &= 0x00000f0f;
        index2 &= 0x00000f0f;
        index1 |= ( index1 << 2 );
        index2 |= ( index2 << 2 );
        index1 &= 0x00003333;
        index2 &= 0x00003333;
        index1 |= ( index1 << 1 );
        index2 |= ( index2 << 1 );
        index1 &= 0x00005555;
        index2 &= 0x00005555;
        return (char)(index1 | ( index2 << 1 ));
    }
    /**
     * Takes two 8-bit unsigned integers index1 and index2, and returns a Morton code, with interleaved index1 and
     * index2 bits and index1 in the least significant bit. With this method, index1 and index2 can have up to 8 bits.
     * This returns a 32-bit Morton code but only uses 16 bits, and will not encode information in the sign bit.
     * Source: http://and-what-happened.blogspot.com/2011/08/fast-2d-and-3d-hilbert-curves-and.html
     * @param index1 a non-negative integer using at most 8 bits, to be placed in the "x" slots
     * @param index2 a non-negative integer using at most 8 bits, to be placed in the "y" slots
     * @return a Morton code that interleaves the two numbers as one 32-bit int, but only in 16 bits of it
     */
    public static int mortonEncode(int index1, int index2)
    { // pack 2 8-bit (unsigned) indices into a 32-bit (signed...) Morton code
        index1 &= 0x000000ff;
        index2 &= 0x000000ff;
        index1 |= ( index1 << 4 );
        index2 |= ( index2 << 4 );
        index1 &= 0x00000f0f;
        index2 &= 0x00000f0f;
        index1 |= ( index1 << 2 );
        index2 |= ( index2 << 2 );
        index1 &= 0x00003333;
        index2 &= 0x00003333;
        index1 |= ( index1 << 1 );
        index2 |= ( index2 << 1 );
        index1 &= 0x00005555;
        index2 &= 0x00005555;
        return index1 | ( index2 << 1 );
    }

    /**
     * Takes a Morton code, with interleaved x and y bits and x in the least significant bit, and returns the Coord
     * representing the same x, y position.
     * This uses 16 bits of the Morton code and requires that the code is non-negative.
     * Source: http://and-what-happened.blogspot.com/2011/08/fast-2d-and-3d-hilbert-curves-and.html
     * @param morton an int containing two interleaved numbers, from 0 to 255 each
     * @return a Coord matching the x and y extracted from the Morton code
     */
    public static Coord mortonDecode( final int morton )
    { // unpack 2 8-bit (unsigned) indices from a 32-bit (signed...) Morton code
        int value1 = morton;
        int value2 = ( value1 >> 1 );
        value1 &= 0x5555;
        value2 &= 0x5555;
        value1 |= ( value1 >> 1 );
        value2 |= ( value2 >> 1 );
        value1 &= 0x3333;
        value2 &= 0x3333;
        value1 |= ( value1 >> 2 );
        value2 |= ( value2 >> 2 );
        value1 &= 0x0f0f;
        value2 &= 0x0f0f;
        value1 |= ( value1 >> 4 );
        value2 |= ( value2 >> 4 );
        value1 &= 0x00ff;
        value2 &= 0x00ff;
        return Coord.get(value1, value2);
    }

    /**
     * Takes a Morton code, with interleaved x and y bits and x in the least significant bit, and returns the Coord
     * representing the same x, y position.
     * This takes a a 16-bit Z-Code with data in the sign bit, as returned by zEncode().
     * Source: http://and-what-happened.blogspot.com/2011/08/fast-2d-and-3d-hilbert-curves-and.html
     * @param morton an int containing two interleaved numbers, from 0 to 255 each
     * @return a Coord matching the x and y extracted from the Morton code
     */
    public static Coord zDecode( final int morton )
    { // unpack 2 8-bit (unsigned) indices from a 32-bit (signed...) Morton code
        int value1 = morton & 0xffff;
        int value2 = ( value1 >> 1 );
        value1 &= 0x5555;
        value2 &= 0x5555;
        value1 |= ( value1 >> 1 );
        value2 |= ( value2 >> 1 );
        value1 &= 0x3333;
        value2 &= 0x3333;
        value1 |= ( value1 >> 2 );
        value2 |= ( value2 >> 2 );
        value1 &= 0x0f0f;
        value2 &= 0x0f0f;
        value1 |= ( value1 >> 4 );
        value2 |= ( value2 >> 4 );
        value1 &= 0x00ff;
        value2 &= 0x00ff;
        return Coord.get(value1, value2);
    }
}
