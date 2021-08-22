from matplotlib import legend
from models import Benchmark
from typing import List

import numpy as np
import matplotlib.pyplot as plt


class PlotBenchmark:

    def __init__(self, benchmarks: List[Benchmark]):
        self.benchmarks: List[Benchmark] = benchmarks

    def plot(self):
        
        valuePerM: np.ndarray = np.empty((10, 2))
        bruteForceMTuple: np.ndarray = np.empty((1, 2))
        
        valuePerN: np.ndarray = np.empty((10, 2))
        bruteForcePerN: np.ndarray = np.empty((10, 2))
        
        print(len(self.benchmarks))

        for benchmark in self.benchmarks:
            if benchmark.particles == 501:
                if (benchmark.config.strategy == "BRUTE_FORCE"):
                    bruteForceMTuple[0][0] = np.mean(np.asarray(benchmark.timeList)[25:]/1e6)
                    bruteForceMTuple[0][1] = np.std(np.asarray(benchmark.timeList)[25:]/1e6)

                else:
                    valuePerM[benchmark.config.m - 1][0] = np.mean(np.asarray(benchmark.timeList)[25:] / 1e6)
                    valuePerM[benchmark.config.m - 1][1] = np.std(np.asarray(benchmark.timeList)[25:] / 1e6)
        
            else:
                if (benchmark.config.strategy == "BRUTE_FORCE"):
                    bruteForcePerN[benchmark.particles//100-1][0] = np.mean(np.asarray(benchmark.timeList)[25:]/1e6)
                    bruteForcePerN[benchmark.particles//100-1][1] = np.std(np.asarray(benchmark.timeList)[25:]/1e6)

                else:
                    valuePerN[benchmark.particles//100-1][0] = np.mean(np.asarray(benchmark.timeList)[25:]/1e6)
                    valuePerN[benchmark.particles//100-1][1] = np.std(np.asarray(benchmark.timeList)[25:]/1e6)


        print("Values: ", valuePerM)
        print("Bruteforce: ", bruteForceMTuple)
        
        plt.errorbar(2.5, bruteForceMTuple[0,0], yerr=bruteForceMTuple[0,1], color='red', capthick=2)
        bfM, = plt.plot(range(1,11), np.repeat(bruteForceMTuple[0][0], 10), color='red')
        vpM = plt.errorbar(range(1,11), valuePerM[:,0], yerr=valuePerM[:,1], capthick=2)
        
        plt.title('CIM(M) vs Bruteforce - 500 particles', fontsize=20)
        plt.xlabel('M')
        plt.ylabel('Execution time [ms]')
        
        plt.legend([bfM, vpM[0]], ['Bruteforce', 'CIM'])
        plt.grid()
        plt.show()
            
        bpN = plt.errorbar(range(100,1001,100), bruteForcePerN[:,0], yerr=bruteForcePerN[:,1], color='red', capthick=2)
        vpN = plt.errorbar(range(100,1001,100), valuePerN[:,0], yerr=valuePerN[:,1], )
        plt.legend([bpN[0], vpN[0]], ['Bruteforce', 'CIM'])
        
        plt.title('CIM vs Bruteforce - M=10', fontsize=20)
        plt.xlabel('Particle count')
        plt.ylabel('Execution time [ms]')
        
        plt.grid()
        plt.show()
