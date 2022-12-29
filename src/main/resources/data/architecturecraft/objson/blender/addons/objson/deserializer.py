import json
from objson.common_types import *
from pathlib import Path


def deserialize_objson(objson_path: Path) -> ModelData:
    loaded_json = json.loads(objson_path.read_text())

    # First we need to identify what version of the format we're dealing with
    # There's three versions of the format, and they're all slightly different.

    # Format one follows the following set of rules:
    # - The root is an object
    # - The object has the following keys: "name", "bounds", "boxes", and "faces"
    # - "name" is a string
    # - "bounds" is a list of 6 floats
    # - "boxes" is a list of lists of 6 floats
    # - "faces" is a list of objects with the following keys: "texture", "vertices", "triangles", and "normal"
    #     - "texture" is an integer
    #     - "vertices" is a list of objects with the following keys: "pos", "normal", and "uv"
    #         - "pos" is a list of 3 floats
    #         - "normal" is a list of 3 floats
    #         - "uv" is a list of 2 floats
    #     - "triangles" is a list of objects with one key: "vertices"
    #         - "vertices" is a list of 3 integers
    #     - "normal" is an object with the following keys: "x", "y", and "z"
    #         - "x", "y", and "z" are all floats

    # Format two follows the following set of rules:
    # - The root is an object
    # - The object has the following keys: "name", "bounds", and "parts"
    # - "name" is a string
    # - "bounds" is a list of 6 floats
    # - "parts" is a list of objects with the following keys: "name", "bounds", and "faces"
    #     - "name" is a string
    #     - "bounds" is a list of 6 floats
    #     - "faces" is a list of objects with the following keys: "texture", "vertices", "triangles", and "normal"
    #         - "texture" is an integer
    #         - "vertices" is a list of objects with the following keys: "pos", "normal", and "uv"
    #             - "pos" is a list of 3 floats
    #             - "normal" is a list of 3 floats
    #             - "uv" is a list of 2 floats
    #         - "triangles" is a list of objects with two keys: "vertices", and "cull_face"
    #             - "vertices" is a list of 3 integers
    #         - "normal" is an array of 3 floats

    # Format three follows the following set of rules:
    # - The root is an object
    # - The object has the following keys: "name", "bounds", "faces", and "parts"
    # - "name" is a string
    # - "bounds" is a list of 6 floats
    # - "faces" is a list of objects with the following keys: "texture", "vertices", and "normal"
    #     - "texture" is an integer
    #     - "vertices" is a list of objects with the following keys: "pos", "normal", and "uv"
    #         - "pos" is a list of 3 floats
    #         - "normal" is a list of 3 floats
    #         - "uv" is a list of 2 floats
    #     - "normal" is an array of 3 floats
    # - "parts" is a list of objects with the following keys: "name", "bounds", and "triangles"
    #     - "name" is a string
    #     - "bounds" is a list of 6 floats
    #     - "triangles" is a list of objects with three keys: "face", "vertices", and "cull_face"
    #         - "face" is an integer
    #         - "vertices" is a list of 3 integers
    #         - "cull_face" is an integer

    # First we need to identify what version of the format we're dealing with, then we can delegate to the appropriate deserializer function.
    if "faces" in loaded_json and "parts" in loaded_json:
        return deserialize_objson_v3(loaded_json)
    elif "faces" not in loaded_json and "parts" in loaded_json:
        return deserialize_objson_v2(loaded_json)
    elif "faces" in loaded_json and "parts" not in loaded_json:
        return deserialize_objson_v1(loaded_json)


def deserialize_objson_v1(objson: dict) -> ModelData:
    # First we need to deserialize the bounds
    bounds = [float(x) for x in objson["bounds"]]

    # Now we need to deserialize the faces
    faces = []
    for face in objson["faces"]:
        # First we need to deserialize the vertices
        vertices = []
        for vertex in face["vertices"]:
            pos = Vec3(*[float(x) for x in vertex["pos"]])
            normal = Vec3(*[float(x) for x in vertex["normal"]])
            uv = UV(*[float(x) for x in vertex["uv"]])
            vertices.append(Vertex(pos, normal, uv))

        # Now we need to deserialize the triangles
        triangles = []
        for triangle in face["triangles"]:
            triangles.append(
                Triangle(*[int(x) for x in triangle["vertices"]], face["texture"])
            )

        # Now we need to deserialize the normal
        normal = Vec3(face["normal"]["x"], face["normal"]["y"], face["normal"]["z"])

        # Now we can create the face
        faces.append(Face(vertices, triangles, normal))

    # Now we can create the part with the default name "root" and pass it out within a model data object
    part = Part("root", bounds, faces)
    return ModelData(objson["name"], bounds, [part])


def deserialize_objson_v2(objson: dict) -> ModelData:
    # First we need to deserialize the bounds
    bounds = [float(x) for x in objson["bounds"]]

    # Now we need to deserialize the parts
    parts = []
    for part in objson["parts"]:
        # First we need to deserialize the faces
        faces = []
        for face in part["faces"]:
            # First we need to deserialize the vertices
            vertices = []
            for vertex in face["vertices"]:
                pos = Vec3(*[float(x) for x in vertex["pos"]])
                normal = Vec3(*[float(x) for x in vertex["normal"]])
                uv = UV(*[float(x) for x in vertex["uv"]])
                vertices.append(Vertex(pos, normal, uv))

            # Now we need to deserialize the triangles
            triangles = []
            for triangle in face["triangles"]:
                triangles.append(
                    Triangle(
                        *[*[int(x) for x in triangle["vertices"]], face["texture"]],
                    )
                )

            # Now we need to deserialize the normal
            normal = Vec3(*face["normal"])

            # Now we can create the face
            faces.append(Face(vertices, triangles, normal))

        # Now we can create the part
        parts.append(Part(part["name"], part["bounds"], faces))

    # Now we can create the model data object
    return ModelData(objson["name"], bounds, parts)


def deserialize_objson_v3(objson: dict) -> ModelData:
    # First we need to deserialize the bounds
    bounds = [float(x) for x in objson["bounds"]]

    # Now we need to deserialize the faces
    shared_faces = []
    for face in objson["faces"]:
        # First we need to deserialize the vertices
        vertices = []
        for vertex in face["vertices"]:
            pos = Vec3(*[float(x) for x in vertex["pos"]])
            normal = Vec3(*[float(x) for x in vertex["normal"]])
            uv = UV(*[float(x) for x in vertex["uv"]])
            vertices.append(Vertex(pos, normal, uv))

        # Now we need to deserialize the normal
        normal = Vec3(*face["normal"])

        # Now we can create the face
        shared_faces.append(Face(vertices, [], normal))

    # The format we use for representing the model data is a bit different from the actual objson format, so we need to convert it.
    # The main difference is that for compression purposes the objson format stores the faces in the root of the model data, but we store them in each part instead.
    # So we need to create a set of faces for each part instead of just one set of faces for the entire model.
    parts = []
    for part in objson["parts"]:
        triangles_by_face = {}
        for triangle in part["triangles"]:
            if triangle["face"] not in triangles_by_face:
                triangles_by_face[triangle["face"]] = []
            triangles_by_face[triangle["face"]].append(
                Triangle(
                    triangle["vertices"][0],
                    triangle["vertices"][1],
                    triangle["vertices"][2],
                    triangle["texture"],
                )
            )

        faces = []
        for face_index, triangles in triangles_by_face.items():
            face = shared_faces[face_index]
            faces.append(Face(face.vertices, triangles, face.normal))

        parts.append(Part(part["name"], part["bounds"], faces))

    # Now we can create the model data object
    return ModelData(objson["name"], bounds, parts)
