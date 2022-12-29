from typing import List, NamedTuple
from numpy import double


class Vec3(NamedTuple):
    x: double
    y: double
    z: double


class UV(NamedTuple):
    u: double
    v: double


class Vertex(NamedTuple):
    pos: Vec3
    normal: Vec3
    uv: UV


class Triangle(NamedTuple):
    v0: int
    v1: int
    v2: int
    texture: int


class Face(NamedTuple):
    vertices: List[Vertex]
    triangles: List[Triangle]
    normal: Vec3


class Part(NamedTuple):
    name: str
    bounds: List[double]
    faces: List[Face]


class ModelData(NamedTuple):
    name: str
    bounds: List[double]
    parts: List[Part]
