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
class VelocityAnalysisInfo:
    initialVelocity:    float
    velocityStep:       float
    tripDuration:       List[int]
    returnTrip:         bool

@dataclass
@from_dict
class VelocityPerIterationInfo:
    secondsStep:    int
    velocities:     List[float]
    returnTrip:     bool


