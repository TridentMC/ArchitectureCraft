import bmesh
import bpy
import math
from dataclasses import dataclass
from decimal import Decimal
from enum import Enum


@dataclass
class Vec3:
    x: float
    y: float
    z: float


@dataclass
class UV:
    u: float
    v: float


class Direction(Enum):
    NORTH = Vec3(0, 0, -1)
    SOUTH = Vec3(0, 0, 1)
    WEST = Vec3(-1, 0, 0)
    EAST = Vec3(1, 0, 0)
    UP = Vec3(0, 1, 0)
    DOWN = Vec3(0, -1, 0)


class SelectedDirection(Enum):
    DETECT = -1
    NORTH = 0
    SOUTH = 1
    WEST = 2
    EAST = 3
    UP = 4
    DOWN = 5


class OOBType(Enum):
    POSITIVE = 0
    NEGATIVE = 1
    NONE = 2


def get_selected_direction_from_prop(prop: str) -> SelectedDirection:
    if prop == "NORTH":
        return SelectedDirection.NORTH
    elif prop == "SOUTH":
        return SelectedDirection.SOUTH
    elif prop == "WEST":
        return SelectedDirection.WEST
    elif prop == "EAST":
        return SelectedDirection.EAST
    elif prop == "UP":
        return SelectedDirection.UP
    elif prop == "DOWN":
        return SelectedDirection.DOWN
    elif prop == "DETECT":
        return SelectedDirection.DETECT


def get_direction_from_selected_direction(
        selected_direction: SelectedDirection, normal: Vec3
) -> Direction:
    if selected_direction == SelectedDirection.DETECT:
        return get_direction_from_normal(normal)
    elif selected_direction == SelectedDirection.NORTH:
        return Direction.NORTH
    elif selected_direction == SelectedDirection.SOUTH:
        return Direction.SOUTH
    elif selected_direction == SelectedDirection.WEST:
        return Direction.WEST
    elif selected_direction == SelectedDirection.EAST:
        return Direction.EAST
    elif selected_direction == SelectedDirection.UP:
        return Direction.UP
    elif selected_direction == SelectedDirection.DOWN:
        return Direction.DOWN


def get_direction_from_normal(normal: Vec3) -> Direction:
    # Normalize the normal, preventing any division by zero errors
    normalized = Vec3(
        normal.x / abs(normal.x) if normal.x != 0 else 0,
        normal.y / abs(normal.y) if normal.y != 0 else 0,
        normal.z / abs(normal.z) if normal.z != 0 else 0,
    )
    if normalized.y > 0:
        return Direction.UP
    elif normalized.y < 0:
        return Direction.DOWN
    elif normalized.z > 0:
        return Direction.SOUTH
    elif normalized.z < 0:
        return Direction.NORTH
    elif normalized.x > 0:
        return Direction.EAST
    elif normalized.x < 0:
        return Direction.WEST


def calculate_uv_at_vertex(
        direction: Direction, vertex: Vec3, x_oob: OOBType, y_oob: OOBType, z_oob: OOBType
) -> UV:
    # Offset the vertex by 0.5 for the calculation so that the corners are at 0, 0, 0, 1, 1, 1
    offset_vertex = Vec3(vertex.x + 0.5, vertex.y + 0.5, vertex.z + 0.5)

    x = Decimal(offset_vertex.x)
    if x > 1:
        x = Decimal(x) % Decimal(1)
    elif x < 0:
        x = Decimal(1) + (Decimal(x) % Decimal(1))

    y = Decimal(offset_vertex.y)
    if y > 1:
        y = Decimal(y) % Decimal(1)
    elif y < 0:
        y = Decimal(1) + (Decimal(y) % Decimal(1))

    z = Decimal(offset_vertex.z)
    if z > 1:
        z = Decimal(z) % Decimal(1)
    elif z < 0:
        z = Decimal(1) + (Decimal(z) % Decimal(1))

    # Make a new vector with the adjusted values, rounding each to the same nearest multiple of 1/2048
    adjusted_vertex = Vec3(
        round(x * 2048) / 2048, round(y * 2048) / 2048, round(z * 2048) / 2048
    )

    if x_oob == OOBType.POSITIVE:
        if math.isclose(adjusted_vertex.x, 1):
            print("X OOB positive")
            adjusted_vertex.x = 0
    elif x_oob == OOBType.NEGATIVE:
        if math.isclose(adjusted_vertex.x, 0):
            print("X OOB negative")
            adjusted_vertex.x = 1

    if y_oob == OOBType.POSITIVE:
        if math.isclose(adjusted_vertex.y, 1):
            print("Y OOB positive")
            adjusted_vertex.y = 0
    elif y_oob == OOBType.NEGATIVE:
        if math.isclose(adjusted_vertex.y, 0):
            print("Y OOB negative")
            adjusted_vertex.y = 1

    if z_oob == OOBType.POSITIVE:
        if math.isclose(adjusted_vertex.z, 1):
            print("Z OOB positive")
            adjusted_vertex.z = 0
    elif z_oob == OOBType.NEGATIVE:
        if math.isclose(adjusted_vertex.z, 0):
            print("Z OOB negative")
            adjusted_vertex.z = 1

    print(f"Placed vertex within bounds {offset_vertex} -> {adjusted_vertex}")

    if direction == Direction.DOWN:
        return UV(u=adjusted_vertex.x, v=(adjusted_vertex.z - 1) * -1)
    elif direction == Direction.UP:
        return UV(u=adjusted_vertex.x, v=adjusted_vertex.z)
    elif direction == Direction.NORTH:
        return UV(u=(adjusted_vertex.x - 1) * -1, v=(adjusted_vertex.y - 1) * -1)
    elif direction == Direction.SOUTH:
        return UV(u=adjusted_vertex.x, v=(adjusted_vertex.y - 1) * -1)
    elif direction == Direction.WEST:
        return UV(u=adjusted_vertex.z, v=(adjusted_vertex.y - 1) * -1)
    elif direction == Direction.EAST:
        return UV(u=(adjusted_vertex.z - 1) * -1, v=(adjusted_vertex.y - 1) * -1)
    else:
        return UV(u=0, v=0)


class AutoUVButtonOperator(bpy.types.Operator):
    bl_idname = "uv.auto_uv_direction"
    bl_label = "Auto UV Direction"
    bl_options = {"REGISTER", "UNDO"}

    direction_prop: bpy.props.EnumProperty(
        name="Direction",
        description="The direction to auto UV",
        items=[
            ("DETECT", "Detect", "Detect the direction from the normal"),
            ("NORTH", "North (-Z)", "North"),
            ("SOUTH", "South (+Z)", "South"),
            ("WEST", "West (-X)", "West"),
            ("EAST", "East (+X)", "East"),
            ("UP", "Up (+Y)", "Up"),
            ("DOWN", "Down (-Y)", "Down"),
        ],
        default="DETECT",
    )

    @classmethod
    def poll(cls, context):
        return context.active_object is not None

    def execute(self, context):
        # Confirm that we're in edit mode and there are editable meshes available
        if context.mode != "EDIT_MESH":
            self.report({"ERROR"}, "Must be in edit mode")
            return {"CANCELLED"}

        # Get all the editable objects containing meshes
        objects = [obj for obj in context.editable_objects if obj.type == "MESH"]

        # Confirm that there are editable meshes
        if len(objects) == 0:
            self.report({"ERROR"}, "No editable meshes")
            return {"CANCELLED"}

        # Iterate over each selected mesh object and apply the UVs
        total_affected_objects = 0
        total_affected_faces = 0
        for obj in objects:
            (affected_object, affected_faces) = self.apply_to_mesh(obj, context)
            total_affected_objects += affected_object
            total_affected_faces += affected_faces

        # Display a message in the viewport indicating how many faces were affected across the selected objects
        self.report(
            {"INFO"},
            f"Auto UV'd {total_affected_faces} faces across {total_affected_objects} objects",
        )

        return {"FINISHED"}

    def apply_to_mesh(self, mesh_obj, context):
        if mesh_obj.mode != "EDIT":
            print(f"Object {mesh_obj.name} is not in edit mode, skipping")
            return (0, 0)

        # Load the bmesh from the passed mesh object
        bm = bmesh.from_edit_mesh(mesh_obj.data)

        # Get the UV layer
        uv_layer = bm.loops.layers.uv.active
        if uv_layer is None:
            self.report({"ERROR"}, "No UV layer found on mesh " + mesh_obj.name)
            return {"CANCELLED"}

        # Get the selected faces
        selected_faces = [face for face in bm.faces if face.select]
        sel_dir = get_selected_direction_from_prop(self.direction_prop)

        # Loop through the faces
        for face in selected_faces:
            # Get the direction of the face
            direction = get_direction_from_selected_direction(sel_dir, face.normal)
            # Determine the OOB types for each axis by iterating through the vertices
            x_oob = OOBType.NONE
            y_oob = OOBType.NONE
            z_oob = OOBType.NONE
            for vertex in face.verts:
                if vertex.co.x > 0.5:
                    x_oob = OOBType.POSITIVE if x_oob == OOBType.NONE else x_oob
                elif vertex.co.x < -0.5:
                    x_oob = OOBType.NEGATIVE if x_oob == OOBType.NONE else x_oob
                if vertex.co.y > 0.5:
                    y_oob = OOBType.POSITIVE if y_oob == OOBType.NONE else y_oob
                elif vertex.co.y < -0.5:
                    y_oob = OOBType.NEGATIVE if y_oob == OOBType.NONE else y_oob
                if vertex.co.z > 0.5:
                    z_oob = OOBType.POSITIVE if z_oob == OOBType.NONE else z_oob
                elif vertex.co.z < -0.5:
                    z_oob = OOBType.NEGATIVE if z_oob == OOBType.NONE else z_oob

            # Loop through the loops of the face
            for loop in face.loops:
                # Calculate the UV at the vertex
                uv = calculate_uv_at_vertex(
                    direction, loop.vert.co, x_oob, y_oob, z_oob
                )
                print(
                    f"Calculating UV for vertex facing {direction} {loop.vert.co} -> {uv}"
                )
                # Set the UV
                loop[uv_layer].uv = (uv.u, uv.v)

        # Update the mesh
        bmesh.update_edit_mesh(mesh_obj.data)
        # Return the number of affected faces
        return (len(selected_faces) > 0, len(selected_faces))


# Create the dropdown menu in the UV editor to start the operator with a direction
class AutoUVDirectionMenu(bpy.types.Menu):
    bl_idname = "uv.auto_uv_direction_menu"
    bl_label = "Auto UV Direction"

    def draw(self, context):
        layout = self.layout

        layout.operator(
            AutoUVButtonOperator.bl_idname, text="Detect"
        ).direction_prop = "DETECT"
        layout.operator(
            AutoUVButtonOperator.bl_idname, text="North"
        ).direction_prop = "NORTH"
        layout.operator(
            AutoUVButtonOperator.bl_idname, text="South"
        ).direction_prop = "SOUTH"
        layout.operator(
            AutoUVButtonOperator.bl_idname, text="West"
        ).direction_prop = "WEST"
        layout.operator(
            AutoUVButtonOperator.bl_idname, text="East"
        ).direction_prop = "EAST"
        layout.operator(AutoUVButtonOperator.bl_idname, text="Up").direction_prop = "UP"
        layout.operator(
            AutoUVButtonOperator.bl_idname, text="Down"
        ).direction_prop = "DOWN"


# Add the menu to the UV editor
def menu_func(self, context):
    self.layout.menu("uv.auto_uv_direction_menu")


# Register the operator and the menu
def register():
    bpy.utils.register_class(AutoUVButtonOperator)
    bpy.utils.register_class(AutoUVDirectionMenu)
    bpy.types.VIEW3D_MT_uv_map.append(menu_func)


def unregister():
    bpy.utils.unregister_class(AutoUVButtonOperator)
    bpy.utils.unregister_class(AutoUVDirectionMenu)
    bpy.types.VIEW3D_MT_uv_map.remove(menu_func)


# Set the addon info
bl_info = {
    "name": "Auto UV",
    "author": "Benjamin K. (darkevilmac)",
    "version": (1, 1),
    "blender": (3, 0, 0),
    "location": "View3D > UV Editor > UVs > Auto UV",
    "description": "Automatically sets the UVs for a face based on the direction it is facing",
    "warning": "",
    "wiki_url": "",
    "category": "UV",
}

if __name__ == "__main__":
    register()
