//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.8 - Texture
//
//------------------------------------------------------------------------------------------------

package com.elytradev.architecture;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

public abstract class BaseTexture implements BaseModClient.ITexture {

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

    public int tintIndex() {
        return tintIndex;
    }

    public double red() {
        return red;
    }

    public double green() {
        return green;
    }

    public double blue() {
        return blue;
    }

    public boolean isEmissive() {
        return isEmissive;
    }

    public boolean isProjected() {
        return isProjected;
    }

    public boolean isSolid() {
        return false;
    }

    public ResourceLocation location() {
        return location;
    }

    public BaseModClient.ITexture tinted(int index) {
        BaseTexture result = new Proxy(this);
        result.tintIndex = index;
        return result;
    }

    public BaseModClient.ITexture colored(double red, double green, double blue) {
        BaseTexture result = new Proxy(this);
        result.red = red;
        result.green = green;
        result.blue = blue;
        return result;
    }

    public BaseModClient.ITexture emissive() {
        BaseTexture result = new Proxy(this);
        result.isEmissive = true;
        return result;
    }

    public BaseModClient.ITexture projected() {
        BaseTexture result = new Proxy(this);
        result.isProjected = true;
        return result;
    }

    public BaseModClient.ITiledTexture tiled(int numRows, int numCols) {
        return new TileSet(this, numRows, numCols);
    }

    //-------------------------------------------------------------------------------------------

    public static class Proxy extends BaseTexture {

        public BaseModClient.ITexture base;

        public Proxy(BaseModClient.ITexture base) {
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

        public double interpolateU(double u) {
            return base.interpolateU(u);
        }

        public double interpolateV(double v) {
            return base.interpolateV(v);
        }

    }

    //-------------------------------------------------------------------------------------------

    public static class Sprite extends BaseTexture {

        public TextureAtlasSprite icon;

        public Sprite(TextureAtlasSprite icon) {
            this.icon = icon;
            red = green = blue = 1.0;
        }

        public double interpolateU(double u) {
            return icon.getInterpolatedU(u * 16);
        }

        public double interpolateV(double v) {
            return icon.getInterpolatedV(v * 16);
        }

    }

    //-------------------------------------------------------------------------------------------

    public static class Image extends BaseTexture {

//      public ResourceLocation location;

        public Image(ResourceLocation location) {
            this.location = location;
        }

//      public ResourceLocation location() {
//          return location;
//      }

        public double interpolateU(double u) {
            return u;
        }

        public double interpolateV(double v) {
            return v;
        }

    }

    //-------------------------------------------------------------------------------------------

    public static class Solid extends BaseTexture {

        public Solid(double red, double green, double blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }

        @Override
        public boolean isSolid() {
            return true;
        }

        public double interpolateU(double u) {
            return 0;
        }

        public double interpolateV(double v) {
            return 0;
        }

    }

    //-------------------------------------------------------------------------------------------

    public static class TileSet extends Proxy implements BaseModClient.ITiledTexture {

        public double tileSizeU, tileSizeV;

        public TileSet(BaseModClient.ITexture base, int numRows, int numCols) {
            super(base);
            tileSizeU = 1.0 / numCols;
            tileSizeV = 1.0 / numRows;
        }

        public BaseModClient.ITexture tile(int row, int col) {
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

        public double interpolateV(double v) {
            double iv = super.interpolateV(v);
            System.out.printf("BaseTexture: %s v (%s - %s)\n", icon.getIconName(), icon.getMinV(), icon.getMaxV());
            System.out.printf("BaseTexture: v %s --> %s\n", v, iv);
            return iv;
        }

    }

    //-------------------------------------------------------------------------------------------

}
