import inspect
from dataclasses import dataclass
from enum import Enum
from typing import List, Optional


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
class Particle:
    id:             int
    x:              float
    y:              float
    velocityMod:    float
    velocityDir:    float
    velocityX:      float
    velocityY:      float
    mass:           float
    radius:         float

class Wall(str, Enum):
    UP = 'UP'
    DOWN = 'DOWN'
    LEFT = 'LEFT'
    RIGHT = 'RIGHT'

@dataclass
@from_dict
class Collision:
    dTime:          float
    particle1:      int
    particle2:      Optional[int]
    wall:           Optional[Wall]

@dataclass
@from_dict
class SimulationState:
    time:           float
    particles:      List[Particle]
    collision:      Collision

@dataclass
@from_dict
class Config:
    spaceWidth:     float
    iterations:     int
    particlesFile:  str
    outputFile:     str


