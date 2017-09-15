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

import com.elytradev.architecture.common.utils.IModHolder;
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

    public static class Screen extends GuiContainer implements IModHolder {

        protected BaseMod mod;
        protected Root root;
        protected String title;
        protected Tessellator tess;
        protected BufferBuilder vb;
        protected IWidget mouseWidget;
        protected GState gstate;

        public Screen(Container container, int width, int height) {
            super(container);
            xSize = width;
            ySize = height;
            root = new Root(this);
            tess = Tessellator.getInstance();
            vb = tess.getBuffer();
            gstate = new GState();
        }

        public Screen(BaseContainer container) {
            this(container, container.xSize, container.ySize);
        }

        public static String playerInventoryName() {
            return I18n.translateToLocal("container.inventory");
        }

        public int getWidth() {
            return xSize;
        }

        public int getHeight() {
            return ySize;
        }

        @Override
        public void setMod(BaseMod mod) {
            this.mod = mod;
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
            root.layout();
        }

        @Override
        protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
            GL11.glPushMatrix();
            GL11.glTranslatef(guiLeft, guiTop, 0.0F);
            drawBackgroundLayer();
            if (title != null)
                drawTitle(title);
            root.draw(this, mouseX - guiLeft, mouseY - guiTop);
            GL11.glPopMatrix();
        }

        protected void drawBackgroundLayer() {
            drawGuiBackground(0, 0, xSize, ySize);
        }

        @Override
        protected void drawGuiContainerForegroundLayer(int par1, int par2) {
            drawForegroundLayer();
        }

        protected void drawForegroundLayer() {
        }

        public void close() {
            dispatchClosure(root);
            onClose();
            mc.player.closeScreen();
        }

        protected void onClose() {
        }

        public void bindTexture(String path) {
            bindTexture(path, 1, 1);
        }

        public void bindTexture(String path, int usize, int vsize) {
            bindTexture(mod.client.textureLocation(path), usize, vsize);
        }

        public void bindTexture(ResourceLocation rsrc) {
            bindTexture(rsrc, 1, 1);
        }

        public void bindTexture(ResourceLocation rsrc, int usize, int vsize) {
            gstate.texture = rsrc;
            mc.getTextureManager().bindTexture(rsrc);
            gstate.uscale = 1.0 / usize;
            gstate.vscale = 1.0 / vsize;
        }

        public void gSave() {
            gstate = new GState(gstate);
        }

        public void gRestore() {
            if (gstate.previous != null) {
                gstate = gstate.previous;
                mc.getTextureManager().bindTexture(gstate.texture);
            } else
                System.out.printf("BaseGui: Warning: Graphics state stack underflow\n");
        }

        public void drawRect(double x, double y, double w, double h) {
            glDisable(GL_TEXTURE_2D);
            glColor3d(gstate.red, gstate.green, gstate.blue);
            glBegin(GL_QUADS);
            glVertex3d(x, y + h, zLevel);
            glVertex3d(x + w, y + h, zLevel);
            glVertex3d(x + w, y, zLevel);
            glVertex3d(x, y, zLevel);
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
            drawTexturedRect(x, y, cw, ch, u, v);               // top left corner
            drawTexturedRect(x2, y, cw, ch, u2, v);             // top right corner
            drawTexturedRect(x, y2, cw, ch, u, v2);             // bottom left corner
            drawTexturedRect(x2, y2, cw, ch, u2, v2);           // bottom right corner
            drawTexturedRect(x1, y, sw, ch, u1, v, usw, ch);    // top side
            drawTexturedRect(x1, y2, sw, ch, u1, v2, usw, ch);  // bottom side
            drawTexturedRect(x, y1, cw, sh, u, v1, cw, ush);    // left side
            drawTexturedRect(x2, y1, cw, sh, u2, v1, cw, ush);  // right side
            drawTexturedRect(x1, y1, sw, sh, u1, v1, usw, ush); // centre
        }

        public void drawGuiBackground(double x, double y, double w, double h) {
            bindTexture("gui/gui_background.png", 16, 16);
            setColor(0xffffff);
            drawBorderedRect(x, y, w, h, 0, 0, 16, 16, 4, 4);
        }

        public void drawTexturedRect(double x, double y, double w, double h) {
            drawTexturedRectUV(x, y, w, h, 0, 0, 1, 1);
        }

        public void drawTexturedRect(double x, double y, double w, double h, double u, double v) {
            drawTexturedRect(x, y, w, h, u, v, w, h);
        }

        public void drawTexturedRect(double x, double y, double w, double h, double u, double v, double us, double vs) {
            drawTexturedRectUV(x, y, w, h, u * gstate.uscale, v * gstate.vscale, us * gstate.uscale, vs * gstate.vscale);
        }

        public void drawTexturedRectUV(double x, double y, double w, double h,
                                       double u, double v, double us, double vs) {
            glBegin(GL_QUADS);
            glColor3f(gstate.red, gstate.green, gstate.blue);
            glTexCoord2d(u, v + vs);
            glVertex3d(x, y + h, zLevel);
            glTexCoord2d(u + us, v + vs);
            glVertex3d(x + w, y + h, zLevel);
            glTexCoord2d(u + us, v);
            glVertex3d(x + w, y, zLevel);
            glTexCoord2d(u, v);
            glVertex3d(x, y, zLevel);
            glEnd();
        }

        public void setColor(int hex) {
            setColor((hex >> 16) / 255.0, ((hex >> 8) & 0xff) / 255.0, (hex & 0xff) / 255.0);
        }

        public void setColor(double r, double g, double b) {
            gstate.red = (float) r;
            gstate.green = (float) g;
            gstate.blue = (float) b;
        }

        public void resetColor() {
            setColor(1, 1, 1);
        }

        public void setTextColor(int hex) {
            gstate.textColor = hex;
        }

        public void setTextColor(double red, double green, double blue) {
            setTextColor(packedColor(red, green, blue));
        }

        public void setTextShadow(boolean state) {
            gstate.textShadow = state;
        }

        public void drawString(String s, int x, int y) {
            fontRenderer.drawString(s, x, y, gstate.textColor, gstate.textShadow);
        }

        public void drawCenteredString(String s, int x, int y) {
            fontRenderer.drawString(s, x - fontRenderer.getStringWidth(s) / 2, y, gstate.textColor, gstate.textShadow);
        }

        public void drawRightAlignedString(String s, int x, int y) {
            fontRenderer.drawString(s, x - fontRenderer.getStringWidth(s), y, gstate.textColor, gstate.textShadow);
        }

        public void drawTitle(String s) {
            drawCenteredString(s, xSize / 2, 4);
        }

        public void drawPlayerInventoryName() {
            drawString(playerInventoryName(), 8, ySize - 96 + 2);
        }

        @Override
        protected void mouseClicked(int x, int y, int button) throws IOException {
            super.mouseClicked(x, y, button);
            mousePressed(x - guiLeft, y - guiTop, button);
        }

        protected void mousePressed(int x, int y, int button) {
            mouseWidget = root.dispatchMousePress(x, y, button);
            if (mouseWidget != null && mouseWidget.parent() != null) {
                closeOldFocus(mouseWidget);
                focusOn(mouseWidget);
                mouseWidget.mousePressed(new MouseCoords(mouseWidget, x - guiLeft, y - guiTop), button);
            }
        }

        @Override
        protected void mouseClickMove(int x, int y, int button, long timeSinceLastClick) {
            super.mouseClickMove(x, y, button, timeSinceLastClick);
            if (mouseWidget != null) {
                MouseCoords m = new MouseCoords(mouseWidget, x, y);
                mouseWidget.mouseDragged(m, button);
            }
        }

        @Override
        protected void mouseReleased(int x, int y, int button) {
            super.mouseReleased(x, y, button);
            if (mouseWidget != null) {
                MouseCoords m = new MouseCoords(mouseWidget, x, y);
                mouseWidget.mouseReleased(m, button);
            }
        }

        void closeOldFocus(IWidget clickedWidget) {
            if (!isFocused()) {
                IWidgetContainer parent = clickedWidget.parent();
                while (!isFocused())
                    parent = parent.parent();
                dispatchClosure(parent.getFocus());
            }
        }

        void dispatchClosure(IWidget target) {
            while (target != null) {
                target.close();
                target = getFocusOf(target);
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
            if (!root.dispatchKeyPress(c, key)) {
                if (key == 1 || key == mc.gameSettings.keyBindInventory.getKeyCode())
                    close();
                else
                    super.keyTyped(c, key);
            }
        }

        public void focusOn(IWidget newFocus) {
            IWidgetContainer parent = newFocus.parent();
            if (parent != null) {
                IWidget oldFocus = parent.getFocus();
                if (isFocused()) {
                    if (oldFocus != newFocus) {
                        tellFocusChanged(oldFocus, false);
                        parent.setFocus(newFocus);
                        tellFocusChanged(newFocus, true);
                    }
                } else {
                    parent.setFocus(newFocus);
                    focusOn(parent);
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
            return parent;
        }

        @Override
        public void setParent(IWidgetContainer widget) {
            parent = widget;
        }

        @Override
        public int left() {
            return left;
        }

        @Override
        public int top() {
            return top;
        }

        @Override
        public int width() {
            return width;
        }

        @Override
        public int height() {
            return height;
        }

        @Override
        public void setLeft(int x) {
            left = x;
        }

        @Override
        public void setTop(int y) {
            top = y;
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
            //System.out.printf("BaseGui.Widget.dispatchMousePress: (%s, %s) in %s\n",
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
            Root root = getRoot();
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
            return focus;
        }

        @Override
        public void setFocus(IWidget widget) {
            //System.out.printf("BaseGui.Group.setFocus: of %s to %s\n",
            //  getClass().getSimpleName(), widget.getClass().getSimpleName());
            focus = widget;
        }

        public void add(int left, int top, IWidget widget) {
            widget.setLeft(left);
            widget.setTop(top);
            widget.setParent(this);
            widgets.add(widget);
        }

        public void remove(IWidget widget) {
            widgets.remove(widget);
            if (getFocus() == widget) {
                if (isFocused(this))
                    tellFocusChanged(widget, false);
                setFocus(null);
            }
        }

        @Override
        public void draw(Screen scr, int mouseX, int mouseY) {
            super.draw(scr, mouseX, mouseY);
            for (IWidget w : widgets) {
                int dx = w.left(), dy = w.top();
                glPushMatrix();
                glTranslated(dx, dy, 0);
                w.draw(scr, mouseX - dx, mouseY - dy);
                glPopMatrix();
            }
        }

        @Override
        public IWidget dispatchMousePress(int x, int y, int button) {
            //System.out.printf("BaseGui.Group.dispatchMousePress: (%s, %s) in %s\n",
            //  x, y, getClass().getSimpleName());
            IWidget target = findWidget(x, y);
            if (target != null)
                return target.dispatchMousePress(x - target.left(), y - target.top(), button);
            else
                return this;
        }

        @Override
        public boolean dispatchKeyPress(char c, int key) {
            IWidget focus = getFocus();
            if (focus != null && focus.dispatchKeyPress(c, key))
                return true;
            else
                return super.dispatchKeyPress(c, key);
        }

        public IWidget findWidget(int x, int y) {
            for (int i = widgets.size() - 1; i >= 0; i--) {
                IWidget w = widgets.get(i);
                int l = w.left(), t = w.top();
                if (x >= l && y >= t && x < l + w.width() && y < t + w.height())
                    return w;
            }
            return null;
        }

        @Override
        public void layout() {
            for (IWidget w : widgets)
                w.layout();
        }

    }

    public static class Root extends Group {

        public Screen screen;
        public List<IWidget> popupStack;

        public Root(Screen screen) {
            this.screen = screen;
            popupStack = new ArrayList<IWidget>();
        }

        @Override
        public int width() {
            return screen.getWidth();
        }

        @Override
        public int height() {
            return screen.getHeight();
        }

        @Override
        public IWidget dispatchMousePress(int x, int y, int button) {
            IWidget w = topPopup();
            if (w == null)
                w = super.dispatchMousePress(x, y, button);
            return w;
        }

        @Override
        public void addPopup(int x, int y, IWidget widget) {
            add(x, y, widget);
            popupStack.add(widget);
            screen.focusOn(widget);
        }

        @Override
        public void remove(IWidget widget) {
            super.remove(widget);
            popupStack.remove(widget);
            focusTopPopup();
        }

        public IWidget topPopup() {
            int n = popupStack.size();
            if (n > 0)
                return popupStack.get(n - 1);
            else
                return null;
        }

        void focusTopPopup() {
            IWidget w = topPopup();
            if (w != null)
                screen.focusOn(w);
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
                return field.get(target);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void set(Object value) {
            try {
                field.set(target, value);
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
                getter = cls.getMethod(getterName);
                setter = cls.getMethod(setterName, getter.getReturnType());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Object get() {
            try {
                return getter.invoke(target);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void set(Object value) {
            try {
                setter.invoke(target, value);
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
                method = target.getClass().getMethod(name);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void perform() {
            try {
                method.invoke(target);
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
            uscale = 1;
            vscale = 1;
            red = green = blue = 1;
            textColor = defaultTextColor;
            textShadow = false;
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
