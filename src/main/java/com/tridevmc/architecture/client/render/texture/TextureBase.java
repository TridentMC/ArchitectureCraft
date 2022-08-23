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

package com.tridevmc.architecture.client.render.texture;

import com.tridevmc.architecture.common.ArchitectureLog;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

public abstract class TextureBase implements ITexture {

    public ResourceLocation location;
    public int tintIndex;
    public double red = 1, green = 1, blue = 1;
    public boolean isEmissive;
    public boolean isProjected;

    public static Sprite fromSprite(TextureAtlasSprite icon) {
        return new Sprite(icon);
    }

    @Override
    public int tintIndex() {
        return this.tintIndex;
    }

    @Override
    public double red() {
        return this.red;
    }

    @Override
    public double green() {
        return this.green;
    }

    @Override
    public double blue() {
        return this.blue;
    }

    @Override
    public boolean isEmissive() {
        return this.isEmissive;
    }

    @Override
    public boolean isProjected() {
        return this.isProjected;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public ResourceLocation location() {
        return this.location;
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
            return this.base.isSolid();
        }

        @Override
        public TextureAtlasSprite getSprite() {
            return null;
        }

        @Override
        public double interpolateU(double u) {
            return this.base.interpolateU(u);
        }

        @Override
        public double interpolateV(double v) {
            return this.base.interpolateV(v);
        }

    }

    //-------------------------------------------------------------------------------------------

    public static class Sprite extends TextureBase {

        public TextureAtlasSprite icon;

        public Sprite(TextureAtlasSprite icon) {
            this.icon = icon;
            this.red = this.green = this.blue = 1.0;
        }

        @Override
        public double interpolateU(double u) {
            return this.icon.getU(u * 16);
        }

        @Override
        public double interpolateV(double v) {
            return this.icon.getV(v * 16);
        }

        @Override
        public TextureAtlasSprite getSprite() {
            return this.icon;
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
        public TextureAtlasSprite getSprite() {
            return null;
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
            this.tileSizeU = 1.0 / numCols;
            this.tileSizeV = 1.0 / numRows;
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
            this.uSize = base.tileSizeU;
            this.vSize = base.tileSizeV;
            this.u0 = this.uSize * col;
            this.v0 = this.vSize * row;
        }

        @Override
        public double interpolateU(double u) {
            return super.interpolateU(this.u0 + u * this.uSize);
        }

        @Override
        public double interpolateV(double v) {
            return super.interpolateV(this.v0 + v * this.vSize);
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
            ArchitectureLog.info("BaseTexture: %s u (%s - %s)\n", this.icon.getName(), this.icon.getU0(), this.icon.getU1());
            ArchitectureLog.info("BaseTexture: u %s --> %s\n", u, iu);
            return iu;
        }

        @Override
        public double interpolateV(double v) {
            double iv = super.interpolateV(v);
            ArchitectureLog.info("BaseTexture: %s v (%s - %s)\n", this.icon.getName(), this.icon.getV0(), this.icon.getV1());
            ArchitectureLog.info("BaseTexture: v %s --> %s\n", v, iv);
            return iv;
        }

    }

    //-------------------------------------------------------------------------------------------

}
