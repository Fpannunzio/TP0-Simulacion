from typing import List
from models import Benchmark, Config 
import json
import sys
from plotBenchmark import PlotBenchmark

def benchmark(config_path: str):
    with open(config_path) as config_fd:
        benchmarks: List[Benchmark] = json.load(config_fd, object_hook=deserializeBenchmark)
    
    PlotBenchmark(benchmarks).plot()

def deserializeBenchmark (d): 
    if 'strategy' in d:
        return Config.from_dict(d)
    else:
        return Benchmark.from_dict(d)

if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        benchmark(sys.argv[1])
    except KeyboardInterrupt:
        pass
