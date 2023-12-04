/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
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
 ******************************************************************************/

package com.github.yellowstonegames.smooth;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import static com.badlogic.gdx.graphics.g2d.SpriteBatch.*;

/** Holds the geometry, color, and texture information for drawing 2D sprites using {@link Batch}. This is exactly the
 * same as {@link com.badlogic.gdx.graphics.g2d.Sprite} except that it is meant to be extended, and so it has no private
 * or package-private variables or methods. You're welcome.
 * <br>
 * A ParentSprite has a position and a size given as width and height. The position is relative to the origin of the
 * coordinate system specified via {@link Batch#begin()} and the respective matrices. A ParentSprite is always
 * rectangular and its position (x, y) are located in the bottom left corner of that rectangle. A ParentSprite also has
 * an origin around which rotations and scaling are performed (that is, the origin is not modified by rotation and
 * scaling). The origin is given relative to the bottom left corner of the ParentSprite, its position.
 * <br>
 * Classes that extend this have full access to fields in Sprite that were private or package-private. However, they
 * will not have access to the package-private fields in the parent TextureRegion class, so you'll need to use getters
 * and setters for those. If you're copying code from Sprite, it's usually a good idea to statically import constants
 * from SpriteBatch: <code>import static com.badlogic.gdx.graphics.g2d.SpriteBatch.*;</code>
 * @author mzechner
 * @author Nathan Sweet */
public class ParentSprite extends TextureRegion {
	public static final int VERTEX_SIZE = 2 + 1 + 2;
	public static final int SPRITE_SIZE = 4 * VERTEX_SIZE;

	protected final float[] vertices = new float[SPRITE_SIZE];
	protected final Color color = new Color(1f, 1f, 1f, 1f);
	protected float x, y;
	public float width, height;
	protected float originX, originY;
	protected float rotation;
	protected float scaleX = 1f, scaleY = 1f;
	protected boolean dirty = true;
	protected Rectangle bounds;

	/** Creates an uninitialized sprite. The sprite will need a texture region and bounds set before it can be drawn. */
	public ParentSprite() {
		setColor(1f, 1f, 1f, 1f);
	}

	/** Creates a sprite with width, height, and texture region equal to the size of the texture. */
	public ParentSprite(Texture texture) {
		this(texture, 0, 0, texture.getWidth(), texture.getHeight());
	}

	/** Creates a sprite with width, height, and texture region equal to the specified size. The texture region's upper left corner
	 * will be 0,0.
	 * @param srcWidth The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param srcHeight The height of the texture region. May be negative to flip the sprite when drawn. */
	public ParentSprite(Texture texture, int srcWidth, int srcHeight) {
		this(texture, 0, 0, srcWidth, srcHeight);
	}

	/** Creates a sprite with width, height, and texture region equal to the specified size.
	 * @param srcWidth The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param srcHeight The height of the texture region. May be negative to flip the sprite when drawn. */
	public ParentSprite(Texture texture, int srcX, int srcY, int srcWidth, int srcHeight) {
		if (texture == null) throw new IllegalArgumentException("texture cannot be null.");
		setTexture(texture);
		setRegion(srcX, srcY, srcWidth, srcHeight);
		setColor(1f, 1f, 1f, 1f);
		setSize(Math.abs(srcWidth), Math.abs(srcHeight));
		setOrigin(width / 2, height / 2);
	}

	// Note the region is copied.
	/** Creates a sprite based on a specific TextureRegion, the new sprite's region is a copy of the parameter region - altering
	 * one does not affect the other */
	public ParentSprite(TextureRegion region) {
		setRegion(region);
		setColor(1f, 1f, 1f, 1f);
		setSize(region.getRegionWidth(), region.getRegionHeight());
		setOrigin(width / 2, height / 2);
	}

	/** Creates a sprite with width, height, and texture region equal to the specified size, relative to specified sprite's texture
	 * region.
	 * @param srcWidth The width of the texture region. May be negative to flip the sprite when drawn.
	 * @param srcHeight The height of the texture region. May be negative to flip the sprite when drawn. */
	public ParentSprite(TextureRegion region, int srcX, int srcY, int srcWidth, int srcHeight) {
		setRegion(region, srcX, srcY, srcWidth, srcHeight);
		setColor(1f, 1f, 1f, 1f);
		setSize(Math.abs(srcWidth), Math.abs(srcHeight));
		setOrigin(width / 2, height / 2);
	}

	/** Creates a sprite that is a copy in every way of the specified sprite. */
	public ParentSprite(ParentSprite sprite) {
		set(sprite);
	}

	/** Make this sprite a copy in every way of the specified sprite */
	public void set (ParentSprite sprite) {
		if (sprite == null) throw new IllegalArgumentException("sprite cannot be null.");
		System.arraycopy(sprite.vertices, 0, vertices, 0, SPRITE_SIZE);
		setTexture(sprite.getTexture());
		setU(sprite.getU());
		setV(sprite.getV());
		setU2(sprite.getU2());
		setV2(sprite.getV2());
		x = sprite.x;
		y = sprite.y;
		width = sprite.width;
		height = sprite.height;
		setRegionWidth(sprite.getRegionWidth());
		setRegionHeight(sprite.getRegionHeight());
		originX = sprite.originX;
		originY = sprite.originY;
		rotation = sprite.rotation;
		scaleX = sprite.scaleX;
		scaleY = sprite.scaleY;
		color.set(sprite.color);
        if (sprite.bounds == null) {
            bounds = null;
        } else {
            if(bounds == null) bounds = new Rectangle(sprite.bounds);
            else bounds.set(sprite.bounds);
        }
        dirty = sprite.dirty;
	}

	/** Sets the position and size of the sprite when drawn, before scaling and rotation are applied. If origin, rotation, or scale
	 * are changed, it is slightly more efficient to set the bounds after those operations. */
	public void setBounds (float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;

		if (dirty) return;
		if (rotation != 0 || scaleX != 1 || scaleY != 1) {
			dirty = true;
			return;
		}

		float x2 = x + width;
		float y2 = y + height;
		float[] vertices = this.vertices;
		vertices[X1] = x;
		vertices[Y1] = y;

		vertices[X2] = x;
		vertices[Y2] = y2;

		vertices[X3] = x2;
		vertices[Y3] = y2;

		vertices[X4] = x2;
		vertices[Y4] = y;
	}

	/** Sets the size of the sprite when drawn, before scaling and rotation are applied. If origin, rotation, or scale are changed,
	 * it is slightly more efficient to set the size after those operations. If both position and size are to be changed, it is
	 * better to use {@link #setBounds(float, float, float, float)}. */
	public void setSize (float width, float height) {
		this.width = width;
		this.height = height;

		if (dirty) return;
		if (rotation != 0 || scaleX != 1 || scaleY != 1) {
			dirty = true;
			return;
		}

		float x2 = x + width;
		float y2 = y + height;
		float[] vertices = this.vertices;
		vertices[X1] = x;
		vertices[Y1] = y;

		vertices[X2] = x;
		vertices[Y2] = y2;

		vertices[X3] = x2;
		vertices[Y3] = y2;

		vertices[X4] = x2;
		vertices[Y4] = y;
	}

	/** Sets the position where the sprite will be drawn. If origin, rotation, or scale are changed, it is slightly more efficient
	 * to set the position after those operations. If both position and size are to be changed, it is better to use
	 * {@link #setBounds(float, float, float, float)}. */
	public void setPosition (float x, float y) {
		this.x = x;
		this.y = y;

		if (dirty) return;
		if (rotation != 0 || scaleX != 1 || scaleY != 1) {
			dirty = true;
			return;
		}

		float x2 = x + width;
		float y2 = y + height;
		float[] vertices = this.vertices;
		vertices[X1] = x;
		vertices[Y1] = y;

		vertices[X2] = x;
		vertices[Y2] = y2;

		vertices[X3] = x2;
		vertices[Y3] = y2;

		vertices[X4] = x2;
		vertices[Y4] = y;
	}

	/** Sets the position where the sprite will be drawn, relative to its current origin. */
	public void setOriginBasedPosition (float x, float y) {
		setPosition(x - this.originX, y - this.originY);
	}

	/** Sets the x position where the sprite will be drawn. If origin, rotation, or scale are changed, it is slightly more
	 * efficient to set the position after those operations. If both position and size are to be changed, it is better to use
	 * {@link #setBounds(float, float, float, float)}. */
	public void setX (float x) {
		this.x = x;

		if (dirty) return;
		if (rotation != 0 || scaleX != 1 || scaleY != 1) {
			dirty = true;
			return;
		}

		float x2 = x + width;
		float[] vertices = this.vertices;
		vertices[X1] = x;
		vertices[X2] = x;
		vertices[X3] = x2;
		vertices[X4] = x2;
	}

	/** Sets the y position where the sprite will be drawn. If origin, rotation, or scale are changed, it is slightly more
	 * efficient to set the position after those operations. If both position and size are to be changed, it is better to use
	 * {@link #setBounds(float, float, float, float)}. */
	public void setY (float y) {
		this.y = y;

		if (dirty) return;
		if (rotation != 0 || scaleX != 1 || scaleY != 1) {
			dirty = true;
			return;
		}

		float y2 = y + height;
		float[] vertices = this.vertices;
		vertices[Y1] = y;
		vertices[Y2] = y2;
		vertices[Y3] = y2;
		vertices[Y4] = y;
	}

	/** Sets the x position so that it is centered on the given x parameter */
	public void setCenterX (float x) {
		setX(x - width / 2);
	}

	/** Sets the y position so that it is centered on the given y parameter */
	public void setCenterY (float y) {
		setY(y - height / 2);
	}

	/** Sets the position so that the sprite is centered on (x, y) */
	public void setCenter (float x, float y) {
		setPosition(x - width / 2, y - height / 2);
	}

	/** Sets the x position relative to the current position where the sprite will be drawn. If origin, rotation, or scale are
	 * changed, it is slightly more efficient to translate after those operations. */
	public void translateX (float xAmount) {
		this.x += xAmount;

		if (dirty) return;
		if (rotation != 0 || scaleX != 1 || scaleY != 1) {
			dirty = true;
			return;
		}

		float[] vertices = this.vertices;
		vertices[X1] += xAmount;
		vertices[X2] += xAmount;
		vertices[X3] += xAmount;
		vertices[X4] += xAmount;
	}

	/** Sets the y position relative to the current position where the sprite will be drawn. If origin, rotation, or scale are
	 * changed, it is slightly more efficient to translate after those operations. */
	public void translateY (float yAmount) {
		y += yAmount;

		if (dirty) return;
		if (rotation != 0 || scaleX != 1 || scaleY != 1) {
			dirty = true;
			return;
		}

		float[] vertices = this.vertices;
		vertices[Y1] += yAmount;
		vertices[Y2] += yAmount;
		vertices[Y3] += yAmount;
		vertices[Y4] += yAmount;
	}

	/** Sets the position relative to the current position where the sprite will be drawn. If origin, rotation, or scale are
	 * changed, it is slightly more efficient to translate after those operations. */
	public void translate (float xAmount, float yAmount) {
		x += xAmount;
		y += yAmount;

		if (dirty) return;
		if (rotation != 0 || scaleX != 1 || scaleY != 1) {
			dirty = true;
			return;
		}

		float[] vertices = this.vertices;
		vertices[X1] += xAmount;
		vertices[Y1] += yAmount;

		vertices[X2] += xAmount;
		vertices[Y2] += yAmount;

		vertices[X3] += xAmount;
		vertices[Y3] += yAmount;

		vertices[X4] += xAmount;
		vertices[Y4] += yAmount;
	}


	/** Sets the color used to tint this sprite. Default is {@link Color#WHITE}. */
	public void setColor (Color tint) {
		color.set(tint);
		float color = tint.toFloatBits();
		float[] vertices = this.vertices;
		vertices[C1] = color;
		vertices[C2] = color;
		vertices[C3] = color;
		vertices[C4] = color;
	}

	/** Sets the alpha portion of the color used to tint this sprite. */
	public void setAlpha (float a) {
		color.a = a;
		float color = this.color.toFloatBits();
		vertices[C1] = color;
		vertices[C2] = color;
		vertices[C3] = color;
		vertices[C4] = color;
	}

	/** @see #setColor(Color) */
	public void setColor (float r, float g, float b, float a) {
		color.set(r, g, b, a);
		float color = this.color.toFloatBits();
		float[] vertices = this.vertices;
		vertices[C1] = color;
		vertices[C2] = color;
		vertices[C3] = color;
		vertices[C4] = color;
	}

	/** Sets the color of this sprite, expanding the alpha from 0-254 to 0-255.
	 * @see #setColor(Color)
	 * @see Color#toFloatBits() */
	public void setPackedColor (float packedColor) {
		Color.abgr8888ToColor(color, packedColor);
		float[] vertices = this.vertices;
		vertices[C1] = packedColor;
		vertices[C2] = packedColor;
		vertices[C3] = packedColor;
		vertices[C4] = packedColor;
	}

	/** Sets the origin in relation to the sprite's position for scaling and rotation. */
	public void setOrigin (float originX, float originY) {
		this.originX = originX;
		this.originY = originY;
		dirty = true;
	}

	/** Place origin in the center of the sprite */
	public void setOriginCenter () {
		this.originX = width / 2;
		this.originY = height / 2;
		dirty = true;
	}

	/** Sets the rotation of the sprite in degrees. Rotation is centered on the origin set in {@link #setOrigin(float, float)} */
	public void setRotation (float degrees) {
		this.rotation = degrees;
		dirty = true;
	}

	/** @return the rotation of the sprite in degrees */
	public float getRotation () {
		return rotation;
	}

	/** Sets the sprite's rotation in degrees relative to the current rotation. Rotation is centered on the origin set in
	 * {@link #setOrigin(float, float)} */
	public void rotate (float degrees) {
		if (degrees == 0) return;
		rotation += degrees;
		dirty = true;
	}

	/** Rotates this sprite 90 degrees in-place by rotating the texture coordinates. This rotation is unaffected by
	 * {@link #setRotation(float)} and {@link #rotate(float)}. */
	public void rotate90 (boolean clockwise) {
		float[] vertices = this.vertices;

		if (clockwise) {
			float temp = vertices[V1];
			vertices[V1] = vertices[V4];
			vertices[V4] = vertices[V3];
			vertices[V3] = vertices[V2];
			vertices[V2] = temp;

			temp = vertices[U1];
			vertices[U1] = vertices[U4];
			vertices[U4] = vertices[U3];
			vertices[U3] = vertices[U2];
			vertices[U2] = temp;
		} else {
			float temp = vertices[V1];
			vertices[V1] = vertices[V2];
			vertices[V2] = vertices[V3];
			vertices[V3] = vertices[V4];
			vertices[V4] = temp;

			temp = vertices[U1];
			vertices[U1] = vertices[U2];
			vertices[U2] = vertices[U3];
			vertices[U3] = vertices[U4];
			vertices[U4] = temp;
		}
	}

	/** Sets the sprite's scale for both X and Y uniformly. The sprite scales out from the origin. This will not affect the values
	 * returned by {@link #getWidth()} and {@link #getHeight()} */
	public void setScale (float scaleXY) {
		this.scaleX = scaleXY;
		this.scaleY = scaleXY;
		dirty = true;
	}

	/** Sets the sprite's scale for both X and Y. The sprite scales out from the origin. This will not affect the values returned
	 * by {@link #getWidth()} and {@link #getHeight()} */
	public void setScale (float scaleX, float scaleY) {
		this.scaleX = scaleX;
		this.scaleY = scaleY;
		dirty = true;
	}

	/** Sets the sprite's scale relative to the current scale. for example: original scale 2 -> sprite.scale(4) -> final scale 6.
	 * The sprite scales out from the origin. This will not affect the values returned by {@link #getWidth()} and
	 * {@link #getHeight()} */
	public void scale (float amount) {
		this.scaleX += amount;
		this.scaleY += amount;
		dirty = true;
	}

	/** Returns the packed vertices, colors, and texture coordinates for this sprite. */
	public float[] getVertices () {
		if (dirty) {
			dirty = false;

			float[] vertices = this.vertices;
			float localX = -originX;
			float localY = -originY;
			float localX2 = localX + width;
			float localY2 = localY + height;
			float worldOriginX = this.x - localX;
			float worldOriginY = this.y - localY;
			if (scaleX != 1 || scaleY != 1) {
				localX *= scaleX;
				localY *= scaleY;
				localX2 *= scaleX;
				localY2 *= scaleY;
			}
			if (rotation != 0) {
				final float cos = MathUtils.cosDeg(rotation);
				final float sin = MathUtils.sinDeg(rotation);
				final float localXCos = localX * cos;
				final float localXSin = localX * sin;
				final float localYCos = localY * cos;
				final float localYSin = localY * sin;
				final float localX2Cos = localX2 * cos;
				final float localX2Sin = localX2 * sin;
				final float localY2Cos = localY2 * cos;
				final float localY2Sin = localY2 * sin;

				final float x1 = localXCos - localYSin + worldOriginX;
				final float y1 = localYCos + localXSin + worldOriginY;
				vertices[X1] = x1;
				vertices[Y1] = y1;

				final float x2 = localXCos - localY2Sin + worldOriginX;
				final float y2 = localY2Cos + localXSin + worldOriginY;
				vertices[X2] = x2;
				vertices[Y2] = y2;

				final float x3 = localX2Cos - localY2Sin + worldOriginX;
				final float y3 = localY2Cos + localX2Sin + worldOriginY;
				vertices[X3] = x3;
				vertices[Y3] = y3;

				vertices[X4] = x1 + (x3 - x2);
				vertices[Y4] = y3 - (y2 - y1);
			} else {
				final float x1 = localX + worldOriginX;
				final float y1 = localY + worldOriginY;
				final float x2 = localX2 + worldOriginX;
				final float y2 = localY2 + worldOriginY;

				vertices[X1] = x1;
				vertices[Y1] = y1;

				vertices[X2] = x1;
				vertices[Y2] = y2;

				vertices[X3] = x2;
				vertices[Y3] = y2;

				vertices[X4] = x2;
				vertices[Y4] = y1;
			}
		}
		return vertices;
	}

	/** Returns the bounding axis aligned {@link Rectangle} that bounds this sprite. The rectangles x and y coordinates describe
	 * its bottom left corner. If you change the position or size of the sprite, you have to fetch the triangle again for it to be
	 * recomputed.
	 *
	 * @return the bounding Rectangle */
	public Rectangle getBoundingRectangle () {
		final float[] vertices = getVertices();

		float minx = vertices[X1];
		float miny = vertices[Y1];
		float maxx = vertices[X1];
		float maxy = vertices[Y1];

		minx = Math.min(minx, vertices[X2]);
		minx = Math.min(minx, vertices[X3]);
		minx = Math.min(minx, vertices[X4]);

		maxx = Math.max(maxx, vertices[X2]);
		maxx = Math.max(maxx, vertices[X3]);
		maxx = Math.max(maxx, vertices[X4]);

		miny = Math.min(miny, vertices[Y2]);
		miny = Math.min(miny, vertices[Y3]);
		miny = Math.min(miny, vertices[Y4]);

		maxy = Math.max(maxy, vertices[Y2]);
		maxy = Math.max(maxy, vertices[Y3]);
		maxy = Math.max(maxy, vertices[Y4]);

		if (bounds == null) bounds = new Rectangle();
		bounds.x = minx;
		bounds.y = miny;
		bounds.width = maxx - minx;
		bounds.height = maxy - miny;
		return bounds;
	}

	public void draw (Batch batch) {
		batch.draw(getTexture(), getVertices(), 0, SPRITE_SIZE);
	}

	public void draw (Batch batch, float alphaModulation) {
		float oldAlpha = getColor().a;
		setAlpha(oldAlpha * alphaModulation);
		draw(batch);
		setAlpha(oldAlpha);
	}

	public float getX () {
		return x;
	}

	public float getY () {
		return y;
	}

	/** @return the width of the sprite, not accounting for scale. */
	public float getWidth () {
		return width;
	}

	/** @return the height of the sprite, not accounting for scale. */
	public float getHeight () {
		return height;
	}

	/** The origin influences {@link #setPosition(float, float)}, {@link #setRotation(float)} and the expansion direction of
	 * scaling {@link #setScale(float, float)} */
	public float getOriginX () {
		return originX;
	}

	/** The origin influences {@link #setPosition(float, float)}, {@link #setRotation(float)} and the expansion direction of
	 * scaling {@link #setScale(float, float)} */
	public float getOriginY () {
		return originY;
	}

	/** X scale of the sprite, independent of size set by {@link #setSize(float, float)} */
	public float getScaleX () {
		return scaleX;
	}

	/** Y scale of the sprite, independent of size set by {@link #setSize(float, float)} */
	public float getScaleY () {
		return scaleY;
	}

	/** Returns the color of this sprite. If the returned instance is manipulated, {@link #setColor(Color)} must be called
	 * afterward. */
	public Color getColor () {
		return color;
	}

	public void setRegion (float u, float v, float u2, float v2) {
		super.setRegion(u, v, u2, v2);

		float[] vertices = ParentSprite.this.vertices;
		vertices[U1] = u;
		vertices[V1] = v2;

		vertices[U2] = u;
		vertices[V2] = v;

		vertices[U3] = u2;
		vertices[V3] = v;

		vertices[U4] = u2;
		vertices[V4] = v2;
	}

	public void setU (float u) {
		super.setU(u);
		vertices[U1] = u;
		vertices[U2] = u;
	}

	public void setV (float v) {
		super.setV(v);
		vertices[V2] = v;
		vertices[V3] = v;
	}

	public void setU2 (float u2) {
		super.setU2(u2);
		vertices[U3] = u2;
		vertices[U4] = u2;
	}

	public void setV2 (float v2) {
		super.setV2(v2);
		vertices[V1] = v2;
		vertices[V4] = v2;
	}

	/** Set the sprite's flip state regardless of current condition
	 * @param x the desired horizontal flip state
	 * @param y the desired vertical flip state */
	public void setFlip (boolean x, boolean y) {
		boolean performX = false;
		boolean performY = false;
		if (isFlipX() != x) {
			performX = true;
		}
		if (isFlipY() != y) {
			performY = true;
		}
		flip(performX, performY);
	}

	/** boolean parameters x,y are not setting a state, but performing a flip
	 * @param x perform horizontal flip
	 * @param y perform vertical flip */
	public void flip (boolean x, boolean y) {
		super.flip(x, y);
		float[] vertices = this.vertices;
		if (x) {
			float temp = vertices[U1];
			vertices[U1] = vertices[U3];
			vertices[U3] = temp;
			temp = vertices[U2];
			vertices[U2] = vertices[U4];
			vertices[U4] = temp;
		}
		if (y) {
			float temp = vertices[V1];
			vertices[V1] = vertices[V3];
			vertices[V3] = temp;
			temp = vertices[V2];
			vertices[V2] = vertices[V4];
			vertices[V4] = temp;
		}
	}

	public void scroll (float xAmount, float yAmount) {
		float[] vertices = this.vertices;
		if (xAmount != 0) {
			float u = (vertices[U1] + xAmount) % 1;
			float u2 = u + width / getTexture().getWidth();
			this.setU(u);
			this.setU2(u2);
			vertices[U1] = u;
			vertices[U2] = u;
			vertices[U3] = u2;
			vertices[U4] = u2;
		}
		if (yAmount != 0) {
			float v = (vertices[V2] + yAmount) % 1;
			float v2 = v + height / getTexture().getHeight();
			this.setV(v);
			this.setV2(v2);
			vertices[V1] = v2;
			vertices[V2] = v;
			vertices[V3] = v;
			vertices[V4] = v2;
		}
	}
}
