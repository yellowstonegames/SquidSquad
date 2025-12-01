/*
 * Copyright (c) 2020-2024 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.yellowstonegames.grid;

import com.github.tommyettinger.digital.ArrayTools;

/**
 * Tools for constructing patterns using box-drawing characters in grids.
 * This can be useful in both text-based games and graphical ones, where graphical games would use a single box-drawing
 * character, like any of {@code "╴╵┘╶─└┴╷┐│┤┌┬├┼"} to determine which wall graphic to draw at a position. The methods
 * {@link #hashesToLines(char[][])} and {@link #pruneLines(char[][], Region, char[][])} are especially commonly-used.
 * The first creates a map that uses box-drawing characters from a "bare" map that represents walls with {@code '#'};
 * there is a variant that fills an existing 2D char array with the box-drawing character map (instead of creating a new
 * 2D array). The second is meant to be used in tandem with {@link FOV} to limit the set of small lines in each
 * box-drawing character to those matching walls a viewer can see. It may be better to use {@link VisionFramework} for
 * handling vision and lighting, because it calls pruneLines() automatically and keeps its parameters up-to-date.
 * <br>
 * The rest of this class is some sort of mad science experiment, meant for procedurally generating decorations in some
 * area (like a circle, square, diamond, shield-shape...). Methods like {@link #encode4x4(char[][])},
 * {@link #decode4x4(long)}, {@link #rotateClockwise(long)}, {@link #flipHorizontal4x4(long)}, and so on are probably
 * not widely useful, but if you find a good use for them (or have a feature request to make them better), an issue on
 * the SquidSquad repo would be a good place to mention it.
 */
public final class LineTools {
    /**
     * No need to instantiate.
     */
    private LineTools(){
    }
    public static final char[] lightA0  = "#╴╵┘╶─└┴╷┐│┤┌┬├┼".toCharArray(),
                               lightAlt = "#╴╷┐╶─┌┬╵┘│┤└┴├┼".toCharArray(),
                               heavyA0  = "#╸╹┛╺━┗┻╻┓┃┫┏┳┣╋".toCharArray(),
                               heavyAlt = "#╸╻┓╺━┏┳╹┛┃┫┗┻┣╋".toCharArray(),
                               light0   = "#─│┘──└┴│┐│┤┌┬├┼".toCharArray(),
                               light    = "#─│┐──┌┬│┘│┤└┴├┼".toCharArray(),
                               heavy0   = "#━┃┛━━┗┻┃┓┃┫┏┳┣╋".toCharArray(),
                               heavy    = "#━┃┓━━┏┳┃┛┃┫┗┻┣╋".toCharArray();
    //                                     0123456789ABCDEF

    /**
     * A constant that represents the encoded pattern for a 4x4 square with all lines possible except those that
     * would extend to touch cells adjacent to the 4x4 area. Meant to restrict cells within the square area by using
     * bitwise AND with an existing encoded pattern as another long, as with {@code LineTools.interiorSquare & encoded}.
     * If you limit the area to the square with this, you may sometimes want to add a border, and for that you can use
     * {@link #exteriorSquare} and bitwise OR that with the restricted area.
     * <br>This looks like:
     * <pre>
     * "┌┬┬┐"
     * "├┼┼┤"
     * "├┼┼┤"
     * "└┴┴┘"
     * </pre>
     * }
     */
    public final static long interiorSquare = 0x3776BFFEBFFE9DDCL,
    /**
     * A constant that represents the encoded pattern for a 4x4 square with only the lines along the border. Meant to
     * either restrict cells to the border by using bitwise AND with an existing encoded pattern as another long, as
     * with {@code LineTools.exteriorSquare & encoded}, or to add a border to an existing pattern with bitwise OR, as with
     * {@code LineTools.exteriorSquare | encoded}.
     * <br>This looks like: 
     * <pre>
     * "┌──┐"
     * "│  │"
     * "│  │"
     * "└──┘"
     * </pre>
     */
    exteriorSquare = 0x3556A00AA00A955CL,
    /**
     * A constant that represents the encoded pattern for a 4x4 plus pattern with only the lines along the border. This
     * pattern has no lines in the corners of the 4x4 area, but has some lines in all other cells, though none that
     * would touch cells adjacent to this 4x4 area. Meant to restrict cells to the border by using bitwise AND with an
     * existing encoded pattern as another long, as with {@code LineTools.interiorPlus & encoded}.
     * <br>This looks like: 
     * <pre>
     * " ┌┐ "
     * "┌┼┼┐"
     * "└┼┼┘"
     * " └┘ "
     * </pre>
     */
    interiorPlus = 0x03603FF69FFC09C0L,
    /**
     * A constant that represents the encoded pattern for a 4x4 plus pattern with only the lines along the border. This
     * pattern has no lines in the corners of the 4x4 area, but has some lines in all other cells, though none that
     * would touch cells adjacent to this 4x4 area. Meant to either restrict cells to the border by using bitwise AND
     * with an existing encoded pattern as another long, as with {@code LineTools.exteriorPlus & encoded}, or to add a
     * border to an existing pattern with bitwise OR, as with {@code LineTools.exteriorPlus | encoded}.
     * <br>This looks like: 
     * <pre>
     * " ┌┐ "
     * "┌┘└┐"
     * "└┐┌┘"
     * " └┘ "
     * </pre>
     */
    exteriorPlus = 0x03603C96963C09C0L,
    /**
     * A constant that represents the encoded pattern for the upper left 4x4 area of an 8x8 square. No lines will touch
     * the upper or left borders, but they do extend into the lower and right borders. This is expected to be flipped
     * using {@link #flipHorizontal4x4(long)} and/or {@link #flipVertical4x4(long)} to make the other corners. If middle
     * pieces are wanted that touch everything but the upper border, you can use
     * {@code (LineTools.interiorSquareLarge | LineTools.flipHorizontal4x4(LineTools.interiorSquareLarge))}. If you want it to
     * touch everything but the left border, you can use
     * {@code (LineTools.interiorSquareLarge | LineTools.flipVertical4x4(LineTools.interiorSquareLarge))}.
     * <br>This looks like: 
     * <pre>
     * "┌┬┬┬"
     * "├┼┼┼"
     * "├┼┼┼"
     * "├┼┼┼"
     * </pre>
     * @see #interiorSquare The docs here cover how to use this as a mask with bitwise AND.
     */
    interiorSquareLarge = 0xFFFEFFFEFFFEDDDCL,
    /**
     * A constant that represents the encoded pattern for the upper left 4x4 area of an 8x8 square border. No lines will
     * touch the upper or left borders, but they do extend into the lower and right borders. The entirety of this
     * pattern is one right-angle. This is expected to be flipped using {@link #flipHorizontal4x4(long)} and/or
     * {@link #flipVertical4x4(long)} to make the other corners.
     * <br>This looks like: 
     * <pre>
     * "┌───"
     * "│   "
     * "│   "
     * "│   "
     * </pre>
     * @see #exteriorSquare The docs here cover how to use this as a mask with bitwise AND or to insert it with OR.
     */
    exteriorSquareLarge = 0x000A000A000A555CL,
    /**
     * A constant that represents the encoded pattern for the upper left 4x4 area of a 6x6 square centered in an 8x8
     * space. A 3x3 square will be filled of the 4x4 area this represents. No lines will touch the upper or left
     * borders, but they do extend into the lower and right borders. This is expected to be flipped using
     * {@link #flipHorizontal4x4(long)} and/or {@link #flipVertical4x4(long)} to make other corners.
     * <br>This looks like: 
     * <pre>
     * "    "
     * " ┌┬┬"
     * " ├┼┼"
     * " ├┼┼"
     * </pre>
     * @see #interiorSquare The docs here cover how to use this as a mask with bitwise AND.
     * 
     */
    shallowInteriorSquareLarge = 0xFFE0FFE0DDC00000L,
    /**
     * A constant that represents the encoded pattern for the upper left 4x4 area of a 6x6 square border centered in an
     * 8x8 space. This consists of a 3-cell-long vertical line and a 3-cell-long horizontal line. No lines will touch
     * the upper or left borders, but they do extend into the lower and right borders. The entirety of this
     * pattern is one right-angle. This is expected to be flipped using {@link #flipHorizontal4x4(long)} and/or
     * {@link #flipVertical4x4(long)} to make the other corners.
     * <br>This looks like: 
     * <pre>
     * "    "
     * " ┌──"
     * " │  "
     * " │  "
     * </pre>
     * @see #exteriorSquare The docs here cover how to use this as a mask with bitwise AND or to insert it with OR.
     */
    shallowExteriorSquareLarge = 0x00A000A055C00000L,
    /**
     * A constant that represents the encoded pattern for the upper left 4x4 area of a 4x4 square centered in an 8x8
     * space. A 2x2 square will be filled of the 4x4 area this represents. No lines will touch the upper or left
     * borders, but they do extend into the lower and right borders. This is expected to be flipped using
     * {@link #flipHorizontal4x4(long)} and/or {@link #flipVertical4x4(long)} to make other corners.
     * <br>This looks like: 
     * <pre>
     * "    "
     * "    "
     * "  ┌┬"
     * "  ├┼"
     * </pre>
     * @see #interiorSquare The docs here cover how to use this as a mask with bitwise AND.
     */
    shallowerInteriorSquareLarge = 0xFE00DC0000000000L,
    /**
     * A constant that represents the encoded pattern for the upper left 4x4 area of a 4x4 square border centered in an
     * 8x8 space. This consists of a 2-cell-long vertical line and a 2-cell-long horizontal line. No lines will touch
     * the upper or left borders, but they do extend into the lower and right borders. The entirety of this
     * pattern is one right-angle. This is expected to be flipped using {@link #flipHorizontal4x4(long)} and/or
     * {@link #flipVertical4x4(long)} to make the other corners.
     * <br>This looks like: 
     * <pre>
     * "    "
     * "    "
     * "  ┌─"
     * "  │ "
     * </pre>
     * @see #exteriorSquare The docs here cover how to use this as a mask with bitwise AND or to insert it with OR.
     */
    shallowerExteriorSquareLarge = 0x0A005C0000000000L,
    /**
     * A constant that represents the encoded pattern for the upper left 4x4 area of an 8x8 plus shape. No lines will
     * touch the upper or left borders, but they do extend into the lower and right borders. This pattern leaves the
     * upper left 2x2 area blank, and touches all of the lower and right borders. This is expected to be flipped using
     * {@link #flipHorizontal4x4(long)} and/or {@link #flipVertical4x4(long)} to make other corners.
     * @see #interiorPlus The docs here cover how to use this as a mask with bitwise AND.
     * <br>This looks like: 
     * <pre>
     * "  ┌┬"
     * "  ├┼"
     * "┌┬┼┼"
     * "├┼┼┼"
     * </pre>
     */
    interiorPlusLarge = 0xFFFEFFDCFE00DC00L,
    /**
     * A constant that represents the encoded pattern for the upper left 4x4 area of an 8x8 plus shape border. No lines
     * will touch the upper or left borders, but they do extend into the lower and right borders. This pattern leaves
     * the upper left 2x2 area blank, as well as all but one each of the bottom and right border cells. This is expected
     * to be flipped using {@link #flipHorizontal4x4(long)} and/or {@link #flipVertical4x4(long)} to make other corners.
     * <br>This looks like: 
     * <pre>
     * "  ┌─"
     * "  │ "
     * "┌─┘ "
     * "│   "
     * </pre>
     * @see #exteriorPlus The docs here cover how to use this as a mask with bitwise AND or to insert it with OR.
     */
    exteriorPlusLarge = 0x000A035C0A005C00L,
    /**
     * A constant that represents the encoded pattern for the upper left 4x4 area of an 8x8 circle shape. No lines will
     * touch the upper or left borders, but they do extend into the lower and right borders. This is expected to be
     * flipped using {@link #flipHorizontal4x4(long)} and/or {@link #flipVertical4x4(long)} to make other corners.
     * <br>This looks like: 
     * <pre>
     * "  ┌┬"
     * " ┌┼┼"
     * "┌┼┼┼"
     * "├┼┼┼"
     * </pre>
     * @see #interiorPlus The docs here cover how to use this as a mask with bitwise AND.
     */
    interiorCircleLarge = 0xFFFEFFFCFFC0DC00L,
    /**
     * A constant that represents the encoded pattern for the upper left 4x4 area of an 8x8 circular border. No lines
     * will touch the upper or left borders, but they do extend into the lower and right borders. The entirety of this
     * pattern is one curving line. This is expected to be flipped using {@link #flipHorizontal4x4(long)} and/or
     * {@link #flipVertical4x4(long)} to make other corners.
     * <br>This looks like: 
     * <pre>
     * "  ┌─"
     * " ┌┘ "
     * "┌┘  "
     * "│   "
     * </pre>
     * @see #exteriorPlus The docs here cover how to use this as a mask with bitwise AND or to insert it with OR.
     */
    exteriorCircleLarge = 0x000A003C03C05C00L,
    /**
     * A constant that represents the encoded pattern for the upper left 4x4 area of an 8x8 diamond shape. No lines will
     * touch the upper or left borders, but they do extend into the lower and right borders. This pattern leaves the
     * upper left 2x2 area blank, and touches all of the lower and right borders. This is expected to be flipped using
     * {@link #flipHorizontal4x4(long)} and/or {@link #flipVertical4x4(long)} to make other corners. This has more of a
     * fine angle than {@link #interiorPlusLarge}, which is otherwise similar.
     * <br>This looks like: 
     * <pre>
     * "   ┌"
     * "  ┌┼"
     * " ┌┼┼"
     * "┌┼┼┼"
     * </pre>
     * @see #interiorPlus The docs here cover how to use this as a mask with bitwise AND.
     */
    interiorDiamondLarge = 0xFFFCFFC0FC00C000L,
    /**
     * A constant that represents the encoded pattern for the upper left 4x4 area of an 8x8 diamond shape border. No
     * lines will touch the upper or left borders, but they do extend into the lower and right borders. This pattern
     * leaves the upper left 2x2 area blank, as well as all but one each of the bottom and right border cells. This is
     * expected to be flipped using {@link #flipHorizontal4x4(long)} and/or {@link #flipVertical4x4(long)} to make other
     * corners. This has more of a fine angle than {@link #exteriorPlusLarge}, which is otherwise similar.
     * <br>This looks like: 
     * <pre>
     * "   ┌"
     * "  ┌┘"
     * " ┌┘ "
     * "┌┘  "
     * </pre>
     * @see #exteriorPlus The docs here cover how to use this as a mask with bitwise AND or to insert it with OR.
     */
    exteriorDiamondLarge = 0x003C03C03C00C000L;
    private static final char[] wallLookup = new char[]
            {
                    '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '┴', '┐', '┤', '┬', '┼',
                    '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '┴', '┐', '┤', '┬', '┼',
                    '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '┴', '┐', '┤', '┬', '┼',
                    '#', '│', '─', '└', '│', '│', '┌', '│', '─', '┘', '─', '┴', '┐', '┤', '┬', '┤',
                    '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '┴', '┐', '┤', '┬', '┼',
                    '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '┴', '┐', '┤', '┬', '┼',
                    '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '┴', '┐', '┤', '─', '┴',
                    '#', '│', '─', '└', '│', '│', '┌', '│', '─', '┘', '─', '┴', '┐', '┤', '─', '┘',
                    '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '┴', '┐', '┤', '┬', '┼',
                    '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '─', '┐', '┤', '┬', '┬',
                    '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '┴', '┐', '┤', '┬', '┼',
                    '#', '│', '─', '└', '│', '│', '┌', '│', '─', '┘', '─', '─', '┐', '┤', '┬', '┐',
                    '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '┴', '┐', '│', '┬', '├',
                    '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '─', '┐', '│', '┬', '┌',
                    '#', '│', '─', '└', '│', '│', '┌', '├', '─', '┘', '─', '┴', '┐', '│', '─', '└',
                    '#', '│', '─', '└', '│', '│', '┌', '│', '─', '┘', '─', '─', '┐', '│', '─', ' '
            };

    /**
     * Produces a 4x4 2D char array by interpreting the bits of the given long as line information. Uses the box drawing
     * chars from {@link #light}, which are compatible with most fonts.
     * @param encoded a long, which can be random, that encodes some pattern of (typically box drawing) characters
     * @return a 4x4 2D char array containing elements from symbols assigned based on encoded
     */
    public static char[][] decode4x4(long encoded)
    {
        return decode4x4(encoded, light);
    }
    /**
     * Produces a 4x4 2D char array by interpreting the bits of the given long as line information. Uses the given char
     * array, which must have at least 16 elements and is usually one of {@link #light}, {@link #heavy},
     * {@link #lightAlt}, or {@link #heavyAlt}, with the last two usable only if using a font that supports the chars
     * {@code ╴╵╶╷} (this is true for Iosevka and Source Code Pro, for instance, but not Inconsolata or GoMono).
     * @param encoded a long, which can be random, that encodes some pattern of (typically box drawing) characters
     * @param symbols a 16-element-or-larger char array; usually a constant in this class like {@link #light}
     * @return a 4x4 2D char array containing elements from symbols assigned based on encoded
     */
    public static char[][] decode4x4(long encoded, char[] symbols)
    {
        char[][] v = new char[4][4];
        for (int i = 0; i < 16; i++) {
            v[i & 3][i >> 2] = symbols[(int) (encoded >>> (i << 2) & 15L)];
        }
        return v;
    }
    /**
     * Fills a 4x4 area of the given 2D char array {@code into} by interpreting the bits of the given long as line
     * information. Uses the given char array {@code symbols}, which must have at least 16 elements and is usually one
     * of {@link #light}, {@link #heavy}, {@link #lightAlt}, or {@link #heavyAlt}, with the last two usable only if
     * using a font that supports the chars {@code ╴╵╶╷} (this is true for Iosevka and Source Code Pro, for instance,
     * but not Inconsolata or GoMono).
     * @param encoded a long, which can be random, that encodes some pattern of (typically box drawing) characters
     * @param symbols a 16-element-or-larger char array; usually a constant in this class like {@link #light}
     * @param into a 2D char array that will be modified in a 4x4 area
     * @param startX the first x position to modify in into
     * @param startY the first y position to modify in into
     * @return into, after modification
     */
    public static char[][] decodeInto4x4(long encoded, char[] symbols, char[][] into, int startX, int startY)
    {
        for (int i = 0; i < 16; i++) {
            into[(i & 3) + startX][(i >> 2) + startY] = symbols[(int) (encoded >>> (i << 2) & 15L)];
        }
        return into;
    }

    /**
     * Reads a 2D char array {@code decoded}, which must be at least 4x4 in size, and returns a long that encodes the cells from 0,0 to
     * 3,3 in a way that this class can interpret and manipulate. The 2D array {@code decoded} must contain box drawing
     * symbols, which can be any of those from {@link #light}, {@link #heavy}, {@link #lightAlt}, or {@link #heavyAlt}.
     * Valid chars are {@code ╴╵┘╶─└┴╷┐│┤┌┬├┼╸╹┛╺━┗┻╻┓┃┫┏┳┣╋}; any other chars will be treated as empty space.
     * @param decoded a 2D char array that must be at least 4x4 and should usually contain box drawing characters
     * @return a long that encodes the box drawing information in decoded so this class can manipulate it
     */
    public static long encode4x4(char[][] decoded)
    {
        long v = 0L;
        for (int i = 0; i < 16; i++) {
            switch (decoded[i & 3][i >> 2])
            {
                // ╴╵┘╶─└┴╷┐│┤┌┬├┼
                // ╸╹┛╺━┗┻╻┓┃┫┏┳┣╋
                //0123456789ABCDEF
                case '─':
                case '━':
                    v |= 5L << (i << 2);
                    break;
                case '│':
                case '┃':
                    v |= 10L << (i << 2);
                    break;
                case '┘':
                case '┛':
                    v |= 3L << (i << 2);
                    break;
                case '└':
                case '┗':
                    v |= 6L << (i << 2);
                    break;
                case '┐':
                case '┓':
                    v |= 9L << (i << 2);
                    break;
                case '┌':
                case '┏':
                    v |= 12L << (i << 2);
                    break;
                case '┴':
                case '┻':
                    v |= 7L << (i << 2);
                    break;
                case '┤':
                case '┫':
                    v |= 11L << (i << 2);
                    break;
                case '┬':
                case '┳':
                    v |= 13L << (i << 2);
                    break;
                case '├':
                case '┣':
                    v |= 14L << (i << 2);
                    break;
                case '┼':
                case '╋':
                    v |= 15L << (i << 2);
                    break;
                case '╴':
                case '╸':
                    v |= 1L << (i << 2);
                    break;
                case '╵':
                case '╹':
                    v |= 2L << (i << 2);
                    break;
                case '╶':
                case '╺':
                    v |= 4L << (i << 2);
                    break;
                case '╷':
                case '╻':
                    v |= 8L << (i << 2);
                    break;
            }
        }
        return v;
    }

    /**
     * Makes a variant on the given encoded 4x4 pattern so the left side is flipped to the right side and vice versa.
     * @param encoded an encoded pattern long that represents a 4x4 area
     * @return a different encoded pattern long that represents the argument flipped left-to-right
     */
    public static long flipHorizontal4x4(long encoded)
    {
        long v = 0L;
        for (int i = 0, i4 = 0; i < 16; i++, i4 += 4) {
            v |= ((encoded >>> i4 & 10L) | ((encoded >>> i4 & 1) << 2L) | ((encoded >>> i4 & 4L) >>> 2)) << (i + 3 - ((i & 3) << 1) << 2);
        }
        return v;
    }
    /**
     * Makes a variant on the given encoded 4x4 pattern so the top side is flipped to the bottom side and vice versa.
     * @param encoded an encoded pattern long that represents a 4x4 area
     * @return a different encoded pattern long that represents the argument flipped top-to-bottom
     */
    public static long flipVertical4x4(long encoded)
    {
        long v = 0L;
        for (int i = 0, i4 = 0; i < 16; i++, i4 += 4) {
            v |= ((encoded >>> i4 & 5L) | ((encoded >>> i4 & 2L) << 2) | ((encoded >>> i4 & 8L) >>> 2)) << (i + 12 - ((i >> 2) << 3) << 2);
        }
        return v;
    }

    /**
     * Makes a variant on the given encoded 4x4 pattern so the x and y axes are interchanged, making the top side become
     * the left side and vice versa, while the bottom side becomes the right side and vice versa.
     * @param encoded an encoded pattern long that represents a 4x4 area
     * @return a different encoded pattern long that represents the argument transposed top-to-left and bottom-to-right
     */
    public static long transpose4x4(long encoded)
    {
        long v = 0L;
        for (int i4 = 0; i4 < 64; i4 += 4) {
            v |= (((encoded >>> i4 & 5L) << 1) | ((encoded >>> i4 & 10L) >>> 1)) << ((i4 >>> 2 & 12L) | ((i4 & 12L) << 2));
        }
        return v;
    }
    /**
     * Makes a variant on the given encoded 4x4 pattern so the lines are rotated 90 degrees clockwise, changing their
     * positions as well as what chars they will decode to. This can be called twice to get a 180 degree rotation, but
     * {@link #rotateCounterclockwise(long)} should be used for a 270 degree rotation.
     * @param encoded an encoded pattern long that represents a 4x4 area
     * @return a different encoded pattern long that represents the argument rotated 90 degrees clockwise
     */
    public static long rotateClockwise(long encoded)
    {
        // this is functionally equivalent to, but faster than, the following:
        // return flipHorizontal4x4(transpose4x4(encoded));
        long v = 0L;
        for (int i4 = 0; i4 < 64; i4 += 4) {
            v |= (((encoded >>> i4 & 7L) << 1) | ((encoded >>> i4 & 8L) >>> 3)) << ((~i4 >>> 2 & 12L) | ((i4 & 12L) << 2));
        }
        return v;
    }
    /**
     * Makes a variant on the given encoded 4x4 pattern so the lines are rotated 90 degrees counterclockwise, changing
     * their positions as well as what chars they will decode to. This can be called twice to get a 180 degree rotation,
     * but {@link #rotateClockwise(long)} should be used for a 270 degree rotation.
     * @param encoded an encoded pattern long that represents a 4x4 area
     * @return a different encoded pattern long that represents the argument rotated 90 degrees counterclockwise
     */
    public static long rotateCounterclockwise(long encoded)
    {
        // this is functionally equivalent to, but faster than, the following:
        // return flipVertical4x4(transpose4x4(encoded));
        long v = 0L;
        for (int i4 = 0; i4 < 64; i4 += 4) {
            v |= ((encoded >>> (i4 + 1) & 7L) | ((encoded >>> i4 & 1L) << 3)) << ((i4 >>> 2 & 12L) | ((~i4 & 12L) << 2));
        }
        return v;
    }

    /** Adjusts an existing map that uses box-drawing characters so non-visible line segments aren't rendered.
     * Takes a map that was produced using {@link #hashesToLines(char[][])} and a Region that
     * stores already-seen cells, and writes an altered version of the 2D char array {@code map} to {@code writeInto},
     * leaving non-box-drawing chars unchanged. This method modifies writeInto in-place, and also returns it after those
     * changesare made. The way this works is explained well with an example: if the player is north of a T-junction
     * wall, '┬', then unless he has already explored the area south of his position, the bottom segment of the wall
     * isn't visible to him, and so '─' should be rendered instead of '┬'. If a cell has already been seen, it is
     * considered still visible for the purpose of calculating shown segments (it won't change once you leave an area).
     * @param map a 2D char array that should have been produced by {@link #hashesToLines(char[][])}
     * @param seen a Region where "on" cells are visible now or were visible in the past
     * @param writeInto a 2D char array that must have at least the dimensions of map; will be modified
     * @return writeInto, after modifications
     */
    public static char[][] pruneLines(char[][] map, Region seen, char[][] writeInto)
    {
        return pruneLines(map, seen, light, writeInto);
    }

    /** Adjusts an existing map that uses box-drawing characters so non-visible line segments aren't rendered.
     * Takes a map that was produced using {@link #hashesToLines(char[][])} a Region that stores
     * already-seen cells, an optional char array that refers to a line drawing style constant in this class (defaults
     * to {@link #light}, and writes an altered version of the 2D char array {@code map} to {@code writeInto}, leaving
     * non-box-drawing chars unchanged. This method modifies writeInto in-place, and also returns it after those changes
     * are made. The way this works is explained well with an example: if the player is north of a T-junction wall, '┬',
     * then unless he has already explored the area south of his position, the bottom segment of the wall isn't visible
     * to him, and so '─' should be rendered instead of '┬'. If a cell has already been seen (it is true in
     * {@code seen}), it is considered still visible for the purpose of calculating shown segments (that is, it won't
     * change once you leave an area).
     * @param map a 2D char array that should have been produced by {@link #hashesToLines(char[][])}
     * @param seen a Region where "on" cells are visible now or were visible in the past
     * @param symbols a char array that should be {@link #light} or {@link #heavy} unless you know your font supports
     *                the chars "╴╵╶╷", in which case you can use {@link #lightAlt}, or the heavy-weight versions of 
     *                those chars, in which case you can use {@link #heavyAlt}
     * @param writeInto a 2D char array that must have at least the dimensions of map; will be modified
     * @return writeInto, after modifications
     */
    public static char[][] pruneLines(char[][] map, Region seen, char[] symbols, char[][] writeInto)
    {
        final int width = map.length, height = map[0].length;
        int mask;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if(seen.contains(x, y)) 
                {
                    mask = 15;
                    if(!seen.contains(x-1, y))
                        mask ^= 1;
                    if(!seen.contains(x+1, y))
                        mask ^= 4;
                    if(!seen.contains(x, y-1))
                        mask ^= 2;
                    if(!seen.contains(x, y+1))
                        mask ^= 8;
                    switch (map[x][y]) {
                        case '─':
                        case '━':
                            writeInto[x][y] = symbols[5 & mask];
                            break;
                        case '│':
                        case '┃':
                            writeInto[x][y] = symbols[10 & mask];
                            break;
                        case '┘':
                        case '┛':
                            writeInto[x][y] = symbols[9 & mask];
                            break;
                        case '└':
                        case '┗':
                            writeInto[x][y] = symbols[12 & mask];
                            break;
                        case '┐':
                        case '┓':
                            writeInto[x][y] = symbols[3 & mask];
                            break;
                        case '┌':
                        case '┏':
                            writeInto[x][y] = symbols[6 & mask];
                            break;
                        case '┴':
                        case '┻':
                            writeInto[x][y] = symbols[13 & mask];
                            break;
                        case '┤':
                        case '┫':
                            writeInto[x][y] = symbols[11 & mask];
                            break;
                        case '┬':
                        case '┳':
                            writeInto[x][y] = symbols[7 & mask];
                            break;
                        case '├':
                        case '┣':
                            writeInto[x][y] = symbols[14 & mask];
                            break;
                        case '┼':
                        case '╋':
                            writeInto[x][y] = symbols[15 & mask];
                            break;
                        case '╴':
                        case '╸':
                            writeInto[x][y] = symbols[1 & mask];
                            break;
                        case '╵':
                        case '╹':
                            writeInto[x][y] = symbols[8 & mask];
                            break;
                        case '╶':
                        case '╺':
                            writeInto[x][y] = symbols[4 & mask];
                            break;
                        case '╷':
                        case '╻':
                            writeInto[x][y] = symbols[2 & mask];
                            break;
                    }
                }
            }
        }
        return writeInto;
    }

    /**
     * Takes a char[][] dungeon map that uses '#' to represent walls, and returns a new char[][] that uses unicode box
     * drawing characters to draw straight, continuous lines for walls, filling regions between walls (that were
     * filled with more walls before) with space characters, ' '. If the lines "point the wrong way," such as having
     * multiple horizontally adjacent vertical lines where there should be horizontal lines, call transposeLines() on
     * the returned map, which will keep the dimensions of the map the same and only change the line chars. You will
     * also need to call transposeLines if you call hashesToLines on a map that already has "correct" line-drawing
     * characters, which means hashesToLines should only be called on maps that use '#' for walls. If you have a
     * jumbled map that contains two or more of the following: "correct" line-drawing characters, "incorrect"
     * line-drawing characters, and '#' characters for walls, you can reset by calling linesToHashes() and then
     * potentially calling hashesToLines() again.
     * <br>
     * This also treats any '+' and '/' chars that are next to a cell this is changing as if they were walls, even
     * though neither '+' nor '/' will be changed itself. This is because '+' and '/' are typically used as door glyphs,
     * and since walls connect to doors, the box-drawing characters for adjacent walls should connect to the door as if
     * it is or is embedded in a wall.
     *
     * @param map a 2D char array indexed with x,y that uses '#' for walls
     * @return a copy of the map passed as an argument with box-drawing characters replacing '#' walls
     */
    public static char[][] hashesToLines(char[][] map) {
        return hashesToLinesInto(map, null, false);
    }

    /**
     * Takes a char[][] dungeon map that uses '#' to represent walls, and returns a new char[][] that uses unicode box
     * drawing characters to draw straight, continuous lines for walls, filling regions between walls (that were
     * filled with more walls before) with space characters, ' '. If keepSingleHashes is true, then '#' will be used if
     * a wall has no orthogonal wall neighbors; if it is false, then a horizontal line will be used for stand-alone
     * wall cells. If the lines "point the wrong way," such as having multiple horizontally adjacent vertical lines
     * where there should be horizontal lines, call {@link #transposeLines(char[][])} on the returned map, which will
     * keep the dimensions of the map the same and only change the line chars. You will also need to call transposeLines
     * if you call hashesToLines on a map that already has "correct" line-drawing characters, which means hashesToLines
     * should only be called on maps that use '#' for walls. If you have a jumbled map that contains two or more of the
     * following: "correct" line-drawing characters, "incorrect" line-drawing characters, and '#' characters for walls,
     * you can reset by calling linesToHashes() and then potentially calling hashesToLines() again.
     * <br>
     * This also treats any '+' and '/' chars that are next to a cell this is changing as if they were walls, even
     * though neither '+' nor '/' will be changed itself. This is because '+' and '/' are typically used as door glyphs,
     * and since walls connect to doors, the box-drawing characters for adjacent walls should connect to the door as if
     * it is or is embedded in a wall.
     *
     * @param map              a 2D char array indexed with x,y that uses '#' for walls
     * @param keepSingleHashes true if walls that are not orthogonally adjacent to other walls should stay as '#'
     * @return a copy of the map passed as an argument with box-drawing characters replacing '#' walls
     */
    public static char[][] hashesToLines(char[][] map, boolean keepSingleHashes) {
        return hashesToLinesInto(map, null, keepSingleHashes);
    }
    /**
     * Takes a char[][] place {@code source} that uses '#' to represent walls, and fills a different char[][] {@code target}
     * with Unicode box drawing characters to draw straight, continuous lines for walls, filling regions between walls
     * (that were
     * filled with more walls before) with space characters, ' '. If keepSingleHashes is true, then '#' will be used if
     * a wall has no orthogonal wall neighbors; if it is false, then a horizontal line will be used for stand-alone
     * wall cells. If the lines "point the wrong way," such as having multiple horizontally adjacent vertical lines
     * where there should be horizontal lines, call {@link #transposeLines(char[][])} on the returned map, which will
     * keep the dimensions of the map the same and only change the line chars. You will also need to call transposeLines
     * if you call hashesToLinesInto() on a map that already has "correct" line-drawing characters, which means
     * hashesToLinesInto()
     * should only be called on maps that use '#' for walls. If you have a jumbled map that contains two or more of the
     * following: "correct" line-drawing characters, "incorrect" line-drawing characters, and '#' characters for walls,
     * you can reset by calling linesToHashes() and then potentially calling hashesToLinesInto() again.
     * <br>
     * This also treats any '+' and '/' chars that are next to a cell this is changing as if they were walls, even
     * though neither '+' nor '/' will be changed itself. This is because '+' and '/' are typically used as door glyphs,
     * and since walls connect to doors, the box-drawing characters for adjacent walls should connect to the door as if
     * it is or is embedded in a wall.
     *
     * @param source           a 2D char array indexed with x,y that uses '#' for walls
     * @param target           a 2D char array that will be entirely modified in-place; if null, a new array will be made
     * @param keepSingleHashes true if walls that are not orthogonally adjacent to other walls should stay as '#'
     * @return into, with box-drawing characters replacing '#' walls
     */
    public static char[][] hashesToLinesInto(char[][] source, char[][] target, boolean keepSingleHashes) {
        if(target == null) target = new char[source.length][source[0].length];

        int width = Math.min(source.length, target.length);
        int height = Math.min(source[0].length, target[0].length);

        for (int i = 0; i < width; i++) {
            System.arraycopy(source[i], 0, target[i], 0, height);
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (source[x][y] == '#') {
                    int q = 0;
                    if(y >= height - 1 || source[x][y+1] == '#' || source[x][y+1] == '+' || source[x][y+1] == '/') q |= 1;
                    if(x >= width - 1 || source[x+1][y] == '#' || source[x+1][y] == '+' || source[x+1][y] == '/') q |= 2;
                    if(y == 0 || source[x][y-1] == '#' || source[x][y-1] == '+' || source[x][y-1] == '/') q |= 4;
                    if(x == 0 || source[x-1][y] == '#' || source[x-1][y] == '+' || source[x-1][y] == '/') q |= 8;

                    if(y >= height - 1 || x >= width - 1 || source[x+1][y+1] == '#' || source[x+1][y+1] == '+' || source[x+1][y+1] == '/') q |= 16;
                    if(y == 0 || x >= width - 1 || source[x+1][y-1] == '#' || source[x+1][y-1] == '+' || source[x+1][y-1] == '/') q |= 32;
                    if(y == 0 || x == 0 || source[x-1][y-1] == '#' || source[x-1][y-1] == '+' || source[x-1][y-1] == '/') q |= 64;
                    if(y >= height - 1 || x == 0 || source[x-1][y+1] == '#' || source[x-1][y+1] == '+' || source[x-1][y+1] == '/') q |= 128;
                    if (!keepSingleHashes && wallLookup[q] == '#') {
                        target[x][y] = '─';
                    } else {
                        target[x][y] = wallLookup[q];
                    }
                }
            }
        }
        return target;
    }

    /**
     * Reverses most of the effects of hashesToLines() that may have been applied to {@code map} and returns the result
     * in a new char[][]. The only things that will not be reversed are the placement of
     * space characters in unreachable wall-cells-behind-wall-cells, which remain as spaces. This is useful if you
     * have a modified map that contains wall characters of conflicting varieties, as described in hashesToLines().
     *
     * @param map a 2D char array indexed with x,y that uses box-drawing characters for walls
     * @return a copy of the map passed as an argument with '#' replacing box-drawing characters for walls
     */
    public static char[][] linesToHashes(char[][] map) {
        return linesToHashesInto(map, null);
    }
    /**
     * Reverses most of the effects of hashesToLinesInto() that may have been applied to {@code source} and stores the
     * result in {@code target}. The only things that will not be reversed are the placement of
     * space characters in unreachable wall-cells-behind-wall-cells, which remain as spaces. This is useful if you
     * have a modified map that contains wall characters of conflicting varieties, as described in hashesToLinesInto().
     *
     * @param source a 2D char array indexed with x,y that uses box-drawing characters for walls
     * @param target a 2D char array that will be entirely modified in-place; if null, a new array will be made
     * @return into, with '#' replacing box-drawing characters for walls
     */
    public static char[][] linesToHashesInto(char[][] source, char[][] target) {
        if(target == null) target = new char[source.length][source[0].length];

        int width = Math.min(source.length, target.length);
        int height = Math.min(source[0].length, target[0].length);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (source[i][j]) {
                    case '├':
                    case '┤':
                    case '┴':
                    case '┬':
                    case '┌':
                    case '┐':
                    case '└':
                    case '┘':
                    case '│':
                    case '─':
                    case '┼':
                        target[i][j] = '#';
                        break;
                    default:
                        target[i][j] = source[i][j];
                }
            }
        }
        return target;
    }

    /**
     * If you call hashesToLines() on a map that uses [y][x] conventions instead of [x][y], it will have the lines not
     * connect as you expect. Use this function to change the directions of the box-drawing characters only, without
     * altering the dimensions in any way. This returns a new char[][], instead of modifying the parameter in place.
     * transposeLines is also needed if the lines in a map have become transposed when they were already correct;
     * calling this method on an incorrectly transposed map will change the directions on all of its lines.
     * <br>
     * This method simply calls {@link #transposeLinesInPlace(char[][])} on a copy of {@code map} made with
     * {@link ArrayTools#copy(char[][])}. You may want to just use transposeLinesInPlace() if map is incorrect.
     *
     * @param map a 2D char array indexed with y,x that uses box-drawing characters for walls
     * @return a copy of map that uses box-drawing characters for walls that will be correct when indexed with x,y
     */
    public static char[][] transposeLines(char[][] map) {
        return transposeLinesInPlace(ArrayTools.copy(map));
    }
    /**
     * If you call hashesToLines() on a map that uses [y][x] conventions instead of [x][y], it will have the lines not
     * connect as you expect. Use this function to change the directions of the box-drawing characters only, without
     * altering the dimensions in any way. This modifies the parameter {@code changing} in-place. Calling
     * transposeLines() is also needed if the lines in a map have become transposed when they were already correct;
     * calling this method on an incorrectly transposed map will change the directions on all of its lines.
     *
     * @param changing a 2D char array that will be modified in-place to use different box-drawing characters
     * @return changing, modified in-place to use x,y instead of y,x box-drawing characters
     */
    public static char[][] transposeLinesInPlace(char[][] changing) {
        int width = changing.length;
        int height = changing[0].length;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                switch (changing[i][j]) {
                    case '├':
                        changing[i][j] = '┬';
                        break;
                    case '┤':
                        changing[i][j] = '┴';
                        break;
                    case '┴':
                        changing[i][j] = '┤';
                        break;
                    case '┬':
                        changing[i][j] = '├';
                        break;
                    case '┐':
                        changing[i][j] = '└';
                        break;
                    case '└':
                        changing[i][j] = '┐';
                        break;
                    case '│':
                        changing[i][j] = '─';
                        break;
                    case '─':
                        changing[i][j] = '│';
                        break;
                    //for ┼┌┘ and any non-box-drawing, do nothing
                }
            }
        }
        return changing;
    }
}
