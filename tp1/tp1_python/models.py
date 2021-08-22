import inspect
from dataclasses import dataclass

from typing import List

def from_dict(cls):
    def class_from_dict(dict):
        cls_properties = inspect.signature(cls).parameters
        return cls(**{
            k: v for k, v in dict.items() if k in cls_properties
        })

    setattr(cls, 'from_dict', class_from_dict)
    return cls

@dataclass
@from_dict
class Config:
    strategy: str
    m: int
    l: float
    actionRadius: float
    periodicOutline: bool
    particlesFile: str
    outputFile: str

@dataclass
@from_dict
class Particle:
    id: int
    x: float
    y: float
    radius: float

@dataclass
@from_dict
class Benchmark:
    config: Config
    particles: int
    timeList: List[int]
