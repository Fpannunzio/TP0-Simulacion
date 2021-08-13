from dataclasses import dataclass

from typing import List

@dataclass
class Config:
    strategy: str
    M: int
    L: float
    actionRadius: float
    periodicOutline: bool
    particlesFile: str
    outputFile: str


@dataclass
class Particle:
    id: int
    x: float
    y: float
    radius: float

@dataclass
class Benchmark:
    config: Config
    particles: int
    timeList: List[int]
