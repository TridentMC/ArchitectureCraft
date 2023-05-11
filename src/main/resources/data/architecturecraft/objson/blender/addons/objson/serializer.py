import dataclasses
import json
from pathlib import Path
from objson.objson_types import *
from objson.common_types import *
import bpy
import bmesh


class EnhancedJSONEncoder(json.JSONEncoder):

    def default(self, o):
        if dataclasses.is_dataclass(o):
            return dataclasses.asdict(o)
        return super().default(o)


def serialize_objson(objson: OBJSON, export_path: Path):
    export_path.parent.mkdir(parents=True, exist_ok=True)
    export_path.write_text(
        json.dumps(objson, indent=4, cls=EnhancedJSONEncoder))


def create_objson_from_object(scene_object: bpy.types.Object) -> OBJSON:
    # First create the ModelData object, then we can create the OBJSON object with the shared face data.
    model_data = get_model_data(scene_object)

    # Group all the faces from each part together based on their normal.
    faces = {}
    for part in model_data.parts:
        for face in part.faces:
            key = face.normal
            if key not in faces:
                faces[key] = []
            faces[key].append(face)

    # Sort the faces by their normal
    faces = sorted(faces.items(), key=lambda f: f[0])

    # Remove duplicate vertices from the faces, then sort the vertices by their position.
    for i, (key, n_faces) in enumerate(faces):
        vertices = []
        for face in n_faces:
            for vertex in face.vertices:
                if vertex not in vertices:
                    vertices.append(vertex)
        vertices = sorted(vertices, key=lambda v: v.pos)
        faces[i] = (key, n_faces, vertices)
    # Create the OBJSON faces.
    objson_faces = []
    for (normal, _, verts) in faces:
        # Create the vertices for the face.
        objson_vertices = []
        for vertex in verts:
            objson_vertices.append(
                OBJSONVertex(vertex.pos, vertex.normal, vertex.uv))

        # Create the face.
        face_dir = get_int_direction_from_normal(normal)
        objson_faces.append(OBJSONFace(normal, objson_vertices, face_dir))
    face_normals = [normal for (normal, _, _) in faces]
    vertices = [vertices for (_, _, vertices) in faces]
    # Create the parts and triangles pointing to the new face indices
    parts = []
    for part in model_data.parts:
        triangles = []
        quads = []
        for face in part.faces:
            key = face.normal
            face_index = face_normals.index(key)
            for triangle in face.triangles:
                vertex_0_data = face.vertices[triangle.v0]
                vertex_1_data = face.vertices[triangle.v1]
                vertex_2_data = face.vertices[triangle.v2]
                vertex_0 = vertices[face_index].index(vertex_0_data)
                vertex_1 = vertices[face_index].index(vertex_1_data)
                vertex_2 = vertices[face_index].index(vertex_2_data)
                cull_face = calculate_cull_face(
                    [vertex_0_data.pos, vertex_1_data.pos, vertex_2_data.pos],
                    get_direction_from_normal(face.normal),
                )
                triangles.append(
                    OBJSONTriangle(
                        face_index,
                        cull_face,
                        triangle.texture,
                        [vertex_0, vertex_1, vertex_2],
                    ))
            for quad in face.quads:
                vertex_0_data = face.vertices[quad.v0]
                vertex_1_data = face.vertices[quad.v1]
                vertex_2_data = face.vertices[quad.v2]
                vertex_3_data = face.vertices[quad.v3]
                vertex_0 = vertices[face_index].index(vertex_0_data)
                vertex_1 = vertices[face_index].index(vertex_1_data)
                vertex_2 = vertices[face_index].index(vertex_2_data)
                vertex_3 = vertices[face_index].index(vertex_3_data)
                cull_face = calculate_cull_face(
                    [
                        vertex_0_data.pos, vertex_1_data.pos,
                        vertex_2_data.pos, vertex_3_data.pos
                    ],
                    get_direction_from_normal(face.normal),
                )
                quads.append(
                    OBJSONQuad(
                        face_index,
                        cull_face,
                        quad.texture,
                        [vertex_0, vertex_1, vertex_2, vertex_3],
                    ))
        parts.append(OBJSONPart(part.name, part.bounds, triangles, quads))

    # Create the OBJSON object.
    objson = OBJSON(
        model_data.name,
        model_data.bounds,
        objson_faces,
        parts,
    )

    return objson


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


def get_int_direction_from_normal(normal: Vec3) -> IntDirection:
    dir = get_direction_from_normal(normal)
    if dir == Direction.UP:
        return IntDirection.UP
    elif dir == Direction.DOWN:
        return IntDirection.DOWN
    elif dir == Direction.SOUTH:
        return IntDirection.SOUTH
    elif dir == Direction.NORTH:
        return IntDirection.NORTH
    elif dir == Direction.EAST:
        return IntDirection.EAST
    elif dir == Direction.WEST:
        return IntDirection.WEST
    else:
        return IntDirection.DOWN  # Default to down


def calculate_cull_face(vertices: List[Vec3],
                        direction: Direction) -> CullFace:
    # Select the coordinates to use based on the direction
    if direction == Direction.UP or direction == Direction.DOWN:
        coords = [vertices[0][1], vertices[1][1], vertices[2][1]]
    elif direction == Direction.NORTH or direction == Direction.SOUTH:
        coords = [vertices[0][2], vertices[1][2], vertices[2][2]]
    elif direction == Direction.EAST or direction == Direction.WEST:
        coords = [vertices[0][0], vertices[1][0], vertices[2][0]]
    else:
        return CullFace.NONE

    if coords[0] != coords[1] or coords[0] != coords[2]:
        return CullFace.NONE
    if (direction == Direction.NORTH or direction == Direction.WEST
            or direction == Direction.DOWN):
        desired_coord = -0.5
    else:
        desired_coord = 0.5
    if coords[0] != desired_coord:
        return CullFace.NONE
    # The cull face is the same as the direction - we need to convert from our Direction enum to the CullFace enum
    if direction == Direction.UP:
        return CullFace.UP
    elif direction == Direction.DOWN:
        return CullFace.DOWN
    elif direction == Direction.NORTH:
        return CullFace.NORTH
    elif direction == Direction.SOUTH:
        return CullFace.SOUTH
    elif direction == Direction.EAST:
        return CullFace.EAST
    elif direction == Direction.WEST:
        return CullFace.WEST
    return CullFace.NONE


def get_model_data(scene_object: bpy.types.Object) -> ModelData:
    # Iterate over each child of the object, and create a part for each one.
    parts = []
    for child in scene_object.children:
        part_name = child.name[len(scene_object.name) + 1:]

        # Load the mesh data from the child object into a bmesh.
        mesh = child.to_mesh()
        bm = bmesh.new()
        bm.from_mesh(mesh)

        # Get the UV layer.
        uv_layer = bm.loops.layers.uv.active

        # Create the faces for the part.
        faces = []
        for f in bm.faces:
            # Determine the UVs for the vertices
            uvs = []
            for l in f.loops:
                uvs.append(l[uv_layer].uv)

            # Create the vertices
            vertices = []
            for i, v in enumerate(f.verts):
                # Round the vertex position to the nearest 1/2048th of a block
                pos = [
                    round(v.co.x * 2048) / 2048,
                    round(v.co.y * 2048) / 2048,
                    round(v.co.z * 2048) / 2048,
                ]
                # Round the UVs to the nearest 1/2048th of a block
                uv = [
                    round(uvs[i].x * 2048) / 2048,
                    round(uvs[i].y * 2048) / 2048
                ]
                vertices.append(
                    Vertex(Vec3(*pos),
                           Vec3(*[v.normal.x, v.normal.y, v.normal.z]),
                           UV(*uv)))

            texture = 0 if f.material_index == 0 else 1

            # Create the triangles for the face.
            triangles = []
            quads = []
            if len(f.verts) == 3:
                triangles.append(Triangle(0, 1, 2, texture))
            elif len(f.verts) == 4:
                quads.append(Quad(0, 1, 2, 3, texture))
            else:
                raise Exception(
                    "Face has an invalid number of vertices (must be 3 or 4)")

            # Create the face.
            faces.append(Face(vertices, triangles, quads, Vec3(*f.normal)))

        flat_vertices = [vertex for face in faces for vertex in face.vertices]

        # Calculate the bounds and create the part.
        bounds = [
            min([v.pos.x for v in flat_vertices]),
            min([v.pos.y for v in flat_vertices]),
            min([v.pos.z for v in flat_vertices]),
            max([v.pos.x for v in flat_vertices]),
            max([v.pos.y for v in flat_vertices]),
            max([v.pos.z for v in flat_vertices]),
        ]
        parts.append(Part(part_name, bounds, faces))

    # Calculate the bounds and create the model data.
    bounds = [
        min([p.bounds[0] for p in parts]),
        min([p.bounds[1] for p in parts]),
        min([p.bounds[2] for p in parts]),
        max([p.bounds[3] for p in parts]),
        max([p.bounds[4] for p in parts]),
        max([p.bounds[5] for p in parts]),
    ]
    return ModelData(scene_object.name, bounds, parts)
