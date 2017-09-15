/*
 * MIT License
 *
 * Copyright (c) 2017 Benjamin K
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.elytradev.architecture.client.render.texture;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

public abstract class TextureBase implements ITexture {

    public ResourceLocation location;
    public int tintIndex;
    public double red = 1, green = 1, blue = 1;
    public boolean isEmissive;
    public boolean isProjected;

    public static Sprite fromSprite(TextureAtlasSprite icon) {
        return new Sprite(icon);
    }

    public static Image fromImage(ResourceLocation location) {
        return new Image(location);
    }

    @Override
    public int tintIndex() {
        return tintIndex;
    }

    @Override
    public double red() {
        return red;
    }

    @Override
    public double green() {
        return green;
    }

    @Override
    public double blue() {
        return blue;
    }

    @Override
    public boolean isEmissive() {
        return isEmissive;
    }

    @Override
    public boolean isProjected() {
        return isProjected;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public ResourceLocation location() {
        return location;
    }

    @Override
    public ITexture tinted(int index) {
        TextureBase result = new Proxy(this);
        result.tintIndex = index;
        return result;
    }

    @Override
    public ITexture colored(double red, double green, double blue) {
        TextureBase result = new Proxy(this);
        result.red = red;
        result.green = green;
        result.blue = blue;
        return result;
    }

    @Override
    public ITexture emissive() {
        TextureBase result = new Proxy(this);
        result.isEmissive = true;
        return result;
    }

    @Override
    public ITexture projected() {
        TextureBase result = new Proxy(this);
        result.isProjected = true;
        return result;
    }

    @Override
    public ITiledTexture tiled(int numRows, int numCols) {
        return new TileSet(this, numRows, numCols);
    }

    //-------------------------------------------------------------------------------------------

    public static class Proxy extends TextureBase {

        public ITexture base;

        public Proxy(ITexture base) {
            this.base = base;
            this.location = base.location();
            this.tintIndex = base.tintIndex();
            this.red = base.red();
            this.green = base.green();
            this.blue = base.blue();
            this.isEmissive = base.isEmissive();
            this.isProjected = base.isProjected();
        }

//      @Override
//      public ResourceLocation location() {
//          return base.location();
//      }

        @Override
        public boolean isSolid() {
            return base.isSolid();
        }

        @Override
        public double interpolateU(double u) {
            return base.interpolateU(u);
        }

        @Override
        public double interpolateV(double v) {
            return base.interpolateV(v);
        }

    }

    //-------------------------------------------------------------------------------------------

    public static class Sprite extends TextureBase {

        public TextureAtlasSprite icon;

        public Sprite(TextureAtlasSprite icon) {
            this.icon = icon;
            red = green = blue = 1.0;
        }

        @Override
        public double interpolateU(double u) {
            return icon.getInterpolatedU(u * 16);
        }

        @Override
        public double interpolateV(double v) {
            return icon.getInterpolatedV(v * 16);
        }

    }

    //-------------------------------------------------------------------------------------------

    public static class Image extends TextureBase {

//      public ResourceLocation location;

        public Image(ResourceLocation location) {
            this.location = location;
        }

//      public ResourceLocation location() {
//          return location;
//      }

        @Override
        public double interpolateU(double u) {
            return u;
        }

        @Override
        public double interpolateV(double v) {
            return v;
        }

    }

    //-------------------------------------------------------------------------------------------

    public static class Solid extends TextureBase {

        public Solid(double red, double green, double blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        @Override
        public boolean isSolid() {
            return true;
        }

        @Override
        public double interpolateU(double u) {
            return 0;
        }

        @Override
        public double interpolateV(double v) {
            return 0;
        }

    }

    //-------------------------------------------------------------------------------------------

    public static class TileSet extends Proxy implements ITiledTexture {

        public double tileSizeU, tileSizeV;

        public TileSet(ITexture base, int numRows, int numCols) {
            super(base);
            tileSizeU = 1.0 / numCols;
            tileSizeV = 1.0 / numRows;
        }

        @Override
        public ITexture tile(int row, int col) {
            return new Tile(this, row, col);
        }

    }

    //-------------------------------------------------------------------------------------------

    public static class Tile extends Proxy {

        protected double u0, v0, uSize, vSize;

        public Tile(TileSet base, int row, int col) {
            super(base);
            uSize = base.tileSizeU;
            vSize = base.tileSizeV;
            u0 = uSize * col;
            v0 = vSize * row;
        }

        @Override
        public double interpolateU(double u) {
            return super.interpolateU(u0 + u * uSize);
        }

        @Override
        public double interpolateV(double v) {
            return super.interpolateV(v0 + v * vSize);
        }

    }

    //-------------------------------------------------------------------------------------------

    public static class Debug extends Sprite {

        public Debug(TextureAtlasSprite icon) {
            super(icon);
        }

        @Override
        public double interpolateU(double u) {
            double iu = super.interpolateU(u);
            System.out.printf("BaseTexture: %s u (%s - %s)\n", icon.getIconName(), icon.getMinU(), icon.getMaxU());
            System.out.printf("BaseTexture: u %s --> %s\n", u, iu);
            return iu;
        }

        @Override
        public double interpolateV(double v) {
            double iv = super.interpolateV(v);
            System.out.printf("BaseTexture: %s v (%s - %s)\n", icon.getIconName(), icon.getMinV(), icon.getMaxV());
            System.out.printf("BaseTexture: v %s --> %s\n", v, iv);
            return iv;
        }

    }

    //-------------------------------------------------------------------------------------------

}
