/*
 * Copyright (c) 2022-2023 See AUTHORS file.
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

import com.github.tommyettinger.ds.ObjectList;

import org.checkerframework.checker.nullness.qual.NonNull;
import java.util.List;

/**
 * A simple line-drawing algorithm that only takes orthogonal steps; may be useful for LOS in games that use Manhattan
 * distances for measurements.
 * Algorithm is from http://www.redblobgames.com/grids/line-drawing.html#stepping , thanks Amit!
 */
public class OrthoLine implements LineDrawer {

    /**
     * A buffer of Coord as an ObjectList; this is cleared and reused by the drawLine() methods, so its state can only
     * be certain until you make another call to one of those methods. The drawLine() overloads that explicitly take a
     * buffer argument don't use this field.
     */
    public final ObjectList<Coord> lastLine;
    /**
     * Makes a new OrthoLine and initializes its only state, {@link #lastLine}.
     */
    public OrthoLine(){
        lastLine = new ObjectList<>();
    }

    /**
     * Draws a line from (startX, startY) to (endX, endY) using only N/S/E/W movement.
     * Consider reusing an ObjectList instead of allocating a new one each time, if
     * possible; this method allocates an ObjectList per call, but
     * {@link #line(int, int, int, int, ObjectList)} does not.
     * Returns an ObjectList of Coord in order.
     *
     * @param startX x of starting point
     * @param startY y of starting point
     * @param endX   x of ending point
     * @param endY   y of ending point
     * @return ObjectList of Coord, including (startX, startY) and (endX, endY) and all points walked between
     */
    public static ObjectList<Coord> line(int startX, int startY, int endX, int endY) {
        return line(startX, startY, endX, endY, null);
    }
    /**
     * Draws a line from (startX, startY) to (endX, endY) using only N/S/E/W movement.
     * If {@code buffer} is not null, it will be cleared and reused; if it is null, then
     * a new ObjectList will be allocated. Reusing {@code buffer} across multiple calls
     * is a good way to reduce GC pressure. Returns an ObjectList of Coord in order.
     *
     * @param startX x of starting point
     * @param startY y of starting point
     * @param endX   x of ending point
     * @param endY   y of ending point
     * @param buffer an ObjectList of Coord that will be reused and cleared if not null; will be modified
     * @return ObjectList of Coord, including (startX, startY) and (endX, endY) and all points walked between
     */
    public static ObjectList<Coord> line(int startX, int startY, int endX, int endY, ObjectList<Coord> buffer) {
        int dx = endX - startX, dy = endY - startY, nx = Math.abs(dx), ny = Math.abs(dy);
        int signX = dx >> 31 | 1, signY = dy >> 31 | 1, workX = startX, workY = startY;
        if(buffer == null) {
            buffer = new ObjectList<>(1 + nx + ny);
        }
        else {
            buffer.clear();
            buffer.ensureCapacity(1 + nx + ny);
        }
        buffer.add(Coord.get(startX, startY));
        for (int ix = 0, iy = 0; ix <= nx || iy <= ny; ) {
            if ((0.5f + ix) / nx < (0.5f + iy) / ny) {
                workX += signX;
                ix++;
            } else {
                workY += signY;
                iy++;
            }
            buffer.add(Coord.get(workX, workY));
        }
        return buffer;
    }
    /**
     * Draws a line from (startX, startY) to (endX, endY) using only N/S/E/W movement.
     * If {@code buffer} is not null, it will be cleared and reused; if it is null, then
     * a new ObjectList will be allocated. Reusing {@code buffer} across multiple calls
     * is a good way to reduce GC pressure. Returns an ObjectList of Coord in order.
     *
     * @param startX x of starting point
     * @param startY y of starting point
     * @param endX   x of ending point
     * @param endY   y of ending point
     * @param buffer an ObjectList of Coord that will be reused and cleared if not null; will be modified
     * @return ObjectList of Coord, including (startX, startY) and (endX, endY) and all points walked between
     */
    public static ObjectList<Coord> line(int startX, int startY, int endX, int endY, int maxLength,
                                         ObjectList<Coord> buffer) {
        int dx = endX - startX, dy = endY - startY, nx = Math.abs(dx), ny = Math.abs(dy);
        maxLength = Math.max(0, Math.min(1 + nx + ny, maxLength));
        int signX = dx >> 31 | 1, signY = dy >> 31 | 1, workX = startX, workY = startY;
        if(buffer == null) {
            buffer = new ObjectList<>(maxLength);
        }
        else {
            buffer.clear();
            buffer.ensureCapacity(maxLength);
        }
        if(maxLength == 0) return buffer;
        buffer.add(Coord.get(startX, startY));
        for (int ix = 0, iy = 0; (ix <= nx || iy <= ny) && buffer.size() < maxLength; ) {
            if ((0.5f + ix) / nx < (0.5f + iy) / ny) {
                workX += signX;
                ix++;
            } else {
                workY += signY;
                iy++;
            }
            buffer.add(Coord.get(workX, workY));
        }
        return buffer;
    }
    /**
     * Checks whether the starting point can see the target point, using the {@code resistanceMap}
     * to determine whether the line of sight is obstructed, and filling the list of cells along the line of sight into
     * {@code buffer}. {@code resistanceMap} must not be null; it can be initialized in the same way as FOV's resistance
     * maps can with {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}.
     * {@code buffer} may be null (in which case a temporary ObjectList is allocated, which can be wasteful), or may be
     * an existing ObjectList of Coord (which will be cleared if it has any contents). If the starting point can see the
     * target point, this returns true and buffer will contain all Coord points along the line of sight; otherwise this
     * returns false and buffer will only contain up to and including the point that blocked the line of sight.
     * @param start the starting point
     * @param target the target point
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @param buffer an ObjectList of Coord that will be reused and cleared if not null; will be modified
     * @return true if the starting point can see the target point; false otherwise
     */
    public static boolean reachable(@NonNull Coord start, @NonNull Coord target, @NonNull float[][] resistanceMap,
                                    ObjectList<Coord> buffer){
        return reachable(start.x, start.y, target.x, target.y, 0x7FFFFFFF, resistanceMap, buffer);
    }
    /**
     * Checks whether the starting point can see the target point, using the {@code resistanceMap}
     * to determine whether the line of sight is obstructed, and filling the list of cells along the line of sight into
     * {@code buffer}. {@code resistanceMap} must not be null; it can be initialized in the same way as FOV's resistance
     * maps can with {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}.
     * {@code buffer} may be null (in which case a temporary ObjectList is allocated, which can be wasteful), or may be
     * an existing ObjectList of Coord (which will be cleared if it has any contents). If the starting point can see the
     * target point, this returns true and buffer will contain all Coord points along the line of sight; otherwise this
     * returns false and buffer will only contain up to and including the point that blocked the line of sight.
     * @param startX the x-coordinate of the starting point
     * @param startY  the y-coordinate of the starting point
     * @param targetX the x-coordinate of the target point
     * @param targetY  the y-coordinate of the target point
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @param buffer an ObjectList of Coord that will be reused and cleared if not null; will be modified
     * @return true if the starting point can see the target point; false otherwise
     */
    public static boolean reachable(int startX, int startY, int targetX, int targetY,
                                    @NonNull float[][] resistanceMap, ObjectList<Coord> buffer){
        return reachable(startX, startY, targetX, targetY, 0x7FFFFFFF, resistanceMap, buffer);
    }
    /**
     * Checks whether the starting point can see the target point, using the {@code maxLength} and {@code resistanceMap}
     * to determine whether the line of sight is obstructed, and filling the list of cells along the line of sight into
     * {@code buffer}. {@code resistanceMap} must not be null; it can be initialized in the same way as FOV's resistance
     * maps can with {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}.
     * {@code buffer} may be null (in which case a temporary ObjectList is allocated, which can be wasteful), or may be
     * an existing ObjectList of Coord (which will be cleared if it has any contents). If the starting point can see the
     * target point, this returns true and buffer will contain all Coord points along the line of sight; otherwise this
     * returns false and buffer will only contain up to and including the point that blocked the line of sight.
     * @param startX the x-coordinate of the starting point
     * @param startY  the y-coordinate of the starting point
     * @param targetX the x-coordinate of the target point
     * @param targetY  the y-coordinate of the target point
     * @param maxLength the maximum permitted length of a line of sight
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @param buffer an ObjectList of Coord that will be reused and cleared if not null; will be modified
     * @return true if the starting point can see the target point; false otherwise
     */
    public static boolean reachable(int startX, int startY, int targetX, int targetY, int maxLength,
                                    @NonNull float[][] resistanceMap, ObjectList<Coord> buffer) {
        int dx = targetX - startX, dy = targetY - startY, nx = Math.abs(dx), ny = Math.abs(dy);
        int signX = dx >> 31 | 1, signY = dy >> 31 | 1, x = startX, y = startY;

        int dist = nx + ny;
        if(buffer == null) {
            buffer = new ObjectList<>(dist + 1);
        }
        else {
            buffer.clear();
        }
        if(maxLength <= 0) return false;

        if(startX == targetX && startY == targetY) {
            buffer.add(Coord.get(startX, startY));
            return true; // already at the point; we can see our own feet just fine!
        }
        float decay = 1f / dist;
        float currentForce = 1f;

        for (int ix = 0, iy = 0; (ix <= nx || iy <= ny) && buffer.size() < maxLength; ) {
            buffer.add(Coord.get(x, y));
            if (x == targetX && y == targetY) {
                return true;
            }

            if (x != startX || y != startY) { //don't discount the start location even if on resistant cell
                currentForce -= resistanceMap[x][y];
            }
            currentForce -= decay;
            if (currentForce <= -0.001f) {
                return false; //too much resistance
            }

            if ((0.5f + ix) / nx < (0.5f + iy) / ny) {
                x += signX;
                ix++;
            } else {
                y += signY;
                iy++;
            }
        }
        return false;//never got to the target point
    }
    /**
     * Checks whether the starting point can see the target point, using the {@code resistanceMap} to determine whether
     * the line of sight is obstructed, without storing the line of points along the way. {@code resistanceMap} must not
     * be null; it can be initialized in the same way as FOV's resistance maps can with
     * {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}. If the starting
     * point can see the target point, this returns true; otherwise this returns false.
     * @param start the starting point
     * @param target the target point
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @return true if the starting point can see the target point; false otherwise
     */
    public static boolean reachable(@NonNull Coord start, @NonNull Coord target, @NonNull float[][] resistanceMap){
        return reachable(start.x, start.y, target.x, target.y, 0x7FFFFFFF, resistanceMap);
    }
    /**
     * Checks whether the starting point can see the target point, using the {@code resistanceMap} to determine whether
     * the line of sight is obstructed, without storing the line of points along the way. {@code resistanceMap} must not
     * be null; it can be initialized in the same way as FOV's resistance maps can with
     * {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}. If the starting
     * point can see the target point, this returns true; otherwise this returns false.
     * @param startX the x-coordinate of the starting point
     * @param startY  the y-coordinate of the starting point
     * @param targetX the x-coordinate of the target point
     * @param targetY  the y-coordinate of the target point
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @return true if the starting point can see the target point; false otherwise
     */
    public static boolean reachable(int startX, int startY, int targetX, int targetY,
                                    @NonNull float[][] resistanceMap){
        return reachable(startX, startY, targetX, targetY, 0x7FFFFFFF, resistanceMap);
    }
    /**
     * Checks whether the starting point can see the target point, using the {@code maxLength} and {@code resistanceMap}
     * to determine whether the line of sight is obstructed, without storing the line of points along the way.
     * {@code resistanceMap} must not be null; it can be initialized in the same way as FOV's resistance maps can with
     * {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}. If the starting
     * point can see the target point, this returns true; otherwise this returns false.
     * @param startX the x-coordinate of the starting point
     * @param startY  the y-coordinate of the starting point
     * @param targetX the x-coordinate of the target point
     * @param targetY  the y-coordinate of the target point
     * @param maxLength the maximum permitted length of a line of sight
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @return true if the starting point can see the target point; false otherwise
     */
    public static boolean reachable(int startX, int startY, int targetX, int targetY, int maxLength,
                                    @NonNull float[][] resistanceMap) {
        if(maxLength <= 0) return false;

        int dx = targetX - startX, dy = targetY - startY, nx = Math.abs(dx), ny = Math.abs(dy);
        int signX = dx >> 31 | 1, signY = dy >> 31 | 1, x = startX, y = startY;

        int dist = nx + ny, traveled = 0;
        if(startX == targetX && startY == targetY) {
            return true; // already at the point; we can see our own feet just fine!
        }
        float decay = 1f / dist;
        float currentForce = 1f;

        for (int ix = 0, iy = 0; (ix <= nx || iy <= ny) && traveled < maxLength; ) {
            ++traveled;
            if (x == targetX && y == targetY) {
                return true;
            }

            if (x != startX || y != startY) { //don't discount the start location even if on resistant cell
                currentForce -= resistanceMap[x][y];
            }
            currentForce -= decay;
            if (currentForce <= -0.001f) {
                return false; //too much resistance
            }

            if ((0.5f + ix) / nx < (0.5f + iy) / ny) {
                x += signX;
                ix++;
            } else {
                y += signY;
                iy++;
            }
        }
        return false;//never got to the target point
    }
    /**
     * Draws a line from start to end using only N/S/E/W movement.
     * Consider reusing an ObjectList instead of allocating a new one each time, if
     * possible; this method allocates an ObjectList per call, but
     * {@link #line(int, int, int, int, ObjectList)} does not.
     * Returns an ObjectList of Coord in order.
     *
     * @param start starting point
     * @param end ending point
     * @return ObjectList of Coord, including start and end and all points walked between
     */
    public static ObjectList<Coord> line(Coord start, Coord end)
    {
        return line(start.x, start.y, end.x, end.y);
    }
    /**
     * Draws a line from (startX, startY) to (endX, endY) using only N/S/E/W movement.
     * Allocates a new exactly-sized array of Coord, in order, and returns it.
     *
     * @param startX x of starting point
     * @param startY y of starting point
     * @param endX   x of ending point
     * @param endY   y of ending point
     * @return array of Coord, including (startX, startY) and (endX, endY) and all points walked between
     */
    public static Coord[] lineArray(int startX, int startY, int endX, int endY) {
        int dx = endX - startX, dy = endY - startY, nx = Math.abs(dx), ny = Math.abs(dy);
        int signX = dx >> 31 | 1, signY = dy >> 31 | 1, workX = startX, workY = startY;
        Coord[] drawn = new Coord[nx + ny + 1];
        drawn[0] = Coord.get(startX, startY);
        for (int i = 1, ix = 0, iy = 0; ix <= nx || iy <= ny; i++) {
            if ((0.5f + ix) / nx < (0.5f + iy) / ny) {
                workX += signX;
                ix++;
            } else {
                workY += signY;
                iy++;
            }
            drawn[i] = Coord.get(workX, workY);
        }
        return drawn;
    }
    /**
     * Draws a line from (startX, startY) to (endX, endY) using only N/S/E/W movement.
     * Allocates a new exactly-sized array of Coord, in order, and returns it.
     *
     * @param startX x of starting point
     * @param startY y of starting point
     * @param endX   x of ending point
     * @param endY   y of ending point
     * @return array of Coord, including (startX, startY) and (endX, endY) and all points walked between
     */
    public static Coord[] lineArray(int startX, int startY, int endX, int endY, int maxLength) {
        int dx = endX - startX, dy = endY - startY, nx = Math.abs(dx), ny = Math.abs(dy);
        maxLength = Math.max(0, Math.min(nx + ny + 1, maxLength));
        int signX = dx >> 31 | 1, signY = dy >> 31 | 1, workX = startX, workY = startY;
        Coord[] drawn = new Coord[maxLength];
        if(maxLength == 0) return drawn;
        drawn[0] = Coord.get(startX, startY);
        for (int i = 1, ix = 0, iy = 0; ix <= nx || iy <= ny; i++) {
            if ((0.5f + ix) / nx < (0.5f + iy) / ny) {
                workX += signX;
                ix++;
            } else {
                workY += signY;
                iy++;
            }
            drawn[i] = Coord.get(workX, workY);
        }
        return drawn;
    }

    /**
     * Draws a line from start to end using only N/S/E/W movement.
     * Allocates a new exactly-sized array of Coord, in order, and returns it.
     *
     * @param start starting point
     * @param end ending point
     * @return array of Coord, including start and end and all points walked between
     */
    public static Coord[] lineArray(Coord start, Coord end)
    {
        return lineArray(start.x, start.y, end.x, end.y);
    }

    /**
     * Given an array of Coord as produced by {@link #lineArray(Coord, Coord)} or
     * {@link #lineArray(int, int, int, int)}, this gets a char array of box-drawing characters that connect when drawn
     * at the corresponding Coord positions in the given line. This can be useful for drawing highlight lines or showing
     * what path something will take, as long as it only uses 4-way orthogonal connections between Coords. Any
     * connections that require a diagonal will not be handled by this method (returning a straight line without much
     * accuracy), and any Coords that aren't adjacent will cause an {@link IllegalStateException} if this has to draw a
     * line between them. If this method is called on the result of this class' lineArray() method, then it should
     * always return a valid result; if it is called on a path made with some other method, then it shouldn't throw an
     * exception but may produce a low-quality (visually disconnected) line.
     *
     * @param line a Coord array where each Coord is orthogonally adjacent to its neighbor(s) in the array; usually
     *             produced via {@link #lineArray(Coord, Coord)} or {@link #lineArray(int, int, int, int)}
     * @return a char array of box-drawing chars that will connect when drawn at the same points as in line
     */
    public static char[] lineChars(Coord[] line)
    {
        // ─│┌┐└┘├┤┬┴┼
        if(line == null) return null;
        int len = line.length;
        if(len == 0) return new char[0];
        if(len == 1) return new char[]{'─'};
        char[] cs = new char[len];
        cs[0] = line[0].x == line[1].x ? '│' : '─';
        cs[len - 1] = line[len - 1].x == line[len - 2].x ? '│' : '─';
        Coord before, current, next;
        for (int i = 1; i < len - 1; i++) {
            before = line[i-1];
            current = line[i];
            next = line[i+1];
            switch (before.toGoTo(current))
            {
                case RIGHT:
                    switch (current.toGoTo(next))
                    {
                        case DOWN: cs[i] = '┘';
                            break;
                        case UP: cs[i] = '┐';
                            break;
                        default: cs[i] = '─';
                            break;
                    }
                    break;
                case LEFT:
                    switch (current.toGoTo(next))
                    {
                        case DOWN: cs[i] = '└';
                            break;
                        case UP: cs[i] = '┌';
                            break;
                        default: cs[i] = '─';
                            break;
                    }
                    break;
                case UP:
                    switch (current.toGoTo(next))
                    {
                        case LEFT: cs[i] = '┐';
                            break;
                        case RIGHT: cs[i] = '┌';
                            break;
                        default: cs[i] = '│';
                            break;
                    }
                    break;
                default:
                    switch (current.toGoTo(next))
                    {
                        case LEFT: cs[i] = '┘';
                            break;
                        case RIGHT: cs[i] = '└';
                            break;
                        default: cs[i] = '│';
                            break;
                    }
            }
        }
        return cs;
    }
    /**
     * Given a List of Coord as produced by {@link #line(Coord, Coord)} or {@link #line(int, int, int, int)}, this
     * gets a char array of box-drawing characters that connect when drawn at the corresponding Coord positions in the
     * given line. This can be useful for drawing highlight lines or showing what path something will take, as long as
     * it only uses 4-way orthogonal connections between Coords. Any connections that require a diagonal will not be
     * handled by this method (returning a straight line without much accuracy), and any Coords that aren't adjacent
     * will cause an {@link IllegalStateException} if this has to draw a line between them. If this method is called on
     * the result of this class' line() method, then it should always return a valid result; if it is called on a path
     * made with some other method, then it shouldn't throw an exception but may produce a low-quality (visually
     * disconnected) line.
     *
     * @param line a List of Coord where each Coord is orthogonally adjacent to its neighbor(s) in the List; usually
     *             produced via {@link #line(Coord, Coord)} or {@link #line(int, int, int, int)}
     * @return a char array of box-drawing chars that will connect when drawn at the same points as in line
     */
    public static char[] lineChars(List<Coord> line)
    {
        // ─│┌┐└┘├┤┬┴┼
        if(line == null) return null;
        int len = line.size();
        if(len == 0) return new char[0];
        if(len == 1) return new char[]{'─'};
        char[] cs = new char[len];
        cs[0] = line.get(0).x == line.get(1).x ? '│' : '─';
        cs[len - 1] = line.get(len - 1).x == line.get(len - 2).x ? '│' : '─';
        Coord before, current, next;
        for (int i = 1; i < len - 1; i++) {
            before = line.get(i-1);
            current = line.get(i);
            next = line.get(i+1);
            switch (before.toGoTo(current))
            {
                case RIGHT:
                    switch (current.toGoTo(next))
                    {
                        case DOWN: cs[i] = '┘';
                            break;
                        case UP: cs[i] = '┐';
                            break;
                        default: cs[i] = '─';
                            break;
                    }
                    break;
                case LEFT:
                    switch (current.toGoTo(next))
                    {
                        case DOWN: cs[i] = '└';
                            break;
                        case UP: cs[i] = '┌';
                            break;
                        default: cs[i] = '─';
                            break;
                    }
                    break;
                case UP:
                    switch (current.toGoTo(next))
                    {
                        case LEFT: cs[i] = '┐';
                            break;
                        case RIGHT: cs[i] = '┌';
                            break;
                        default: cs[i] = '│';
                            break;
                    }
                    break;
                default:
                    switch (current.toGoTo(next))
                    {
                        case LEFT: cs[i] = '┘';
                            break;
                        case RIGHT: cs[i] = '└';
                            break;
                        default: cs[i] = '│';
                            break;
                    }
            }
        }
        return cs;
    }


    /**
     * Gets the last line drawn using the internal buffer this carries, rather than an explicitly-specified buffer.
     *
     * @return an ObjectList of Coord that contains the last line drawn with this BresenhamLine's internal buffer
     */
    @Override
    public ObjectList<Coord> getLastLine() {
        return lastLine;
    }

    /**
     * Generates a 2D Bresenham line between two points. Reuses {@link #lastLine}
     * and returns it as the buffer; later calls to drawLine() without a buffer
     * will probably clear lastLine (which is the same ObjectList this returns)
     * as they are run.
     * @param a the starting point
     * @param b the ending point
     * @return The path between {@code a} and {@code b}.
     */
    @Override
    public ObjectList<Coord> drawLine(Coord a, Coord b) {
        lastLine.clear();
        return line(a.x, a.y, b.x, b.y, lastLine);
    }

    /**
     * Generates a 2D Bresenham line between two points. Reuses {@link #lastLine}
     * and returns it as the buffer; later calls to drawLine() without a buffer
     * will probably clear lastLine (which is the same ObjectList this returns)
     * as they are run.
     * <br>
     * Uses ordinary Coord values for points, and these can be pooled
     * if they are within what the current pool allows (it starts,
     * by default, pooling Coords with x and y between -3 and 255,
     * inclusive). If the Coords are pool-able, it can significantly
     * reduce the work the garbage collector needs to do, especially
     * on Android.
     *
     * @param startX the x coordinate of the starting point
     * @param startY the y coordinate of the starting point
     * @param targetX the x coordinate of the target point
     * @param targetY the y coordinate of the target point
     * @return a ObjectList of Coord points along the line
     */
    @Override
    public ObjectList<Coord> drawLine(int startX, int startY, int targetX, int targetY) {
        lastLine.clear();
        // largest positive int for maxLength; a ObjectList cannot actually be given that many elements on the JVM
        return line(startX, startY, targetX, targetY, 0x7fffffff, lastLine);
    }

    /**
     * Generates a 2D Bresenham line between two points, stopping early if
     * the number of Coords returned reaches maxLength (measured using
     * Chebyshev distance, where diagonally adjacent cells are considered
     * exactly as distant as orthogonally-adjacent cells). Reuses {@link #lastLine}
     * and returns it as the buffer; later calls to drawLine() without a buffer
     * will probably clear lastLine (which is the same ObjectList this returns)
     * as they are run.
     * <br>
     * Uses ordinary Coord values for points, and these can be pooled
     * if they are within what the current pool allows (it starts,
     * by default, pooling Coords with x and y between -3 and 255,
     * inclusive). If the Coords are pool-able, it can significantly
     * reduce the work the garbage collector needs to do, especially
     * on Android.
     *
     * @param startX the x coordinate of the starting point
     * @param startY the y coordinate of the starting point
     * @param targetX the x coordinate of the target point
     * @param targetY the y coordinate of the target point
     * @return a ObjectList of Coord points along the line
     */
    @Override
    public ObjectList<Coord> drawLine(int startX, int startY, int targetX, int targetY, int maxLength) {
        lastLine.clear();
        return line(startX, startY, targetX, targetY, maxLength, lastLine);
    }

    /**
     * Generates a 2D Bresenham line between two points. If you want to save
     * some memory, you can reuse an ObjectList of Coord, {@code buffer},
     * which will be cleared and filled with the resulting line of Coord.
     * If {@code buffer} is null, this will create a new ObjectList of Coord
     * and return that.
     * <br>
     * Uses ordinary Coord values for points, and these can be pooled
     * if they are within what the current pool allows (it starts,
     * by default, pooling Coords with x and y between -3 and 255,
     * inclusive). If the Coords are pool-able, it can significantly
     * reduce the work the garbage collector needs to do, especially
     * on Android.
     *
     * @param startX the x coordinate of the starting point
     * @param startY the y coordinate of the starting point
     * @param targetX the x coordinate of the target point
     * @param targetY the y coordinate of the target point
     * @param buffer an ObjectList of Coord that will be reused and cleared if not null; will be modified
     * @return an ObjectList of Coord points along the line
     */
    @Override
    public ObjectList<Coord> drawLine(int startX, int startY, int targetX, int targetY, ObjectList<Coord> buffer) {
        return line(startX, startY, targetX, targetY, buffer);
    }

    /**
     * Checks whether the starting point can see the target point, using the {@code resistanceMap}
     * to determine whether the line of sight is obstructed, and filling the list of cells along the line of sight into
     * {@code buffer}. {@code resistanceMap} must not be null; it can be initialized in the same way as FOV's resistance
     * maps can with {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}.
     * {@code buffer} may be null (in which case a temporary ObjectList is allocated, which can be wasteful), or may be
     * an existing ObjectList of Coord (which will be cleared if it has any contents). If the starting point can see the
     * target point, this returns true and buffer will contain all Coord points along the line of sight; otherwise this
     * returns false and buffer will only contain up to and including the point that blocked the line of sight.
     * @param start the starting point
     * @param target the target point
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @param buffer an ObjectList of Coord that will be reused and cleared if not null; will be modified
     * @return true if the starting point can see the target point; false otherwise
     */
    @Override
    public boolean isReachable(@NonNull Coord start, @NonNull Coord target, @NonNull float[][] resistanceMap,
                               ObjectList<Coord> buffer){
        return reachable(start.x, start.y, target.x, target.y, resistanceMap, buffer);
    }

    /**
     * Generates a 2D Bresenham line between two points, stopping early if
     * the number of Coords returned reaches maxLength (measured using
     * Chebyshev distance, where diagonally adjacent cells are considered
     * exactly as distant as orthogonally-adjacent cells). If you want to save
     * some memory, you can reuse an ObjectList of Coord, {@code buffer},
     * which will be cleared and filled with the resulting line of Coord.
     * If {@code buffer} is null, this will create a new ObjectList of Coord
     * and return that.
     * <br>
     * Uses ordinary Coord values for points, and these can be pooled
     * if they are within what the current pool allows (it starts,
     * by default, pooling Coords with x and y between -3 and 255,
     * inclusive). If the Coords are pool-able, it can significantly
     * reduce the work the garbage collector needs to do, especially
     * on Android.
     *
     * @param startX the x coordinate of the starting point
     * @param startY the y coordinate of the starting point
     * @param targetX the x coordinate of the target point
     * @param targetY the y coordinate of the target point
     * @param maxLength the largest count of Coord points this can return; will stop early if reached
     * @param buffer an ObjectList of Coord that will be reused and cleared if not null; will be modified
     * @return an ObjectList of Coord points along the line
     */
    @Override
    public ObjectList<Coord> drawLine(int startX, int startY, int targetX, int targetY, int maxLength, ObjectList<Coord> buffer) {
        return line(startX, startY, targetX, targetY, maxLength, buffer);
    }

    /**
     * Checks whether the starting point can see the target point, using the {@code resistanceMap}
     * to determine whether the line of sight is obstructed, and filling the list of cells along the line of sight into
     * {@code buffer}. {@code resistanceMap} must not be null; it can be initialized in the same way as FOV's resistance
     * maps can with {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}.
     * {@code buffer} may be null (in which case a temporary ObjectList is allocated, which can be wasteful), or may be
     * an existing ObjectList of Coord (which will be cleared if it has any contents). If the starting point can see the
     * target point, this returns true and buffer will contain all Coord points along the line of sight; otherwise this
     * returns false and buffer will only contain up to and including the point that blocked the line of sight.
     * @param startX the x-coordinate of the starting point
     * @param startY  the y-coordinate of the starting point
     * @param targetX the x-coordinate of the target point
     * @param targetY  the y-coordinate of the target point
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @param buffer an ObjectList of Coord that will be reused and cleared if not null; will be modified
     * @return true if the starting point can see the target point; false otherwise
     */
    @Override
    public boolean isReachable(int startX, int startY, int targetX, int targetY,
                               @NonNull float[][] resistanceMap, ObjectList<Coord> buffer){
        return reachable(startX, startY, targetX, targetY, resistanceMap, buffer);
    }

    /**
     * Checks whether the starting point can see the target point, using the {@code maxLength} and {@code resistanceMap}
     * to determine whether the line of sight is obstructed, and filling the list of cells along the line of sight into
     * {@code buffer}. The {@code maxLength} is measured using Chebyshev distance, where diagonally adjacent cells are
     * considered exactly as distant as orthogonally-adjacent cells.
     * {@code resistanceMap} must not be null; it can be initialized in the same way as FOV's resistance
     * maps can with {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}.
     * {@code buffer} may be null (in which case a temporary ObjectList is allocated, which can be wasteful), or may be
     * an existing ObjectList of Coord (which will be cleared if it has any contents). If the starting point can see the
     * target point, this returns true and buffer will contain all Coord points along the line of sight; otherwise this
     * returns false and buffer will only contain up to and including the point that blocked the line of sight.
     * @param startX the x-coordinate of the starting point
     * @param startY  the y-coordinate of the starting point
     * @param targetX the x-coordinate of the target point
     * @param targetY  the y-coordinate of the target point
     * @param maxLength the maximum permitted length of a line of sight
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @param buffer an ObjectList of Coord that will be reused and cleared if not null; will be modified
     * @return true if the starting point can see the target point; false otherwise
     */
    @Override
    public boolean isReachable(int startX, int startY, int targetX, int targetY, int maxLength,
                               @NonNull float[][] resistanceMap, ObjectList<Coord> buffer) {
        return reachable(startX, startY, targetX, targetY, maxLength, resistanceMap, buffer);
    }

    /**
     * Checks whether the starting point can see the target point, using the {@code resistanceMap} to determine whether
     * the line of sight is obstructed, without storing the line of points along the way. {@code resistanceMap} must not
     * be null; it can be initialized in the same way as FOV's resistance maps can with
     * {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}. If the starting
     * point can see the target point, this returns true; otherwise this returns false.
     * @param start the starting point
     * @param target the target point
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @return true if the starting point can see the target point; false otherwise
     */
    @Override
    public boolean isReachable(@NonNull Coord start, @NonNull Coord target, @NonNull float[][] resistanceMap){
        return reachable(start.x, start.y, target.x, target.y, resistanceMap);
    }

    /**
     * Checks whether the starting point can see the target point, using the {@code resistanceMap}
     * to determine whether the line of sight is obstructed, without storing the line of points along the way.
     * {@code resistanceMap} must not be null; it can be initialized in the same way as FOV's resistance maps can with
     * {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}. If the starting
     * point can see the target point, this returns true; otherwise this returns false.
     * @param startX the x-coordinate of the starting point
     * @param startY  the y-coordinate of the starting point
     * @param targetX the x-coordinate of the target point
     * @param targetY  the y-coordinate of the target point
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @return true if the starting point can see the target point; false otherwise
     */
    @Override
    public boolean isReachable(int startX, int startY, int targetX, int targetY,
                               @NonNull float[][] resistanceMap){
        return reachable(startX, startY, targetX, targetY, resistanceMap);
    }

    /**
     * Checks whether the starting point can see the target point, using the {@code maxLength} and {@code resistanceMap}
     * to determine whether the line of sight is obstructed, without storing the line of points along the way.
     * {@code resistanceMap} must not be null; it can be initialized in the same way as FOV's resistance maps can with
     * {@link FOV#generateResistances(char[][])} or {@link FOV#generateSimpleResistances(char[][])}. If the starting
     * point can see the target point, this returns true; otherwise this returns false.
     * @param startX the x-coordinate of the starting point
     * @param startY  the y-coordinate of the starting point
     * @param targetX the x-coordinate of the target point
     * @param targetY  the y-coordinate of the target point
     * @param maxLength the maximum permitted length of a line of sight
     * @param resistanceMap a resistance map as produced by {@link FOV#generateResistances(char[][])}; 0 is visible and 1 is blocked
     * @return true if the starting point can see the target point; false otherwise
     */
    @Override
    public boolean isReachable(int startX, int startY, int targetX, int targetY, int maxLength,
                               @NonNull float[][] resistanceMap){
        return reachable(startX, startY, targetX, targetY, maxLength, resistanceMap);
    }
    /**
     * Generates a 2D Bresenham line between two points.
     * This allocates a new array with each call, sized to fit the
     * line exactly. It does not change {@link #lastLine}.
     *
     * @param a the starting point
     * @param b the ending point
     * @return The path between {@code a} and {@code b}.
     */
    @Override
    public Coord[] drawLineArray(Coord a, Coord b) {
        return lineArray(a.x, a.y, b.x, b.y);
    }

    /**
     * Generates a 2D Bresenham line between two points. Returns an array
     * of Coord instead of a ObjectList.
     * This allocates a new array with each call, sized to fit the
     * line exactly.
     * <br>
     * Uses ordinary Coord values for points, and these can be pooled
     * if they are within what the current pool allows (it starts,
     * by default, pooling Coords with x and y between -3 and 255,
     * inclusive). If the Coords are pool-able, it can significantly
     * reduce the work the garbage collector needs to do, especially
     * on Android.
     *
     * @param startX the x coordinate of the starting point
     * @param startY the y coordinate of the starting point
     * @param targetX the x coordinate of the target point
     * @param targetY the y coordinate of the target point
     * @return an array of Coord points along the line
     */
    @Override
    public Coord[] drawLineArray(int startX, int startY, int targetX, int targetY) {
        // largest positive int for maxLength; it is extremely unlikely that this could be reached
        return lineArray(startX, startY, targetX, targetY, 0x7fffffff);
    }

    /**
     * Generates a 2D Bresenham line between two points, stopping early if
     * the number of Coords returned reaches maxLength.. Returns an array
     * of Coord instead of an ObjectList.
     * This allocates a new array with each call, sized to fit the
     * line exactly.
     * <br>
     * Uses ordinary Coord values for points, and these can be pooled
     * if they are within what the current pool allows (it starts,
     * by default, pooling Coords with x and y between -3 and 255,
     * inclusive). If the Coords are pool-able, it can significantly
     * reduce the work the garbage collector needs to do, especially
     * on Android.
     *
     * @param startX the x coordinate of the starting point
     * @param startY the y coordinate of the starting point
     * @param targetX the x coordinate of the target point
     * @param targetY the y coordinate of the target point
     * @param maxLength the largest count of Coord points this can return; will stop early if reached
     * @return an array of Coord points along the line
     */
    @Override
    public Coord[] drawLineArray(int startX, int startY, int targetX, int targetY, int maxLength) {
        return lineArray(startX, startY, targetX, targetY, maxLength);
    }

}
