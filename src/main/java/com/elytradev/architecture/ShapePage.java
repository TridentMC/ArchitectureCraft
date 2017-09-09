//------------------------------------------------------------------------------
//
//	 ArchitectureCraft - Pages for sawbench, etc.
//
//------------------------------------------------------------------------------

package com.elytradev.architecture;

public class ShapePage {

    public String title;
    public Shape[] shapes;

    public ShapePage(String title, Shape... shapes) {
        this.title = title;
        this.shapes = shapes;
    }

    public int size() {
        return shapes.length;
    }

    public Shape get(int i) {
        if (i >= 0 && i < shapes.length)
            return shapes[i];
        return null;
    }

}
