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

package com.elytradev.architecture.client.render.target;

import com.elytradev.architecture.client.render.texture.TextureBase;
import com.elytradev.architecture.common.ArchitectureLog;
import com.elytradev.architecture.common.helpers.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
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
    protected static VertexFormat theFormat = new VertexFormat();
    protected static List<BakedQuad> emptyQuads = new ArrayList<BakedQuad>();
    protected static Map<EnumFacing, List<BakedQuad>> faceQuads = new HashMap<>();
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
        theFormat.addElement(POSITION_3F);
        theFormat.addElement(COLOR_4UB);
        theFormat.addElement(TEX_2F);
        theFormat.addElement(PADDING_1B);
        theFormat.addElement(NORMAL_3B);
    }

    static {
        for (EnumFacing face : EnumFacing.values())
            faceQuads.put(face, emptyQuads);
    }

    protected VertexFormat format = theFormat;
    protected int bytesPerVertex = format.getSize();
    protected int intsPerVertex = bytesPerVertex / 4;
    protected ByteBuffer buf = ByteBuffer.allocate(bytesPerVertex * 4);
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
        quads = new ArrayList<>();
    }

    @Override
    protected void setMode(int m) {
        super.setMode(m);
        buf.clear();
    }

    @Override
    public void endFace() {
        super.endFace();
        buf.flip();
        int intsPerQuad = intsPerVertex * 4;
        int[] data = new int[intsPerQuad];
        IntBuffer intBuf = buf.asIntBuffer();
        int n = intBuf.limit();
        intBuf.get(data, 0, n);
        while (n < intsPerQuad) {
            data[n] = data[n - intsPerVertex];
            ++n;
        }
        prescrambleVertexColors(data);
        quads.add(new BakedQuad(data, 0, face, getActiveTexture(), false, DefaultVertexFormats.ITEM));
    }

    private TextureAtlasSprite getActiveTexture() {
        // Use missingno as a fallback
        TextureAtlasSprite activeTexture = Minecraft.getInstance().getTextureMap().getAtlasSprite("missingno");
        if (texture instanceof TextureBase.Sprite) {
            TextureBase.Sprite sprite = (TextureBase.Sprite) texture;
            activeTexture = sprite.icon;
        } else if (texture instanceof TextureBase.Proxy) {
            TextureBase.Proxy proxySprite = (TextureBase.Proxy) texture;
            if (proxySprite.base instanceof TextureBase.Sprite) {
                activeTexture = ((TextureBase.Sprite) proxySprite.base).icon;
            }
        }

        return activeTexture;
    }

    protected void dumpVertexData(int[] data, int n) {
        ArchitectureLog.info("BaseBakedRenderTarget.endFace: Vertex data:\n");
        for (int i = 0; i < 4; i++) {
            int k = i * intsPerVertex;
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
            c[i] = data[i * intsPerVertex + 3];
        for (int i = 0; i < 4; i++)
            data[i * intsPerVertex + 3] = c[3 - i];
    }

    public IBakedModel getBakedModel() {
        return getBakedModel(null);
    }

    public IBakedModel getBakedModel(TextureAtlasSprite particleTexture) {
        if (verticesPerFace != 0)
            throw new IllegalStateException("Rendering ended with incomplete face");
        return new SimpleBakedModel(quads, faceQuads, false, true,
                particleTexture, transforms, ItemOverrideList.EMPTY);
    }

    @Override
    protected void rawAddVertex(Vector3 p, double u, double v) {
        for (VertexFormatElement e : format.getElements()) {
            switch (e.getUsage()) {
                case POSITION:
                    putElement(e, p.x, p.y, p.z);
                    break;
                case COLOR:
                    putElement(e, alpha, blue, green, red);
                    break;
                case NORMAL:
                    putElement(e, normal.x, normal.y, normal.z);
                    break;
                case UV:
                    putElement(e, u, v);
                    break;
                default:
                    putElement(e);
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
                    buf.put((byte) (n.floatValue() * 0x7f));
                    break;
                case UBYTE:
                    buf.put((byte) (n.floatValue() * 0xff));
                    break;
                case SHORT:
                    buf.putShort((short) (n.floatValue() * 0x7fff));
                    break;
                case USHORT:
                    buf.putShort((short) (n.floatValue() * 0xffff));
                    break;
                case INT:
                    buf.putInt((int) (n.doubleValue() * 0x7fffffff));
                    break;
                case UINT:
                    buf.putInt((int) (n.doubleValue() * 0xffffffff));
                    break;
                case FLOAT:
                    buf.putFloat(n.floatValue());
                    break;
            }
        }
    }

}
