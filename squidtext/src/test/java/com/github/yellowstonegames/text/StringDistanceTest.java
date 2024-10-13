/* Copyright (c) 2012 Kevin L. Stern
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.yellowstonegames.text;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test class for StringDistance.
 * 
 * @author Kevin L. Stern
 */
public class StringDistanceTest {
  @Test
  public void test() {
    Assert.assertEquals(7, new StringDistance(1, 1, 1, 1)
        .distance("NawKtYu", ""));

    Assert.assertEquals(7, new StringDistance(1, 1, 1, 1)
        .distance("", "NawKtYu"));

    Assert.assertEquals(0, new StringDistance(1, 1, 1, 1)
        .distance("NawKtYu", "NawKtYu"));

    Assert.assertEquals(6, new StringDistance(1, 1, 1, 1)
        .distance("NawKtYu", "tKNwYua"));

    Assert.assertEquals(1, new StringDistance(1, 1, 1, 1)
        .distance("Jdc", "dJc"));

    Assert.assertEquals(5, new StringDistance(1, 1, 1, 1)
        .distance("sUzSOwx", "zsSxUwO"));

    Assert.assertEquals(7, new StringDistance(1, 1, 1, 1)
        .distance("eOqoHAta", "tAeaqHoO"));

    Assert.assertEquals(1, new StringDistance(1, 1, 1, 1)
        .distance("glSbo", "lgSbo"));

    Assert.assertEquals(4, new StringDistance(1, 1, 1, 1)
        .distance("NJtQKcJE", "cJEtQKJN"));

    Assert.assertEquals(5, new StringDistance(1, 1, 1, 1)
        .distance("GitIEVs", "EGItVis"));

    Assert.assertEquals(4, new StringDistance(1, 1, 1, 1)
        .distance("MiWK", "WKiM"));
  }

  @Test
  public void testCosts() {
    /*
     * Test replace cost.
     */
    Assert.assertEquals(1, new StringDistance(100, 100, 1, 100)
        .distance("a", "b"));
    /*
     * Test swap cost.
     */
    Assert.assertEquals(200,
                        new StringDistance(100, 100, 100, 200)
                            .distance("ab", "ba"));
    /*
     * Test delete cost.
     */
    Assert.assertEquals(1, new StringDistance(1, 100, 100, 100)
        .distance("aa", "a"));
    /*
     * Test insert cost.
     */
    Assert.assertEquals(1, new StringDistance(100, 1, 100, 100)
        .distance("a", "aa"));
  }

  @Test
  public void testInvalidCosts() {
    try {
      new StringDistance(1, 1, 1, 0);
      Assert.fail();
    } catch (IllegalArgumentException e) {

    }
  }
}