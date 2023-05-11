from pathlib import Path
import bpy
import bmesh

from bpy.props import StringProperty, BoolProperty, EnumProperty
from bpy_extras.io_utils import ImportHelper, ExportHelper

import objson.common_types as common_types
import objson.objson_types as objson_types

import objson.serializer as serializer
import objson.deserializer as deserializer


# This is the main file for the addon, the addon provides the following functionality:
# - Importing .objson files
# - Exporting .objson files

# region Addon Info
bl_info = {
    "name": "OBJSON",
    "author": "Benjamin K. (darkevilmac)",
    "version": (0, 0, 1),
    "blender": (3, 0, 0),
    "location": "File > Import-Export",
    "description": "Import-Export OBJSON files",
    "category": "Import-Export",
}
# endregion

# region Import

# Create the import operator, capable of importing one or more .objson files
class ImportOBJSON(bpy.types.Operator, ImportHelper):
    bl_idname = "import_scene.objson"
    bl_label = "Import OBJSON"
    bl_options = {"PRESET", "UNDO"}

    # ImportHelper mixin class uses this
    filename_ext = ".objson"
    filter_glob: StringProperty(
        default="*.objson",
        options={"HIDDEN"},
        maxlen=255,  # Max internal buffer length, longer would be clamped.
    )

    # List of operator properties, the attributes will be assigned
    # to the class instance from the operator settings before calling.
    files: bpy.props.CollectionProperty(
        name="File Path",
        type=bpy.types.OperatorFileListElement,
    )
    directory: bpy.props.StringProperty(
        subtype="DIR_PATH",
    )

    def draw(self, context):
        layout = self.layout

        row = layout.row()
        row.label(text="OBJSON Import Options")

        row = layout.row()
        row.prop(self, "use_setting")

    def execute(self, context):
        models = []
        # Iterate over each selected file, create a Path object and pass it to the import function
        for file in self.files:
            path = Path(self.directory) / file.name
            models.append(deserializer.deserialize_objson(path))

        # Load the texture materials from the directory containing the .objson file
        texture_dir = Path(self.directory)
        mat_0 = self.create_texture_material("texture_0", texture_dir / "texture_0.png")
        mat_1 = self.create_texture_material("texture_1", texture_dir / "texture_1.png")

        # Iterate over each model and create a new object for it
        for model in models:
            self.add_model_to_scene(model, mat_0, mat_1)

        self.report(
            {"INFO"},
            f"Imported {len(models)} models, with a total of {sum([len(m.parts) for m in models])} parts",
        )

        return {"FINISHED"}

    def create_texture_material(self, name, texture_path):
        if name in bpy.data.materials:
            return bpy.data.materials[name]
        mat = bpy.data.materials.new(name)
        mat.use_nodes = True
        mat.node_tree.nodes.clear()
        mat_output = mat.node_tree.nodes.new("ShaderNodeOutputMaterial")
        mat_emission = mat.node_tree.nodes.new("ShaderNodeEmission")
        mat_texture = mat.node_tree.nodes.new("ShaderNodeTexImage")
        mat_texture.image = bpy.data.images.load(texture_path)
        mat_texture.interpolation = "Closest"
        mat_texture.extension = "REPEAT"
        mat_texture.image.colorspace_settings.name = "sRGB"
        mat_emission.inputs[0].default_value = (1, 1, 1, 1)
        mat_emission.inputs[1].default_value = 1
        mat.node_tree.links.new(mat_emission.outputs[0], mat_output.inputs[0])
        mat.node_tree.links.new(mat_texture.outputs[0], mat_emission.inputs[0])
        return mat

    def add_model_to_scene(self, model: common_types.ModelData, mat_0, mat_1):
        # Create the parent object for the model
        model_obj = bpy.data.objects.new(model.name, None)
        model_obj.location = (0, 0, 0)
        bpy.context.collection.objects.link(model_obj)

        # Iterate over each part of the model and create a new object and mesh for it
        for part in model.parts:
            part_name = f"{model.name}.{part.name}"

            faces = part.faces
            vertices = []
            triangles = []
            quads = []
            for face in faces:
                for triangle in [t for t in face.triangles]:
                    triangles.append(
                        common_types.Triangle(
                            triangle.v0 + len(vertices),
                            triangle.v1 + len(vertices),
                            triangle.v2 + len(vertices),
                            triangle.texture,
                        )
                    )
                for quad in [q for q in face.quads]:
                    quads.append(
                        common_types.Quad(
                            quad.v0 + len(vertices),
                            quad.v1 + len(vertices),
                            quad.v2 + len(vertices),
                            quad.v3 + len(vertices),
                            quad.texture,
                        )
                    )
                for vertex in face.vertices:
                    vertices.append(vertex)

            # Create the mesh using the vertices and triangles
            mesh = bpy.data.meshes.new(part_name)
            mesh.from_pydata(
                [
                    common_types.Vec3(
                        round(v.pos.x * 2048) / 2048,
                        round(v.pos.y * 2048) / 2048,
                        round(v.pos.z * 2048) / 2048,
                    )
                    for v in vertices
                ],
                [],
                [*[t[:3] for t in triangles], *[q[:4] for q in quads]],
            )

            # Apply corrections to the mesh
            corrections = mesh.validate(verbose=True, clean_customdata=True)

            # Create the bmesh and apply the UVs and materials
            bm = bmesh.new()
            bm.from_mesh(mesh)
            uv_layer = bm.loops.layers.uv.new()
            for face in bm.faces:
                for loop in face.loops:
                    loop[uv_layer].uv = vertices[loop.vert.index].uv

            for face in bm.faces:
                if triangles[face.index].texture == 0:
                    face.material_index = 0
                else:
                    face.material_index = 1

            bm.to_mesh(mesh)
            bm.free()

            # Create the object and link it to the scene
            part_obj = bpy.data.objects.new(part_name, mesh)
            part_obj.data.materials.append(mat_0)
            part_obj.data.materials.append(mat_1)
            part_obj.parent = model_obj
            bpy.context.collection.objects.link(part_obj)

        return model_obj


def menu_func_import(self, context):
    self.layout.operator(ImportOBJSON.bl_idname, text="OBJSON (.objson)")


# endregion

# region Export
# Export operator, uses the serializer to export the selected object to an .objson file
# The default file name is always set to the name of the selected object with the .objson extension
# If there's more than one object selected, then we should instead select a directory to export to.
class ExportOBJSON(bpy.types.Operator, ExportHelper):
    """Export an object to an OBJSON file"""

    bl_idname = "export_scene.objson"
    bl_label = "Export OBJSON"
    bl_options = {"PRESET", "UNDO"}

    # ExportHelper mixin class uses this
    filename_ext = ".objson"
    filter_glob: bpy.props.StringProperty(
        default="*.objson",
        options={"HIDDEN"},
    )

    def execute(self, context):
        total_models = 0
        total_parts = 0
        # Check if we're exporting a single mesh or multiple meshes
        if len(context.selected_objects) > 1:
            # We should already have a directory selected from our invoke function, so we just need to iterate over the root objects and export them
            output_path = Path(self.filepath)
            output_path.mkdir(parents=True, exist_ok=True)
            print(f"Selecting more than one object, exporting to {output_path.absolute()}")
            for obj in context.selected_objects:
                path = Path(self.filepath) / f"{obj.name}.objson"
                print(f"Exporting {obj.name} to {path.absolute()}")
                model = serializer.create_objson_from_object(obj)
                serializer.serialize_objson(model, path)
                total_models += 1
                total_parts += len(model.parts)
        else:
            # Get the selected object
            obj = context.active_object
            print(f"Exporting {obj.name} to {self.filepath}")

            # Create the model data from the object
            model = serializer.create_objson_from_object(obj)
            # Create the output directory if it doesn't exist
            output_dir = Path(self.filepath).parent
            if not output_dir.exists():
                output_dir.mkdir(parents=True)
            # Serialize the model data to the output file
            output_path = Path(self.filepath)
            serializer.serialize_objson(model, output_path)
            total_models += 1
            total_parts += len(model.parts)

        self.report({"INFO"}, f"Exported {total_models} models with a total part count of {total_parts}")

        return {"FINISHED"}

    def invoke(self, context, event):
        # Check if there's more than one object selected, if so then we should be asking for a directory instead of a file
        if len(context.selected_objects) > 1:
            print("Multiple objects selected, asking for directory")
            self.filename_ext = ""
            self.filter_glob = "*"
            self.filepath = str(Path(bpy.data.filepath).parent)
        else:
            print("Single object selected, asking for file")
            self.filename_ext = ".objson"
            self.filter_glob = "*.objson"
            self.filepath = context.active_object.name + ".objson"

        return super().invoke(context, event)


def menu_func_export(self, context):
    self.layout.operator(ExportOBJSON.bl_idname, text="OBJSON (.objson)")


# endregion

# region Registration
classes = (
    ImportOBJSON,
    ExportOBJSON,
)


def register():
    import importlib
    importlib.reload(common_types)
    importlib.reload(objson_types)
    importlib.reload(serializer)
    importlib.reload(deserializer)
    
    for cls in classes:
        bpy.utils.register_class(cls)

    bpy.types.TOPBAR_MT_file_import.append(menu_func_import)
    bpy.types.TOPBAR_MT_file_export.append(menu_func_export)


def unregister():
    for cls in reversed(classes):
        bpy.utils.unregister_class(cls)

    bpy.types.TOPBAR_MT_file_import.remove(menu_func_import)
    bpy.types.TOPBAR_MT_file_export.remove(menu_func_export)


# endregion