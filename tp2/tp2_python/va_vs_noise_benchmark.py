import json
import math
import sys
from formater import multiple_formatter
from typing import Any, Dict, Optional, Union

import numpy as np
from matplotlib import pyplot as plt

from matplotlib import cm

from models import VaVsNoiseBenchmarkResult, VaVsNoiseBenchmarkSummary, VaVsNoiseBenchmarkConfig


def parse_config(data: Dict[str, Any]) -> Optional[VaVsNoiseBenchmarkConfig]:
    if 'outputFile' in data:
        return VaVsNoiseBenchmarkConfig.from_dict(data)
    else:
        return None

def parse_benchmark_summary(data: Dict[str, Any]) -> Union[VaVsNoiseBenchmarkResult, VaVsNoiseBenchmarkSummary]:
    if 'spaceWidth' in data:
        return VaVsNoiseBenchmarkResult.from_dict(data)
    else:
        return VaVsNoiseBenchmarkSummary.from_dict(data)

def main(config_path):
    with open(config_path, 'r') as config_fd:
        config: VaVsNoiseBenchmarkConfig = json.load(config_fd, object_hook=parse_config)

    with open(config.outputFile, 'r') as particles_fd:
        benchmark_summary: VaVsNoiseBenchmarkSummary = json.load(particles_fd, object_hook=parse_benchmark_summary)

    # Graficamos el primero para testear
    first_benchmark_result: VaVsNoiseBenchmarkResult = benchmark_summary.variableDensityBenchmarks[0]

    fig1 = plt.figure(100, figsize=(16, 8))
    ax1 = fig1.add_subplot(1, 1, 1)

    plot_results(benchmark_summary.variableDensityBenchmarks, benchmark_summary.noiseStep, ax1)

    fig2 = plt.figure(200, figsize=(16, 8))
    ax2 = fig2.add_subplot(1, 1, 1)

    plot_results(benchmark_summary.constantDensityBenchmarks, benchmark_summary.noiseStep, ax2)
    plt.show()
    

def plot_results(benchmarks, noiseStep,axis):
    
    for i in range(len(benchmarks)):
        
        benchmark_result = benchmarks[i]

        axis.errorbar(
            np.arange(0, 2 * math.pi, noiseStep)
            , benchmark_result.vaMean
            , yerr=benchmark_result.vaStd
            , color=cm.get_cmap('tab10')(i)
            , capsize=2
            , label=f'N= {int(benchmark_result.particleCount)}')

    axis.set_xlabel(r'$\eta$: Noise', size=20)
    axis.set_ylabel(r'$v_a$: Average Normalized Velocity', size=20)

    axis.grid()
    axis.legend()
    axis.xaxis.set_major_locator(plt.MultipleLocator(np.pi / 4))
    axis.xaxis.set_major_formatter(plt.FuncFormatter(multiple_formatter(denominator=4)))



if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass

