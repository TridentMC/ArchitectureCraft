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

package com.elytradev.architecture.client.gui;

import com.elytradev.architecture.common.network.SelectShapeMessage;
import com.elytradev.architecture.common.shape.EnumShape;
import com.elytradev.architecture.common.shape.ShapePage;
import com.elytradev.architecture.common.tile.ContainerSawbench;
import com.elytradev.architecture.common.tile.TileSawbench;
import com.elytradev.architecture.legacy.base.BaseGui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static org.lwjgl.opengl.GL11.*;

public class GuiSawbench extends BaseGui.Screen {

    public static int pageMenuLeft = 176;
    public static int pageMenuTop = 19;
    public static int pageMenuWidth = 58;
    public static int pageMenuRowHeight = 10;
    public static float pageMenuScale = 1;

    public static int shapeMenuLeft = 44;
    public static int shapeMenuTop = 23;
    public static int shapeMenuMargin = 4;
    public static int shapeMenuCellSize = 24;
    public static int shapeMenuRows = 4, shapeMenuCols = 5;
    public static int shapeMenuWidth = shapeMenuCols * shapeMenuCellSize;
    public static int shapeMenuHeight = shapeMenuRows * shapeMenuCellSize;
    public static int selectedShapeTitleLeft = 40;
    public static int selectedShapeTitleTop = 128;
    public static int selectedShapeTitleRight = 168;
    public static int materialUsageLeft = 7;
    public static int materialUsageTop = 82;
    public static float shapeMenuScale = 2;
    public static float shapeMenuItemScale = 2;
    public static float shapeMenuItemUSize = 40, shapeMenuItemVSize = 45;
    public static float shapeMenuItemWidth = shapeMenuItemUSize / shapeMenuItemScale;
    public static float shapeMenuItemHeight = shapeMenuItemVSize / shapeMenuItemScale;

    TileSawbench te;

    public GuiSawbench(EntityPlayer player, TileSawbench te) {
        super(new ContainerSawbench(player, te));
        this.te = te;
    }

    public static GuiSawbench create(EntityPlayer player, World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileSawbench)
            return new GuiSawbench(player, (TileSawbench) te);
        else
            return null;
    }

    @Override
    protected void drawBackgroundLayer() {
        glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.bindTexture("gui/gui_sawbench.png", 256, 256);
        this.drawTexturedRect(0, 0, this.xSize, this.ySize, 0, 0);
        this.drawShapeMenu();
        this.drawShapeSelection();
        this.drawPageMenu();
        this.drawSelectedShapeTitle();
        this.fontRenderer.drawString("Sawbench", 7, 7, 4210752);
    }

    void drawPageMenu() {
        glPushMatrix();
        glTranslatef(pageMenuLeft, pageMenuTop, 0);
        this.gSave();
        this.setColor(102 / 255d, 204 / 255d, 1);
        this.drawRect(0, this.te.selectedPage * pageMenuRowHeight, pageMenuWidth, pageMenuRowHeight);
        this.gRestore();
        for (int i = 0; i < TileSawbench.pages.length; i++) {
            this.drawString(TileSawbench.pages[i].title, 1, 1);
            glTranslatef(0, pageMenuRowHeight, 0);
        }
        glPopMatrix();
    }

    void drawShapeMenu() {
        this.gSave();
        glPushMatrix();
        glDisable(GL_ALPHA_TEST);
        glEnable(GL_BLEND);
        glTranslatef(shapeMenuLeft, shapeMenuTop, 0);
        this.bindTexture("gui/shapemenu_bg.png", 256, 256);
        double w = shapeMenuWidth + 2 * shapeMenuMargin;
        double h = shapeMenuHeight + 2 * shapeMenuMargin;
        this.drawTexturedRect(-shapeMenuMargin, -shapeMenuMargin, w, h,
                0, 0, shapeMenuScale * w, shapeMenuScale * h);
        this.bindTexture("gui/shapemenu_items.png", 512, 512);
        int p = this.te.selectedPage;
        if (p >= 0 && p < TileSawbench.pages.length) {
            ShapePage page = TileSawbench.pages[p];
            if (page != null) {
                EnumShape[] shapes = page.shapes;
                for (int i = 0; i < shapes.length; i++) {
                    EnumShape shape = shapes[i];
                    int mrow = i / shapeMenuCols, mcol = i % shapeMenuCols;
                    int id = shape.id;
                    int trow = id / 10, tcol = id % 10;
                    this.drawTexturedRect(
                            (mcol + 0.5) * shapeMenuCellSize - 0.5 * shapeMenuItemWidth,
                            (mrow + 0.5) * shapeMenuCellSize - 0.5 * shapeMenuItemHeight,
                            shapeMenuItemWidth, shapeMenuItemHeight,
                            tcol * shapeMenuItemUSize, trow * shapeMenuItemVSize,
                            shapeMenuItemUSize, shapeMenuItemVSize);
                }
            }
        }
        glPopMatrix();
        this.gRestore();
    }

    void drawShapeSelection() {
        int i = this.te.selectedSlots[this.te.selectedPage];
        int row = i / shapeMenuCols;
        int col = i % shapeMenuCols;
        int x = shapeMenuLeft + shapeMenuCellSize * col;
        int y = shapeMenuTop + shapeMenuCellSize * row;
        this.drawTexturedRect(x, y, 24.5, 24.5, 44, 23, 49, 49);
    }

    void drawSelectedShapeTitle() {
        EnumShape shape = this.te.getSelectedShape();
        if (shape != null) {
            int x = selectedShapeTitleLeft;
            int w = this.fontRenderer.getStringWidth(shape.getLocalizedShapeName());
            if (x + w > selectedShapeTitleRight)
                x = selectedShapeTitleRight - w;
            this.drawString(shape.getLocalizedShapeName(), x, selectedShapeTitleTop);
            glPushMatrix();
            glTranslatef(materialUsageLeft, materialUsageTop, 0);
            glScalef(0.5f, 0.5f, 1.0f);
            this.drawString(String.format("%s makes %s", this.te.materialMultiple(), this.te.resultMultiple()), 0, 0);
            glPopMatrix();
        }
    }

    @Override
    protected void mousePressed(int x, int y, int btn) {
        if (x >= pageMenuLeft && y >= pageMenuTop && x < pageMenuLeft + pageMenuWidth)
            this.clickPageMenu(x - pageMenuLeft, y - pageMenuTop);
        else if (x >= shapeMenuLeft && y >= shapeMenuTop &&
                x < shapeMenuLeft + shapeMenuWidth && y < shapeMenuTop + shapeMenuHeight)
            this.clickShapeMenu(x - shapeMenuLeft, y - shapeMenuTop);
        else
            super.mousePressed(x, y, btn);
    }

    void clickPageMenu(int x, int y) {
        int i = y / pageMenuRowHeight;
        if (i >= 0 && i < TileSawbench.pages.length)
            this.sendSelectShape(i, this.te.selectedSlots[i]);
    }

    void clickShapeMenu(int x, int y) {
        int row = y / shapeMenuCellSize;
        int col = x / shapeMenuCellSize;
        if (row >= 0 && row < shapeMenuRows && col >= 0 && col < shapeMenuCols) {
            int i = row * shapeMenuCols + col;
            this.sendSelectShape(this.te.selectedPage, i);
        }
    }

    protected void sendSelectShape(int page, int slot) {
        new SelectShapeMessage(this.te, page, slot).sendToServer();
    }
}
