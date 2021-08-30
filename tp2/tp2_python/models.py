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
class Particle:
    id:             int
    x:              float
    y:              float
    velocityMod:    float
    velocityDir:    float
    radius:         float

@dataclass
@from_dict
class Config:
    spaceWidth:     float
    actionRadius:   float
    noise:          float
    periodicBorder: bool
    outputFile:     str

@dataclass
@from_dict
class VaVsNoiseBenchmarkConfig:
    spaceWidth:     float
    actionRadius:   float
    noiseStep:      float
    periodicBorder: bool
    velocity:       float
    particleCounts: List[int]
    outputFile:     str

@dataclass
@from_dict
class VaVsNoiseBenchmarkResult:
    spaceWidth:     float
    particleCount:  int
    vaMean:         List[float]
    vaStd:          List[float]

@dataclass
@from_dict
class VaVsNoiseBenchmarkSummary:
    noiseStep:                  float
    # variableDensityBenchmarks:  List[VaVsNoiseBenchmarkResult]
    constantDensityBenchmarks:  List[VaVsNoiseBenchmarkResult]



