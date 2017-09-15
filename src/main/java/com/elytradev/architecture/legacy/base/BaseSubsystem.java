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

package com.elytradev.architecture.legacy.base;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class BaseSubsystem<MOD extends BaseMod, CLIENT extends BaseModClient> {

    public MOD mod;
    public CLIENT client;

    public static Item findItem(String name) {
        return Item.REGISTRY.getObject(new ResourceLocation(name));
    }

    public void preInit(FMLPreInitializationEvent e) {
    }

    public void init(FMLInitializationEvent e) {
    }

    public void postInit(FMLPostInitializationEvent e) {
    }

    public void configure(BaseConfiguration config) {
    }

    protected void registerBlocks() {
    }

    protected void registerItems() {
    }

    protected void registerOres() {
    }

    protected void registerRecipes() {
    }

    protected void registerTileEntities() {
    }

    protected void registerWorldGenerators() {
    }

    protected void registerContainers() {
    }

    protected void registerEntities() {
    }

    protected void registerVillagers() {
    }

    protected void registerSounds() {
    }

    protected void registerOther() {
    }

    protected void registerScreens() {
    }

    protected void registerBlockRenderers() {
    }

    protected void registerItemRenderers() {
    }

    protected void registerEntityRenderers() {
    }

    protected void registerTileEntityRenderers() {
    }

    protected void registerModelLocations() {
    }

    protected void registerOtherClient() {
    }

    public Item searchForItem(String... names) {
        Item result = null;
        for (String name : names) {
            result = findItem(name);
            if (result != null)
                return result;
        }
        System.out.printf("%s: Unable to find an item with any of the following names:",
                getClass().getName());
        for (String name : names)
            System.out.printf(" %s", name);
        System.out.printf("\n");
        return null;
    }

}
