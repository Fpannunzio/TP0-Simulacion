from models import Benchmark
from typing import List

import numpy as np
import matplotlib.pyplot as plt


class PlotBenchmark:

    def __init__(self, benchmarks: List[Benchmark]):
        self.benchmarks: List[Benchmark] = benchmarks

    def plot(self):
        valuePerM: np.ndarray = np.empty((10, 2))
        bruteForceTuple: np.ndarray = np.empty((1, 2))
        
        for benchmark in self.benchmarks:
            if (benchmark.config.strategy == "BRUTE_FORCE"):
                bruteForceTuple[0][0] = np.mean(np.asarray(benchmark.timeList)/1e6)
                bruteForceTuple[0][1] = np.std(np.asarray(benchmark.timeList)/1e6)

            else:
                valuePerM[benchmark.config.M - 1][0] = np.mean(np.asarray(benchmark.timeList)/1e6)
                valuePerM[benchmark.config.M - 1][1] = np.std(np.asarray(benchmark.timeList)/1e6)
        
        print(valuePerM)
        print(bruteForceTuple)
        print(len(range(1,11)), len(valuePerM[:,0]))
        plt.errorbar(2.5, bruteForceTuple[0,0], yerr=bruteForceTuple[0,1], color='red')
        plt.plot(range(2,11), np.repeat(bruteForceTuple[0][0], 9), color='red')
        plt.errorbar(range(2,11), valuePerM[1:,0], yerr=valuePerM[1:,1])
        plt.show()
            