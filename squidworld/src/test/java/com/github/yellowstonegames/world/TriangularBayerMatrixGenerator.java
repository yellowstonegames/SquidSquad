package com.github.yellowstonegames.world;

public class TriangularBayerMatrixGenerator {

    private static final int TBM_BITS = 7;
    private static final int TBM_MASK = (1 << TBM_BITS) - 1;

    /**
     * Takes two 8-bit unsigned integers index1 and index2, and returns a Morton code, with interleaved index1 and
     * index2 bits and index1 in the least significant bit. With this method, index1 and index2 can have up to 8 bits.
     * This returns a 32-bit Morton code but only uses 16 bits, and will not encode information in the sign bit.
     * Source: <a href="http://and-what-happened.blogspot.com/2011/08/fast-2d-and-3d-hilbert-curves-and.html">and-what-happened blog post</a>.
     *
     * @param x byte to interleave into the least-significant bit
     * @param y byte to interleave into the second-least-significant bit
     * @return an int that interleaves x and y into the low-order 16 bits
     */
    public static  int interleaveBytes(int x, int y) {
//        return (int)(
//                (((x & 0xFFL) * 0x0101010101010101L & 0x8040201008040201L) * 0x0102040810204081L >>> 49) & 0x5555 |
//                (((y & 0xFFL) * 0x0101010101010101L & 0x8040201008040201L) * 0x0102040810204081L >>> 48) & 0xAAAA
//        );
        x |= x << 4;
        y |= y << 4;
        x &= 0x00000f0f;
        y &= 0x00000f0f;
        x |= x << 2;
        y |= y << 2;
        x &= 0x00003333;
        y &= 0x00003333;
        x |= x << 1;
        y |= y << 1;
        x &= 0x00005555;
        y &= 0x00005555;
        return x | y << 1;
    }

    /**
     * From <a href="https://graphics.stanford.edu/~seander/bithacks.html#ReverseParallel">Bit Twiddling Hacks</a>.
     * @param v 16-bit or smaller int to reverse bits
     * @return v with its low 16 bits reversed in order
     */
    public static int reverseShortBits(int v) {
        v = ((v >>> 1) & 0x5555) | ((v & 0x5555) << 1);
        v = ((v >>> 2) & 0x3333) | ((v & 0x3333) << 2);
        v = ((v >>> 4) & 0x0F0F) | ((v & 0x0F0F) << 4);
        v = ((v >>> 8) & 0x00FF) | ((v & 0x00FF) << 8);
        return v;
    }

    public static int bayer(int x, int y) {return reverseShortBits(interleaveBytes(x ^ y, y)) >>> 16 - TBM_BITS - TBM_BITS;}

    public static void main(String[] args) {
        byte[] levelArray = new byte[1 << TBM_BITS + TBM_BITS];
        int span = 1, lastIndex = levelArray.length - 1;
        for(int i = 0, inner = 0; i < 128; i++) {
            for (int j = 0; j < span; j++) {
                levelArray[inner] = (byte)(i+128);
                levelArray[lastIndex - inner] = (byte)(127 - i);
                inner++;
            }
/*
            int boost = -1;
            for (int j = BITS, s = 0; j < 7; j++, s += 2) {
                boost &= i >>> s & i >>> s + 1;
            }
            span += (-63 + i | 63 - i) >>> 31 & boost;
*/
            span += (-63 + i | 63 - i) >>> 31;
        }
        final byte[] matrix = new byte[1 << TBM_BITS + TBM_BITS];
        for (int i = 0; i < matrix.length; i++) {
            matrix[i] = levelArray[bayer(i & TBM_MASK, i >>> TBM_BITS)];
        }

        for (int i = 0, index = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                System.out.print(matrix[index] + ", ");
                index++;
            }
            System.out.println();
        }
    }
}
