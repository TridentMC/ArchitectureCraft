from typing import List
from dataclasses import dataclass
from enum import Enum, IntEnum

from objson.common_types import Vec3

class CullFace(IntEnum):
    NONE = -1
    DOWN = 0
    UP = 1
    NORTH = 2
    SOUTH = 3
    WEST = 4
    EAST = 5


class Direction(Enum):
    NORTH = Vec3(0, 0, -1)
    SOUTH = Vec3(0, 0, 1)
    WEST = Vec3(-1, 0, 0)
    EAST = Vec3(1, 0, 0)
    UP = Vec3(0, 1, 0)
    DOWN = Vec3(0, -1, 0)


@dataclass
class OBJSONTriangle:
    face: int
    cull_face: CullFace
    texture: int
    vertices: List[int]


@dataclass
class OBJSONVertex:
    pos: List[float]
    normal: List[float]
    uv: List[float]


@dataclass
class OBJSONFace:
    normal: List[float]
    vertices: List[OBJSONVertex]


@dataclass
class OBJSONPart:
    name: str
    bounds: List[float]
    triangles: List[OBJSONTriangle]


@dataclass
class OBJSON:
    name: str
    bounds: List[float]
    faces: List[OBJSONFace]
    parts: List[OBJSONPart]