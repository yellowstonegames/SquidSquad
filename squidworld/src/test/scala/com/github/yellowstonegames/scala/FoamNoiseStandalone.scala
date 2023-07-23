package com.github.yellowstonegames.scala


import com.badlogic.gdx.utils.NumberUtils

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


/**
  * Foam noise code that depends only on libGDX.
  * <br>
  * This is a very-smooth type of noise that can work well using fewer octaves than simplex noise or value noise.
  * You can call [[noiseWithOctaves(double, double, long, int, double)]] with 2 or more octaves to get higher
  * levels of detail.
  * <br>
  * This is a drop-in replacement for the SimplexNoise.java file written by Stefan Gustavson in 2012.
  * It (obviously) uses foam noise instead of simplex noise, but otherwise follows the same ideas as my other
  * standalone noise class(es), which could be distributed in the same folder.
  */
object FoamNoiseStandalone {

  val instance = new FoamNoiseStandalone()

  /**
    * 2D foam noise with the lowest, fastest level of detail. Uses the
    * seed <pre>12345L</pre> and does not change x or y.
    *
    * @param x x coordinate
    * @param y y coordinate
    * @return noise between -1 and 1, inclusive
    */
  def noise(x: Double, y: Double): Double = noiseWithSeed(x, y, 12345L)

  /**
    * Gets foam noise with the lowest, fastest level of detail. Uses the given seed
    * and does not change x or y.
    *
    * @param x    x coordinate
    * @param y    y coordinate
    * @param seed the seed to use for the noise (used in place of { @link #getSeed()})
    * @return noise between -1 and 1
    */
  def noiseWithSeed(x: Double, y: Double, seed: Long): Double = {
    val p0 = x
    val p1 = x * -0.5 + y * 0.8660254037844386
    val p2 = x * -0.5 + y * -0.8660254037844387
    var xin = p1
    var yin = p2
    val a = valueNoise(xin, yin, seed)
    xin = p2
    yin = p0
    val b = valueNoise(xin + a, yin, seed + 0x9A827999FCEF3243L)
    xin = p0
    yin = p1
    val c = valueNoise(xin + b, yin, seed + 0x3504F333F9DE6486L)
    val result = (a + b + c) * 0.3333333333333333
    // Barron spline
    val sharp = 0.75 * 2.2 // increase to sharpen, decrease to soften
    val diff = 0.5 - result
    val sign = NumberUtils.doubleToLongBits(diff) >> 63
    val one = sign | 1
    ((result + sign) / (Double.MinValue - sign + (result + sharp * diff) * one) - sign - sign) - 1.0
  }

  /**
    * Gets value noise with the lowest, fastest level of detail. Uses the given seed
    * and does not change x or y. This has a different output range (0 to 1) than foam noise.
    *
    * @param xi    x coordinate
    * @param yi    y coordinate
    * @param seed the seed to use for the noise (used in place of { @link #getSeed()})
    * @return noise between 0 and 1
    */
  def valueNoise(xi: Double, yi: Double, seed: Long): Double = {
    var x = xi
    var y = yi
    val STEPX = 0xC13FA9A902A6328FL
    val STEPY = 0x91E10DA5C79E7B1DL
    var xFloor = Math.floor(x).toLong
    x -= xFloor
    x *= x * (3 - 2 * x)
    var yFloor = Math.floor(y).toLong
    y -= yFloor
    y *= y * (3 - 2 * y)
    xFloor *= STEPX
    yFloor *= STEPY
    ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, seed) + x * hashPart(xFloor + STEPX, yFloor, seed)) + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, seed))) * 5.421010862427522E-20 + 0.5 //0x1 p -64 + 0.5
  }

  /**
    * Constants are from harmonious numbers, essentially negative integer powers of specific irrational numbers times
    * (2 to the 64).
    *
    * @param x should be premultiplied by 0xC13FA9A902A6328FL
    * @param y should be premultiplied by 0x91E10DA5C79E7B1DL
    * @param si state, any long
    * @return a mediocre 64-bit hash
    */
  def hashPart(x: Long, y: Long, si: Long) = {
    var s = si
    s ^= x ^ y
    //val s2 = (s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L
    //s2 ^ s >>> 25

    val s2 = (s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L
    s2 ^ s2 >>> 25
  }

  ///**
  //  * Gets foam noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses
  //  * the given seed, and multiplies x and y by frequency.
  //  *
  //  * @param x    x coordinate, will be adjusted by frequency
  //  * @param y    y coordinate, will be adjusted by frequency
  //  * @param seed the seed to use for the noise (used in place of { @link #getSeed()})
  //  * @param octaves level of detail, from 1 to about 16 as a practical maximum
  //  * @return noise between -1 and 1
  //  */
  //def noiseWithOctaves(x: Double, y: Double, seed: Long, octaves: Int, frequency: Double): Double = noiseWithOctaves(x, y, seed, octaves, frequency)

  /**
    * Gets foam noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses the given
    * seed instead of [[#getSeed()]], and multiplies x and y by frequency.
    *
    * @param xi    x coordinate, will be adjusted by frequency
    * @param yi    y coordinate, will be adjusted by frequency
    * @param seed the seed to use for the noise (used in place of { @link #getSeed()})
    * @param octaves   level of detail, from 1 to about 16 as a practical maximum
    * @param frequency a multiplier applied to the coordinates; lower values increase the size of features
    * @return noise between -1 and 1
    */
  def noiseWithOctaves(xi: Double, yi: Double, seed: Long, octaves: Int, frequency: Double): Double = {
    var x = xi
    var y = yi
    x *= frequency
    y *= frequency
    var sum = noiseWithSeed(x, y, seed)
    var amp = 1d
    var i = 1
    while ( {
      i < octaves
    }) {
      x += x
      y += y
      amp *= 0.5d
      sum += noiseWithSeed(x, y, seed + i) * amp

      {
        i += 1; i - 1
      }
    }
    sum / (amp * ((1 << Math.max(1, octaves)) - 1))
  }

  /**
    * 2D foam noise with the lowest, fastest level of detail. Uses the
    * seed <pre>12345L</pre> and does not change x, y, or z.
    *
    * @param x x coordinate
    * @param y y coordinate
    * @param z z coordinate
    * @return noise between -1 and 1, inclusive
    */
  def noise(x: Double, y: Double, z: Double): Double = noiseWithSeed(x, y, z, 12345L)

  /**
    * Gets foam noise with the lowest, fastest level of detail. Uses the given seed
    * and does not change x, y, or z.
    *
    * @param x    x coordinate
    * @param y    y coordinate
    * @param z    z coordinate
    * @param seed the seed to use for the noise (used in place of { @link #getSeed()})
    * @return noise between -1 and 1
    */
  def noiseWithSeed(x: Double, y: Double, z: Double, seed: Long): Double = {
    val p0 = x
    val p1 = x * -0.3333333333333333 + y * 0.9428090415820634
    val p2 = x * -0.3333333333333333 + y * -0.4714045207910317 + z * 0.816496580927726
    val p3 = x * -0.3333333333333333 + y * -0.4714045207910317 + z * -0.816496580927726
    var xin = p1
    var yin = p2
    var zin = p3
    val a = valueNoise(xin, yin, zin, seed)
    xin = p0
    yin = p2
    zin = p3
    val b = valueNoise(xin + a, yin, zin, seed + 0x9A827999FCEF3243L)
    xin = p0
    yin = p1
    zin = p3
    val c = valueNoise(xin + b, yin, zin, seed + 0x3504F333F9DE6486L)
    xin = p0
    yin = p1
    zin = p2
    val d = valueNoise(xin + c, yin, zin, seed + 0xCF876CCDF6CD96C9L)
    val result = (a + b + c + d) * 0.25
    val sharp = 0.75 * 3.3
    val diff = 0.5 - result
    val sign = NumberUtils.doubleToLongBits(diff) >> 63
    val one = sign | 1
    ((result + sign) / (Double.MinValue - sign + (result + sharp * diff) * one) - sign - sign) - 1.0
  }

  /**
    * Gets value noise with the lowest, fastest level of detail. Uses the given seed
    * and does not change x, y, or z. This has a different output range (0 to 1) than foam noise.
    *
    * @param xi    x coordinate
    * @param yi    y coordinate
    * @param zi    z coordinate
    * @param seed the seed to use for the noise (used in place of { @link #getSeed()})
    * @return noise between 0 and 1
    */
  def valueNoise(xi: Double, yi: Double, zi: Double, seed: Long): Double = {
    var x = xi
    var y = yi
    var z = zi
    val STEPX = 0xD1B54A32D192ED03L
    val STEPY = 0xABC98388FB8FAC03L
    val STEPZ = 0x8CB92BA72F3D8DD7L
    var xFloor = Math.floor(x).toLong
    x -= xFloor
    x *= x * (3 - 2 * x)
    var yFloor = Math.floor(y).toLong
    y -= yFloor
    y *= y * (3 - 2 * y)
    var zFloor = Math.floor(z).toLong
    z -= zFloor
    z *= z * (3 - 2 * z)
    xFloor *= STEPX
    yFloor *= STEPY
    zFloor *= STEPZ
    ((1 - z) * ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor, seed)) + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor, seed))) + z * ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor + STEPZ, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor + STEPZ, seed)) + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor + STEPZ, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, seed)))) * 5.421010862427522E-20 + 0.5 //0x1 p -64 + 0.5
  }

  /**
    * Constants are from harmonious numbers, essentially negative integer powers of specific irrational numbers times
    * (2 to the 64).
    *
    * @param x should be premultiplied by 0xD1B54A32D192ED03L
    * @param y should be premultiplied by 0xABC98388FB8FAC03L
    * @param z should be premultiplied by 0x8CB92BA72F3D8DD7L
    * @param si state, any long
    * @return a mediocre 64-bit hash
    */
  def hashPart(x: Long, y: Long, z: Long, si: Long) = {
    var s = si
    s ^= x ^ y ^ z
    val s2 = (s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L
    s2 ^ s2 >>> 25
  }

  ///**
  //  * Gets foam noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses
  //  * the given seed, and multiplies x, y, and z by frequency.
  //  *
  //  * @param x    x coordinate, will be adjusted by frequency
  //  * @param y    y coordinate, will be adjusted by frequency
  //  * @param z    z coordinate, will be adjusted by frequency
  //  * @param seed the seed to use for the noise (used in place of { @link #getSeed()})
  //  * @param octaves level of detail, from 1 to about 16 as a practical maximum
  //  * @return noise between -1 and 1
  //  */
  //def noiseWithOctaves(x: Double, y: Double, z: Double, seed: Long, octaves: Int, frequency: Double): Double = noiseWithOctaves(x, y, z, seed, octaves, frequency)

  /**
    * Gets foam noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses the given
    * seed instead of [[#getSeed()]], and multiplies x, y, and z by frequency.
    *
    * @param xi    x coordinate, will be adjusted by frequency
    * @param yi    y coordinate, will be adjusted by frequency
    * @param zi    z coordinate, will be adjusted by frequency
    * @param seed the seed to use for the noise (used in place of { @link #getSeed()})
    * @param octaves   level of detail, from 1 to about 16 as a practical maximum
    * @param frequency a multiplier applied to the coordinates; lower values increase the size of features
    * @return noise between -1 and 1
    */
  def noiseWithOctaves(xi: Double, yi: Double, zi: Double, seed: Long, octaves: Int, frequency: Double): Double = {
    var x = xi
    var y = yi
    var z = zi
    x *= frequency
    y *= frequency
    z *= frequency
    var sum = noiseWithSeed(x, y, z, seed)
    var amp = 1d
    var i = 1
    while ( {
      i < octaves
    }) {
      x += x
      y += y
      z += z
      amp *= 0.5d
      sum += noiseWithSeed(x, y, z, seed + i) * amp

      {
        i += 1; i - 1
      }
    }
    sum / (amp * ((1 << Math.max(1, octaves)) - 1))
  }

  /**
    * 4D foam noise with the lowest, fastest level of detail. Uses the
    * seed <pre>12345L</pre> and does not change x, y, z, or w.
    *
    * @param x x coordinate
    * @param y y coordinate
    * @param z z coordinate
    * @param w w coordinate
    * @return noise between -1 and 1, inclusive
    */
  def noise(x: Double, y: Double, z: Double, w: Double): Double = noiseWithSeed(x, y, z, w, 12345L)

  /**
    * Gets 4D foam noise with the lowest, fastest level of detail. Uses the given seed
    * and does not change x, y, z, or w.
    *
    * @param x    x coordinate
    * @param y    y coordinate
    * @param z    z coordinate
    * @param w    w coordinate
    * @param seed the seed to use for the noise (used in place of { @link #getSeed()})
    * @return noise between -1 and 1
    */
  def noiseWithSeed(x: Double, y: Double, z: Double, w: Double, seed: Long): Double = {
    val p0 = x
    val p1 = x * -0.25 + y * 0.9682458365518543
    val p2 = x * -0.25 + y * -0.3227486121839514 + z * 0.9128709291752769
    val p3 = x * -0.25 + y * -0.3227486121839514 + z * -0.45643546458763834 + w * 0.7905694150420949
    val p4 = x * -0.25 + y * -0.3227486121839514 + z * -0.45643546458763834 + w * -0.7905694150420947
    var xin = p1
    var yin = p2
    var zin = p3
    var win = p4
    val a = valueNoise(xin, yin, zin, win, seed)
    xin = p0
    yin = p2
    zin = p3
    win = p4
    val b = valueNoise(xin + a, yin, zin, win, seed + 0x9A827999FCEF3243L)
    xin = p0
    yin = p1
    zin = p3
    win = p4
    val c = valueNoise(xin + b, yin, zin, win, seed + 0x3504F333F9DE6486L)
    xin = p0
    yin = p1
    zin = p2
    win = p4
    val d = valueNoise(xin + c, yin, zin, win, seed + 0xCF876CCDF6CD96C9L)
    xin = p0
    yin = p1
    zin = p2
    win = p3
    val e = valueNoise(xin + d, yin, zin, win, seed + 0x6A09E667F3BCC90CL)
    val result = (a + b + c + d + e) * 0.2
    val sharp = 0.75 * 4.4
    val diff = 0.5 - result
    val sign = NumberUtils.doubleToLongBits(diff) >> 63
    val one = sign | 1
    ((result + sign) / (Double.MinValue - sign + (result + sharp * diff) * one) - sign - sign) - 1.0
  }

  /**
    * Gets 4D value noise with the lowest, fastest level of detail. Uses the given seed
    * and does not change x, y, z, or w. This has a different output range (0 to 1) than foam noise.
    *
    * @param xi    x coordinate
    * @param yi    y coordinate
    * @param zi    z coordinate
    * @param wi    w coordinate
    * @param seed the seed to use for the noise (used in place of { @link #getSeed()})
    * @return noise between 0 and 1
    */
  def valueNoise(xi: Double, yi: Double, zi: Double, wi: Double, seed: Long): Double = {
    var x = xi
    var y = yi
    var z = zi
    var w = wi
    val STEPX = 0xDB4F0B9175AE2165L
    val STEPY = 0xBBE0563303A4615FL
    val STEPZ = 0xA0F2EC75A1FE1575L
    val STEPW = 0x89E182857D9ED689L
    var xFloor = Math.floor(x).toLong
    x -= xFloor
    x *= x * (3 - 2 * x)
    var yFloor = Math.floor(y).toLong
    y -= yFloor
    y *= y * (3 - 2 * y)
    var zFloor = Math.floor(z).toLong
    z -= zFloor
    z *= z * (3 - 2 * z)
    var wFloor = Math.floor(w).toLong
    w -= wFloor
    w *= w * (3 - 2 * w)
    xFloor *= STEPX
    yFloor *= STEPY
    zFloor *= STEPZ
    wFloor *= STEPW
    ((1 - w) * ((1 - z) * ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor, wFloor, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor, wFloor, seed)) + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor, wFloor, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor, seed))) + z * ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor + STEPZ, wFloor, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor, seed)) + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor, seed)))) + (w * ((1 - z) * ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor, wFloor + STEPW, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor, wFloor + STEPW, seed)) + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor, wFloor + STEPW, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor, wFloor + STEPW, seed))) + z * ((1 - y) * ((1 - x) * hashPart(xFloor, yFloor, zFloor + STEPZ, wFloor + STEPW, seed) + x * hashPart(xFloor + STEPX, yFloor, zFloor + STEPZ, wFloor + STEPW, seed)) + y * ((1 - x) * hashPart(xFloor, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, seed) + x * hashPart(xFloor + STEPX, yFloor + STEPY, zFloor + STEPZ, wFloor + STEPW, seed)))))) * 5.421010862427522E-20 + 0.5 //0x1 p -64 + 0.5
  }

  /**
    * Constants are from harmonious numbers, essentially negative integer powers of specific irrational numbers times
    * (2 to the 64).
    *
    * @param x should be premultiplied by 0xDB4F0B9175AE2165L
    * @param y should be premultiplied by 0xBBE0563303A4615FL
    * @param z should be premultiplied by 0xA0F2EC75A1FE1575L
    * @param w should be premultiplied by 0x89E182857D9ED689L
    * @param si state, any long
    * @return a mediocre 64-bit hash
    */
  def hashPart(x: Long, y: Long, z: Long, w: Long, si: Long) = {
    var s = si
    s ^= x ^ y ^ z ^ w
    val s2 = (s ^ (s << 47 | s >>> 17) ^ (s << 23 | s >>> 41)) * 0xF1357AEA2E62A9C5L + 0x9E3779B97F4A7C15L
    s2 ^ s2 >>> 25
  }

  ///**
  //  * Gets 4D foam noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses
  //  * the given seed, and multiplies x, y, z, and w by frequency.
  //  *
  //  * @param x    x coordinate, will be adjusted by frequency
  //  * @param y    y coordinate, will be adjusted by frequency
  //  * @param z    z coordinate, will be adjusted by frequency
  //  * @param w    w coordinate, will be adjusted by frequency
  //  * @param seed the seed to use for the noise (used in place of { @link #getSeed()})
  //  * @param octaves level of detail, from 1 to about 16 as a practical maximum
  //  * @return noise between -1 and 1
  //  */
  //def noiseWithOctaves(x: Double, y: Double, z: Double, w: Double, seed: Long, octaves: Int, frequency: Double): Double = noiseWithOctaves(x, y, z, w, seed, octaves, frequency)

  /**
    * Gets 4D foam noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses the given
    * seed instead of [[#getSeed()]], and multiplies x, y, z, and w by frequency.
    *
    * @param xi    x coordinate, will be adjusted by frequency
    * @param yi    y coordinate, will be adjusted by frequency
    * @param zi    z coordinate, will be adjusted by frequency
    * @param wi    w coordinate, will be adjusted by frequency
    * @param seed the seed to use for the noise (used in place of { @link #getSeed()})
    * @param octaves   level of detail, from 1 to about 16 as a practical maximum
    * @param frequency a multiplier applied to the coordinates; lower values increase the size of features
    * @return noise between -1 and 1
    */
  def noiseWithOctaves(xi: Double, yi: Double, zi: Double, wi: Double, seed: Long, octaves: Int, frequency: Double): Double = {
    var x = xi
    var y = yi
    var z = zi
    var w = wi
    x *= frequency
    y *= frequency
    z *= frequency
    w *= frequency
    var sum = noiseWithSeed(x, y, z, w, seed)
    var amp = 1d
    var i = 1
    while ( {
      i < octaves
    }) {
      x += x
      y += y
      z += z
      w += w
      amp *= 0.5d
      sum += noiseWithSeed(x, y, z, w, seed + i) * amp

      {
        i += 1; i - 1
      }
    }
    sum / (amp * ((1 << Math.max(1, octaves)) - 1))
  }
}

/**
  * Use the same seed for any noise that should be continuous (smooth) across calls to nearby points.
  */
class FoamNoiseStandalone(var seed: Long=1234567890L, val frequencyInitial: Double=1d) {

  /**
    * You will probably need to change the frequency to be much closer or much further from 0, depending on the scale
    * of your noise. The default is 1.0 .
    */
  protected var frequency: Double = frequencyInitial

  /**
    * Gets foam noise with the lowest, fastest level of detail. Uses
    * [[#getSeed()]] and multiplies x and y by frequency.
    *
    * @param x x coordinate, will be adjusted by frequency
    * @param y y coordinate, will be adjusted by frequency
    * @return noise between -1 and 1, inclusive
    */
  def getNoise(x: Double, y: Double): Double = FoamNoiseStandalone.noiseWithSeed(x * frequency, y * frequency, seed)

  /**
    * Gets foam noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses
    * [[#getSeed()]] and multiplies x and y by frequency.
    *
    * @param x       x coordinate, will be adjusted by frequency
    * @param y       y coordinate, will be adjusted by frequency
    * @param octaves level of detail, from 1 to about 16 as a practical maximum
    * @return noise between -1 and 1
    */
  def getNoiseWithOctaves(x: Double, y: Double, octaves: Int, frequency: Double): Double = FoamNoiseStandalone.noiseWithOctaves(x, y, seed, octaves, frequency)

  /**
    * Gets foam noise with the lowest, fastest level of detail. Uses
    * [[#getSeed()]] and multiplies x, y, and z by frequency.
    *
    * @param x x coordinate, will be adjusted by frequency
    * @param y y coordinate, will be adjusted by frequency
    * @param z z coordinate, will be adjusted by frequency
    * @return noise between -1 and 1, inclusive
    */
  def getNoise(x: Double, y: Double, z: Double): Double = FoamNoiseStandalone.noiseWithSeed(x * frequency, y * frequency, z * frequency, seed)

  /**
    * Gets foam noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses
    * [[#getSeed()]] and multiplies x, y, and z by frequency.
    *
    * @param x       x coordinate, will be adjusted by frequency
    * @param y       y coordinate, will be adjusted by frequency
    * @param z       z coordinate, will be adjusted by frequency
    * @param octaves level of detail, from 1 to about 16 as a practical maximum
    * @return noise between -1 and 1
    */
  def getNoiseWithOctaves(x: Double, y: Double, z: Double, octaves: Int): Double = FoamNoiseStandalone.noiseWithOctaves(x, y, z, seed, octaves, 1.0)

  /**
    * Gets 4D foam noise with the lowest, fastest level of detail. Uses
    * [[#getSeed()]] and multiplies x, y, z, and w by frequency.
    *
    * @param x x coordinate, will be adjusted by frequency
    * @param y y coordinate, will be adjusted by frequency
    * @param z z coordinate, will be adjusted by frequency
    * @param w w coordinate, will be adjusted by frequency
    * @return noise between -1 and 1, inclusive
    */
  def getNoise(x: Double, y: Double, z: Double, w: Double): Double = FoamNoiseStandalone.noiseWithSeed(x * frequency, y * frequency, z * frequency, w * frequency, seed)

  /**
    * Gets 4D foam noise with variable level of detail, with higher octaves producing more detail, more slowly. Uses
    * [[#getSeed()]] and multiplies x, y, z, and w by frequency.
    *
    * @param x       x coordinate, will be adjusted by frequency
    * @param y       y coordinate, will be adjusted by frequency
    * @param z       z coordinate, will be adjusted by frequency
    * @param w       w coordinate, will be adjusted by frequency
    * @param octaves level of detail, from 1 to about 16 as a practical maximum
    * @return noise between -1 and 1
    */
  def getNoiseWithOctaves(x: Double, y: Double, z: Double, w: Double, octaves: Int): Double = FoamNoiseStandalone.noiseWithOctaves(x, y, z, w, seed, octaves, 1.0)

  def setSeed(seed: Long): Unit = {
    this.seed = seed
  }

  def getSeed: Long = seed

  def getFrequency: Double = frequency

  def setFrequency(f: Double): Unit = frequency = f

  override def toString: String = "FoamNoiseStandalone{seed=" + seed + "}"

  override def equals(o: Any): Boolean = {
    false
    // TODO: ????
    //if (this eq o) return true
    //if (o == null || (getClass ne o.getClass)) return false
    //val that = o.asInstanceOf[FoamNoiseStandalone]
    //if (seed != that.seed) return false
    //Double.compare(that.frequency, frequency) == 0
  }

  override def hashCode: Int = {
    val bits = NumberUtils.doubleToLongBits(frequency) * 421L
    (seed ^ seed >>> 32 ^ bits ^ bits >>> 32).toInt
  }
}