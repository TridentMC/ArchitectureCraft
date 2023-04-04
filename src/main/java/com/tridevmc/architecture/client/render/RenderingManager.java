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

package com.tridevmc.architecture.client.render;

import com.tridevmc.architecture.client.render.texture.ITexture;
import com.tridevmc.architecture.client.render.texture.TextureBase;
import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.model.ITextureConsumer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class RenderingManager {

    protected static String[] texturePrefixes = {"blocks/", "textures/"};
    protected Map<ResourceLocation, ITexture> textureCache = new HashMap<ResourceLocation, ITexture>();

    public ResourceLocation textureResourceLocation(int type, String name) {
        // TextureMap adds "textures/"
        return new ResourceLocation(ArchitectureMod.MOD_ID, texturePrefixes[type] + name);
    }

    public void registerSprites(int textureType, TextureAtlas reg, Object obj) {
        if (obj instanceof ITextureConsumer) {
            String[] names = ((ITextureConsumer) obj).getTextureNames();
            if (names != null) {
                for (String name : names) {
                    ResourceLocation loc = this.textureResourceLocation(textureType, name);
                    if (this.textureCache.get(loc) == null) {
                        TextureAtlasSprite icon = reg.getSprite(loc);
                        ITexture texture = TextureBase.fromSprite(icon);
                        this.textureCache.put(loc, texture);
                    }
                }
            }
        }
    }

}
