import inspect
from dataclasses import dataclass

from typing import List

def class_from_dict(cls, dict):
    cls_properties = inspect.signature(cls).parameters
    return cls(**{
        k: v for k, v in dict.items() if k in cls_properties
    })

@dataclass
class Config:
    strategy: str
    M: int
    L: float
    actionRadius: float
    periodicOutline: bool
    particlesFile: str
    outputFile: str

    @classmethod
    def from_dict(cls, dict):
        return class_from_dict(cls, dict)


@dataclass
class Particle:
    id: int
    x: float
    y: float
    radius: float

    @classmethod
    def from_dict(cls, dict):
        return class_from_dict(cls, dict)

@dataclass
class Benchmark:
    config: Config
    particles: int
    timeList: List[int]

    @classmethod
    def from_dict(cls, dict):
        return class_from_dict(cls, dict)
