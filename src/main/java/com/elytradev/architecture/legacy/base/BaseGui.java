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

import com.elytradev.architecture.common.ArchitectureLog;
import com.elytradev.architecture.common.ArchitectureMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.elytradev.architecture.common.utils.MiscUtils.packedColor;
import static org.lwjgl.opengl.GL11.*;

public class BaseGui {

    public final static int defaultTextColor = 0x404040;

    static boolean isFocused(IWidget widget) {
        if (widget == null)
            return false;
        else if (widget instanceof Root)
            return true;
        else {
            IWidgetContainer parent = widget.parent();
            return (parent != null && parent.getFocus() == widget && isFocused(parent));
        }
    }

    static void tellFocusChanged(IWidget widget, boolean state) {
        if (widget != null) {
            widget.focusChanged(state);
            if (widget instanceof IWidgetContainer)
                tellFocusChanged(((IWidgetContainer) widget).getFocus(), state);
        }
    }

    static String name(Object obj) {
        if (obj != null)
            return obj.getClass().getSimpleName();
        else
            return "null";
    }

    public static Ref ref(Object target, String name) {
        return new FieldRef(target, name);
    }

    //------------------------------------------------------------------------------------------------

    public static Ref ref(Object target, String getterName, String setterName) {
        return new PropertyRef(target, getterName, setterName);
    }

    //------------------------------------------------------------------------------------------------

    public static Action action(Object target, String name) {
        return new MethodAction(target, name);
    }

    //------------------------------------------------------------------------------------------------

    public interface IWidget {
        IWidgetContainer parent();

        void setParent(IWidgetContainer widget);

        int left();

        int top();

        int width();

        int height();

        void setLeft(int x);

        void setTop(int y);

        void draw(Screen scr, int mouseX, int mouseY);

        IWidget dispatchMousePress(int x, int y, int button);

        boolean dispatchKeyPress(char c, int key);

        void mousePressed(MouseCoords m, int button);

        void mouseDragged(MouseCoords m, int button);

        void mouseReleased(MouseCoords m, int button);

        boolean keyPressed(char c, int key);

        void focusChanged(boolean state);

        void close();

        void layout();
    }

    public interface IWidgetContainer extends IWidget {
        IWidget getFocus();

        void setFocus(IWidget widget);
    }

    public interface Ref {
        Object get();

        void set(Object value);
    }

    public interface Action {
        void perform();
    }

    public static class Screen extends GuiContainer {

        protected Root root;
        protected String title;
        protected Tessellator tess;
        protected BufferBuilder vb;
        protected IWidget mouseWidget;
        protected GState gstate;

        public Screen(Container container, int width, int height) {
            super(container);
            this.xSize = width;
            this.ySize = height;
            this.root = new Root(this);
            this.tess = Tessellator.getInstance();
            this.vb = this.tess.getBuffer();
            this.gstate = new GState();
        }

        public Screen(BaseContainer container) {
            this(container, container.xSize, container.ySize);
        }

        public static String playerInventoryName() {
            return I18n.translateToLocal("container.inventory");
        }

        public int getWidth() {
            return this.xSize;
        }

        public int getHeight() {
            return this.ySize;
        }

        @Override
        public void drawScreen(int mouseX, int mouseY, float partialTicks) {
            this.drawDefaultBackground();
            super.drawScreen(mouseX, mouseY, partialTicks);
            this.renderHoveredToolTip(mouseX, mouseY);
        }

        @Override
        public void initGui() {
            super.initGui();
            this.root.layout();
        }

        @Override
        protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
            GL11.glPushMatrix();
            GL11.glTranslatef(this.guiLeft, this.guiTop, 0.0F);
            this.drawBackgroundLayer();
            if (this.title != null)
                this.drawTitle(this.title);
            this.root.draw(this, mouseX - this.guiLeft, mouseY - this.guiTop);
            GL11.glPopMatrix();
        }

        protected void drawBackgroundLayer() {
            this.drawGuiBackground(0, 0, this.xSize, this.ySize);
        }

        @Override
        protected void drawGuiContainerForegroundLayer(int par1, int par2) {
            this.drawForegroundLayer();
        }

        protected void drawForegroundLayer() {
        }

        public void close() {
            this.dispatchClosure(this.root);
            this.onClose();
            this.mc.player.closeScreen();
        }

        protected void onClose() {
        }

        public void bindTexture(String path) {
            this.bindTexture(path, 1, 1);
        }

        public void bindTexture(String path, int usize, int vsize) {
            this.bindTexture(new ResourceLocation(ArchitectureMod.MOD_ID, "textures/" + path), usize, vsize);
        }

        public void bindTexture(ResourceLocation rsrc) {
            this.bindTexture(rsrc, 1, 1);
        }

        public void bindTexture(ResourceLocation rsrc, int usize, int vsize) {
            this.gstate.texture = rsrc;
            this.mc.getTextureManager().bindTexture(rsrc);
            this.gstate.uscale = 1.0 / usize;
            this.gstate.vscale = 1.0 / vsize;
        }

        public void gSave() {
            this.gstate = new GState(this.gstate);
        }

        public void gRestore() {
            if (this.gstate.previous != null) {
                this.gstate = this.gstate.previous;
                this.mc.getTextureManager().bindTexture(this.gstate.texture);
            } else
                ArchitectureLog.info("BaseGui: Warning: Graphics state stack underflow\n");
        }

        public void drawRect(double x, double y, double w, double h) {
            glDisable(GL_TEXTURE_2D);
            glColor3d(this.gstate.red, this.gstate.green, this.gstate.blue);
            glBegin(GL_QUADS);
            glVertex3d(x, y + h, this.zLevel);
            glVertex3d(x + w, y + h, this.zLevel);
            glVertex3d(x + w, y, this.zLevel);
            glVertex3d(x, y, this.zLevel);
            glEnd();
            glEnable(GL_TEXTURE_2D);
        }

        public void drawBorderedRect(double x, double y, double w, double h,
                                     double u, double v, double uSize, double vSize, double cornerWidth, double cornerHeight) {
            double cw = cornerWidth, ch = cornerHeight;
            double sw = w - 2 * cornerWidth;       // side width
            double sh = h - 2 * cornerHeight;      // side height
            double usw = uSize - 2 * cornerWidth;  // u side width
            double ush = vSize - 2 * cornerHeight; // v side height
            double x1 = x + cw, x2 = w - cw;
            double y1 = y + ch, y2 = h - ch;
            double u1 = u + cw, u2 = uSize - cw;
            double v1 = v + ch, v2 = vSize - cw;
            this.drawTexturedRect(x, y, cw, ch, u, v);               // top left corner
            this.drawTexturedRect(x2, y, cw, ch, u2, v);             // top right corner
            this.drawTexturedRect(x, y2, cw, ch, u, v2);             // bottom left corner
            this.drawTexturedRect(x2, y2, cw, ch, u2, v2);           // bottom right corner
            this.drawTexturedRect(x1, y, sw, ch, u1, v, usw, ch);    // top side
            this.drawTexturedRect(x1, y2, sw, ch, u1, v2, usw, ch);  // bottom side
            this.drawTexturedRect(x, y1, cw, sh, u, v1, cw, ush);    // left side
            this.drawTexturedRect(x2, y1, cw, sh, u2, v1, cw, ush);  // right side
            this.drawTexturedRect(x1, y1, sw, sh, u1, v1, usw, ush); // centre
        }

        public void drawGuiBackground(double x, double y, double w, double h) {
            this.bindTexture("gui/gui_background.png", 16, 16);
            this.setColor(0xffffff);
            this.drawBorderedRect(x, y, w, h, 0, 0, 16, 16, 4, 4);
        }

        public void drawTexturedRect(double x, double y, double w, double h) {
            this.drawTexturedRectUV(x, y, w, h, 0, 0, 1, 1);
        }

        public void drawTexturedRect(double x, double y, double w, double h, double u, double v) {
            this.drawTexturedRect(x, y, w, h, u, v, w, h);
        }

        public void drawTexturedRect(double x, double y, double w, double h, double u, double v, double us, double vs) {
            this.drawTexturedRectUV(x, y, w, h, u * this.gstate.uscale, v * this.gstate.vscale, us * this.gstate.uscale, vs * this.gstate.vscale);
        }

        public void drawTexturedRectUV(double x, double y, double w, double h,
                                       double u, double v, double us, double vs) {
            glBegin(GL_QUADS);
            glColor3f(this.gstate.red, this.gstate.green, this.gstate.blue);
            glTexCoord2d(u, v + vs);
            glVertex3d(x, y + h, this.zLevel);
            glTexCoord2d(u + us, v + vs);
            glVertex3d(x + w, y + h, this.zLevel);
            glTexCoord2d(u + us, v);
            glVertex3d(x + w, y, this.zLevel);
            glTexCoord2d(u, v);
            glVertex3d(x, y, this.zLevel);
            glEnd();
        }

        public void setColor(int hex) {
            this.setColor((hex >> 16) / 255.0, ((hex >> 8) & 0xff) / 255.0, (hex & 0xff) / 255.0);
        }

        public void setColor(double r, double g, double b) {
            this.gstate.red = (float) r;
            this.gstate.green = (float) g;
            this.gstate.blue = (float) b;
        }

        public void resetColor() {
            this.setColor(1, 1, 1);
        }

        public void setTextColor(int hex) {
            this.gstate.textColor = hex;
        }

        public void setTextColor(double red, double green, double blue) {
            this.setTextColor(packedColor(red, green, blue));
        }

        public void setTextShadow(boolean state) {
            this.gstate.textShadow = state;
        }

        public void drawString(String s, int x, int y) {
            this.fontRenderer.drawString(s, x, y, this.gstate.textColor, this.gstate.textShadow);
        }

        public void drawCenteredString(String s, int x, int y) {
            this.fontRenderer.drawString(s, x - this.fontRenderer.getStringWidth(s) / 2, y, this.gstate.textColor, this.gstate.textShadow);
        }

        public void drawRightAlignedString(String s, int x, int y) {
            this.fontRenderer.drawString(s, x - this.fontRenderer.getStringWidth(s), y, this.gstate.textColor, this.gstate.textShadow);
        }

        public void drawTitle(String s) {
            this.drawCenteredString(s, this.xSize / 2, 4);
        }

        public void drawPlayerInventoryName() {
            this.drawString(playerInventoryName(), 8, this.ySize - 96 + 2);
        }

        @Override
        protected void mouseClicked(int x, int y, int button) throws IOException {
            super.mouseClicked(x, y, button);
            this.mousePressed(x - this.guiLeft, y - this.guiTop, button);
        }

        protected void mousePressed(int x, int y, int button) {
            this.mouseWidget = this.root.dispatchMousePress(x, y, button);
            if (this.mouseWidget != null && this.mouseWidget.parent() != null) {
                this.closeOldFocus(this.mouseWidget);
                this.focusOn(this.mouseWidget);
                this.mouseWidget.mousePressed(new MouseCoords(this.mouseWidget, x - this.guiLeft, y - this.guiTop), button);
            }
        }

        @Override
        protected void mouseClickMove(int x, int y, int button, long timeSinceLastClick) {
            super.mouseClickMove(x, y, button, timeSinceLastClick);
            if (this.mouseWidget != null) {
                MouseCoords m = new MouseCoords(this.mouseWidget, x, y);
                this.mouseWidget.mouseDragged(m, button);
            }
        }

        @Override
        protected void mouseReleased(int x, int y, int button) {
            super.mouseReleased(x, y, button);
            if (this.mouseWidget != null) {
                MouseCoords m = new MouseCoords(this.mouseWidget, x, y);
                this.mouseWidget.mouseReleased(m, button);
            }
        }

        void closeOldFocus(IWidget clickedWidget) {
            if (!this.isFocused()) {
                IWidgetContainer parent = clickedWidget.parent();
                while (!this.isFocused())
                    parent = parent.parent();
                this.dispatchClosure(parent.getFocus());
            }
        }

        void dispatchClosure(IWidget target) {
            while (target != null) {
                target.close();
                target = this.getFocusOf(target);
            }
        }

        IWidget getFocusOf(IWidget widget) {
            if (widget instanceof IWidgetContainer)
                return ((IWidgetContainer) widget).getFocus();
            else
                return null;
        }

        @Override
        protected void keyTyped(char c, int key) throws IOException {
            if (!this.root.dispatchKeyPress(c, key)) {
                if (key == 1 || key == this.mc.gameSettings.keyBindInventory.getKeyCode())
                    this.close();
                else
                    super.keyTyped(c, key);
            }
        }

        public void focusOn(IWidget newFocus) {
            IWidgetContainer parent = newFocus.parent();
            if (parent != null) {
                IWidget oldFocus = parent.getFocus();
                if (this.isFocused()) {
                    if (oldFocus != newFocus) {
                        tellFocusChanged(oldFocus, false);
                        parent.setFocus(newFocus);
                        tellFocusChanged(newFocus, true);
                    }
                } else {
                    parent.setFocus(newFocus);
                    this.focusOn(parent);
                }
            }
        }

        public void focusChanged(boolean state) {
        }
    }

    public static class MouseCoords {

        int x, y;

        public MouseCoords(IWidget widget, int x, int y) {
            while (widget != null) {
                x -= widget.left();
                y -= widget.top();
                widget = widget.parent();
            }
            this.x = x;
            this.y = y;
        }

    }

    public static class Widget implements IWidget {

        public IWidgetContainer parent;
        //public IWidget focus;
        public int left, top, width, height;

        public Widget() {
        }

        public Widget(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public static int stringWidth(String s) {
            return Minecraft.getMinecraft().fontRenderer.getStringWidth(s);
        }

        @Override
        public IWidgetContainer parent() {
            return this.parent;
        }

        @Override
        public void setParent(IWidgetContainer widget) {
            this.parent = widget;
        }

        @Override
        public int left() {
            return this.left;
        }

        @Override
        public int top() {
            return this.top;
        }

        @Override
        public int width() {
            return this.width;
        }

        @Override
        public int height() {
            return this.height;
        }

        @Override
        public void setLeft(int x) {
            this.left = x;
        }

        @Override
        public void setTop(int y) {
            this.top = y;
        }

        @Override
        public void draw(Screen scr, int mouseX, int mouseY) {
        }

        @Override
        public void mousePressed(MouseCoords m, int button) {
        }

        @Override
        public void mouseDragged(MouseCoords m, int button) {
        }

        @Override
        public void mouseReleased(MouseCoords m, int button) {
        }

        @Override
        public boolean keyPressed(char c, int key) {
            return false;
        }

        @Override
        public void focusChanged(boolean state) {
        }

        @Override
        public void close() {
        }

        @Override
        public void layout() {
        }

        @Override
        public IWidget dispatchMousePress(int x, int y, int button) {
            //ArchitectureLog.info("BaseGui.Widget.dispatchMousePress: (%s, %s) in %s\n",
            //  x, y, getClass().getSimpleName());
            return this;
        }

        @Override
        public boolean dispatchKeyPress(char c, int key) {
            return this.keyPressed(c, key);
        }

        public void addPopup(int x, int y, IWidget widget) {
            IWidget w = this;
            while (!(w instanceof Root)) {
                x += w.left();
                y += w.top();
                w = w.parent();
            }
            ((Root) w).addPopup(x, y, widget);
        }

        public void removePopup() {
            Root root = this.getRoot();
            root.remove(this);
        }

        public Root getRoot() {
            IWidget w = this;
            while (w != null && !(w instanceof Root))
                w = w.parent();
            return (Root) w;
        }

    }

    public static class Group extends Widget implements IWidgetContainer {

        protected List<IWidget> widgets = new ArrayList<IWidget>();
        protected IWidget focus;

        @Override
        public IWidget getFocus() {
            return this.focus;
        }

        @Override
        public void setFocus(IWidget widget) {
            //ArchitectureLog.info("BaseGui.Group.setFocus: of %s to %s\n",
            //  getClass().getSimpleName(), widget.getClass().getSimpleName());
            this.focus = widget;
        }

        public void add(int left, int top, IWidget widget) {
            widget.setLeft(left);
            widget.setTop(top);
            widget.setParent(this);
            this.widgets.add(widget);
        }

        public void remove(IWidget widget) {
            this.widgets.remove(widget);
            if (this.getFocus() == widget) {
                if (isFocused(this))
                    tellFocusChanged(widget, false);
                this.setFocus(null);
            }
        }

        @Override
        public void draw(Screen scr, int mouseX, int mouseY) {
            super.draw(scr, mouseX, mouseY);
            for (IWidget w : this.widgets) {
                int dx = w.left(), dy = w.top();
                glPushMatrix();
                glTranslated(dx, dy, 0);
                w.draw(scr, mouseX - dx, mouseY - dy);
                glPopMatrix();
            }
        }

        @Override
        public IWidget dispatchMousePress(int x, int y, int button) {
            //ArchitectureLog.info("BaseGui.Group.dispatchMousePress: (%s, %s) in %s\n",
            //  x, y, getClass().getSimpleName());
            IWidget target = this.findWidget(x, y);
            if (target != null)
                return target.dispatchMousePress(x - target.left(), y - target.top(), button);
            else
                return this;
        }

        @Override
        public boolean dispatchKeyPress(char c, int key) {
            IWidget focus = this.getFocus();
            if (focus != null && focus.dispatchKeyPress(c, key))
                return true;
            else
                return super.dispatchKeyPress(c, key);
        }

        public IWidget findWidget(int x, int y) {
            for (int i = this.widgets.size() - 1; i >= 0; i--) {
                IWidget w = this.widgets.get(i);
                int l = w.left(), t = w.top();
                if (x >= l && y >= t && x < l + w.width() && y < t + w.height())
                    return w;
            }
            return null;
        }

        @Override
        public void layout() {
            for (IWidget w : this.widgets)
                w.layout();
        }

    }

    public static class Root extends Group {

        public Screen screen;
        public List<IWidget> popupStack;

        public Root(Screen screen) {
            this.screen = screen;
            this.popupStack = new ArrayList<IWidget>();
        }

        @Override
        public int width() {
            return this.screen.getWidth();
        }

        @Override
        public int height() {
            return this.screen.getHeight();
        }

        @Override
        public IWidget dispatchMousePress(int x, int y, int button) {
            IWidget w = this.topPopup();
            if (w == null)
                w = super.dispatchMousePress(x, y, button);
            return w;
        }

        @Override
        public void addPopup(int x, int y, IWidget widget) {
            this.add(x, y, widget);
            this.popupStack.add(widget);
            this.screen.focusOn(widget);
        }

        @Override
        public void remove(IWidget widget) {
            super.remove(widget);
            this.popupStack.remove(widget);
            this.focusTopPopup();
        }

        public IWidget topPopup() {
            int n = this.popupStack.size();
            if (n > 0)
                return this.popupStack.get(n - 1);
            else
                return null;
        }

        void focusTopPopup() {
            IWidget w = this.topPopup();
            if (w != null)
                this.screen.focusOn(w);
        }

    }

    //------------------------------------------------------------------------------------------------

    public static class FieldRef implements Ref {

        public Object target;
        public Field field;

        public FieldRef(Object target, String name) {
            try {
                this.target = target;
                this.field = target.getClass().getField(name);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Object get() {
            try {
                return this.field.get(this.target);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void set(Object value) {
            try {
                this.field.set(this.target, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    public static class PropertyRef implements Ref {

        public Object target;
        public Method getter, setter;

        public PropertyRef(Object target, String getterName, String setterName) {
            this.target = target;
            try {
                Class cls = target.getClass();
                this.getter = cls.getMethod(getterName);
                this.setter = cls.getMethod(setterName, this.getter.getReturnType());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Object get() {
            try {
                return this.getter.invoke(this.target);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void set(Object value) {
            try {
                this.setter.invoke(this.target, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    public static class MethodAction implements Action {

        Object target;
        Method method;

        public MethodAction(Object target, String name) {
            try {
                this.target = target;
                this.method = target.getClass().getMethod(name);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void perform() {
            try {
                this.method.invoke(this.target);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }

    //------------------------------------------------------------------------------------------------

    public static class GState {

        public GState previous;
        public double uscale, vscale;
        public float red, green, blue;
        public int textColor;
        public boolean textShadow;
        public ResourceLocation texture;

        public GState() {
            this.uscale = 1;
            this.vscale = 1;
            this.red = this.green = this.blue = 1;
            this.textColor = defaultTextColor;
            this.textShadow = false;
        }

        public GState(GState previous) {
            this.previous = previous;
            this.uscale = previous.uscale;
            this.vscale = previous.vscale;
            this.red = previous.red;
            this.green = previous.green;
            this.blue = previous.blue;
            this.textColor = previous.textColor;
            this.textShadow = previous.textShadow;
            this.texture = previous.texture;
        }

    }

    //------------------------------------------------------------------------------------------------

}
