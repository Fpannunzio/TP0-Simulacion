import json
import math
import sys
from formater import multiple_formatter
from typing import Any, Dict, Optional, Union

import numpy as np
from matplotlib import pyplot as plt

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

    fig = plt.figure(figsize=(16, 8))
    ax = fig.add_subplot(1, 1, 1)

    ax.errorbar(np.arange(0, 2 * math.pi, benchmark_summary.noiseStep), first_benchmark_result.vaMean, yerr=first_benchmark_result.vaStd, color='red', capsize=2)

    ax.set_xlabel(r'$\eta$')
    ax.set_ylabel(r'$v_a$')

    ax.grid()
    ax.xaxis.set_major_locator(plt.MultipleLocator(np.pi / 4))
    ax.xaxis.set_minor_locator(plt.MultipleLocator(np.pi / 12))
    ax.xaxis.set_major_formatter(plt.FuncFormatter(multiple_formatter(denominator=4)))

    plt.show()


if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass

