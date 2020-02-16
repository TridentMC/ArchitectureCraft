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

package com.tridevmc.architecture.client.render.target;

import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.client.render.texture.TextureBase;
import com.tridevmc.architecture.common.ArchitectureLog;
import com.tridevmc.architecture.common.helpers.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.*;

public class RenderTargetBaked extends RenderTargetBase {

    // It seems to be necessary to put the padding byte *before* the
    // normal bytes, even though DefaultVertexFormats.ITEM says it
    // should be after.
    protected static VertexFormat theFormat = new VertexFormat(ImmutableList.of(POSITION_3F,
            COLOR_4UB,
            TEX_2F,
            PADDING_1B,
            NORMAL_3B));
    protected static List<BakedQuad> emptyQuads = new ArrayList<BakedQuad>();
    protected static Map<Direction, List<BakedQuad>> faceQuads = new HashMap<>();
    static protected ItemTransformVec3f
            transThirdPerson = new ItemTransformVec3f(
            new Vector3f(75f, -45f, 0f),
            new Vector3f(0f, 2.5f / 16f, 0f),
            new Vector3f(0.375f, 0.375f, 0.375f)),

    transFirstPerson = new ItemTransformVec3f(
            new Vector3f(0f, -45f, 0f),
            new Vector3f(0f, 0f, 0f),
            new Vector3f(0.4f, 0.4f, 0.4f)),

    transOnHead = new ItemTransformVec3f(
            new Vector3f(0f, 180f, 0f),
            new Vector3f(0f, 13f / 16f, 7f / 16f),
            new Vector3f(1f, 1f, 1f)),

    transInGui = new ItemTransformVec3f(
            new Vector3f(30f, -45f, 0f),
            new Vector3f(0f, 0f, 0f),
            new Vector3f(0.625f, 0.625f, 0.625f)),

    transOnGround = new ItemTransformVec3f(
            new Vector3f(0f, 0f, 0f),
            new Vector3f(0f, 3f / 16f, 0f),
            new Vector3f(0.25f, 0.25f, 0.25f)),

    transFixed = new ItemTransformVec3f(
            new Vector3f(0f, 0f, 0f),
            new Vector3f(0f, 0f, 0f),
            new Vector3f(0.5f, 0.5f, 0.5f));
    static protected ItemCameraTransforms transforms = new ItemCameraTransforms(
            transThirdPerson, // thirdPerson_left
            transThirdPerson, // thirdPerson_right
            transFirstPerson, // firstperson_left
            transFirstPerson, // firstperson_right
            transOnHead, // head,
            transInGui, // gui
            transOnGround, // ground
            transFixed // fixed
    );

    static {
        for (Direction face : Direction.values())
            faceQuads.put(face, emptyQuads);
    }

    protected VertexFormat format = theFormat;
    protected int bytesPerVertex = this.format.getSize();
    protected int intsPerVertex = this.bytesPerVertex / 4;
    protected ByteBuffer buf = ByteBuffer.allocate(this.bytesPerVertex * 4);
    protected List<BakedQuad> quads;

    public RenderTargetBaked() {
        this(0, 0, 0, null);
    }

    public RenderTargetBaked(BlockPos pos) {
        this(pos, null);
    }

    public RenderTargetBaked(TextureAtlasSprite overrideIcon) {
        this(0, 0, 0, overrideIcon);
    }

    //--------------------------------- IRenderTarget ------------------------------------------

    public RenderTargetBaked(BlockPos pos, TextureAtlasSprite overrideIcon) {
        this(pos.getX(), pos.getY(), pos.getZ(), overrideIcon);
    }

    public RenderTargetBaked(double x, double y, double z, TextureAtlasSprite overrideIcon) {
        super(x, y, z, overrideIcon);
        this.quads = new ArrayList<>();
    }

    @Override
    protected void setMode(int m) {
        super.setMode(m);
        this.buf.clear();
    }

    @Override
    public void endFace() {
        super.endFace();
        this.buf.flip();
        int intsPerQuad = this.intsPerVertex * 4;
        int[] data = new int[intsPerQuad];
        IntBuffer intBuf = this.buf.asIntBuffer();
        int n = intBuf.limit();
        intBuf.get(data, 0, n);
        while (n < intsPerQuad) {
            data[n] = data[n - this.intsPerVertex];
            ++n;
        }
        this.prescrambleVertexColors(data);
        this.quads.add(new BakedQuad(data, 0, this.face, this.getActiveTexture(), false));
    }

    private TextureAtlasSprite getActiveTexture() {
        // Use missingno as a fallback
        TextureAtlasSprite activeTexture = Minecraft.getInstance().getTextureGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(MissingTextureSprite.getLocation());
        if (this.texture instanceof TextureBase.Sprite) {
            TextureBase.Sprite sprite = (TextureBase.Sprite) this.texture;
            activeTexture = sprite.icon;
        } else if (this.texture instanceof TextureBase.Proxy) {
            TextureBase.Proxy proxySprite = (TextureBase.Proxy) this.texture;
            if (proxySprite.base instanceof TextureBase.Sprite) {
                activeTexture = ((TextureBase.Sprite) proxySprite.base).icon;
            }
        }

        return activeTexture;
    }

    protected void dumpVertexData(int[] data, int n) {
        ArchitectureLog.info("BaseBakedRenderTarget.endFace: Vertex data:\n");
        for (int i = 0; i < 4; i++) {
            int k = i * this.intsPerVertex;
            ArchitectureLog.info("[%s] coords (%.3f,%.3f,%.3f) color %08x\n",
                    i,
                    Float.intBitsToFloat(data[k]),
                    Float.intBitsToFloat(data[k + 1]),
                    Float.intBitsToFloat(data[k + 2]),
                    data[k + 3]);
        }
    }

    protected void prescrambleVertexColors(int[] data) {
        int[] c = new int[4];
        for (int i = 0; i < 4; i++)
            c[i] = data[i * this.intsPerVertex + 3];
        for (int i = 0; i < 4; i++)
            data[i * this.intsPerVertex + 3] = c[3 - i];
    }

    public IBakedModel getBakedModel() {
        return this.getBakedModel(null);
    }

    public IBakedModel getBakedModel(TextureAtlasSprite particleTexture) {
        if (this.verticesPerFace != 0)
            throw new IllegalStateException("Rendering ended with incomplete face");
        return new SimpleBakedModel(this.quads, faceQuads, false, true, true,
                particleTexture, transforms, ItemOverrideList.EMPTY);
    }

    @Override
    protected void rawAddVertex(Vector3 p, double u, double v) {
        for (VertexFormatElement e : this.format.getElements()) {
            switch (e.getUsage()) {
                case POSITION:
                    this.putElement(e, p.x, p.y, p.z);
                    break;
                case COLOR:
                    this.putElement(e, this.alpha, this.blue, this.green, this.red);
                    break;
                case NORMAL:
                    this.putElement(e, this.normal.x, this.normal.y, this.normal.z);
                    break;
                case UV:
                    this.putElement(e, u, v);
                    break;
                default:
                    this.putElement(e);
            }
        }
    }

    protected void putElement(VertexFormatElement e, Number... ns) {
        Number n;
        for (int i = 0; i < e.getElementCount(); i++) {
            if (i < ns.length)
                n = ns[i];
            else
                n = 0;
            switch (e.getType()) {
                case BYTE:
                    this.buf.put((byte) (n.floatValue() * 0x7f));
                    break;
                case UBYTE:
                    this.buf.put((byte) (n.floatValue() * 0xff));
                    break;
                case SHORT:
                    this.buf.putShort((short) (n.floatValue() * 0x7fff));
                    break;
                case USHORT:
                    this.buf.putShort((short) (n.floatValue() * 0xffff));
                    break;
                case INT:
                    this.buf.putInt((int) (n.doubleValue() * 0x7fffffff));
                    break;
                case UINT:
                    this.buf.putInt((int) (n.doubleValue() * 0xffffffff));
                    break;
                case FLOAT:
                    this.buf.putFloat(n.floatValue());
                    break;
            }
        }
    }

}
