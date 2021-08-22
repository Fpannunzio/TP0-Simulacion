import inspect
from dataclasses import dataclass

def class_from_dict(cls, dict):
    cls_properties = inspect.signature(cls).parameters
    return cls(**{
        k: v for k, v in dict.items() if k in cls_properties
    })

@dataclass
class Particle:
    id:             int
    x:              float
    y:              float
    velocityMod:    float
    velocityDir:    float
    radius:         float

    @classmethod
    def from_dict(cls, dict):
        return class_from_dict(cls, dict)

@dataclass
class Config:
    spaceWidth:     float
    actionRadius:   float
    periodicBorder: bool
    outputFile:     str

    @classmethod
    def from_dict(cls, dict):
        return class_from_dict(cls, dict)
