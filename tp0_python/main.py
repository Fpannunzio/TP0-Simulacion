import json
import sys
from typing import List, Dict

from models import Config, Particle, Benchmark
from plot import Plotter
from plotBenchmark import PlotBenchmark

def excercise(config_path: str):
    with open(config_path) as config_fd:
        config: Config = json.load(config_fd, object_hook=lambda d: Config(**d))

    with open(config.particlesFile, "r") as particles_fd, open(config.outputFile) as neighbours_fd:
        particles: List[Particle] = json.load(particles_fd, object_hook=lambda d: Particle(**d))
        neighbours: Dict[str, List[int]] = json.load(neighbours_fd)

    Plotter(config.strategy, particles, config.actionRadius, neighbours, config.M, config.L, config.periodicOutline).plot()

def benchmark(config_path: str):
    with open(config_path) as config_fd:
        benchmarks: List[Benchmark] = json.load(config_fd, object_hook=deserializeBenchmark)
    
    PlotBenchmark(benchmarks).plot()
    
def deserializeBenchmark (d): 
    if 'strategy' in d:
        return Config(**d)
    else:
        return Benchmark(**d)
    

if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        benchmark(sys.argv[1])
    except KeyboardInterrupt:
        pass
