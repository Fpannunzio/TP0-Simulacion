import json
import math
import sys
from formater import multiple_formatter
from typing import Any, Dict, Optional

import numpy as np
from matplotlib import pyplot as plt

from models import Config, VaVsNoiseBenchmarkResult


def parse_config(data: Dict[str, Any]) -> Optional[Config]:
    if 'outputFile' in data:
        return Config.from_dict(data)
    else:
        return None

def main(config_path):
    with open(config_path, 'r') as config_fd:
        config: Config = json.load(config_fd, object_hook=parse_config)

    with open(config.outputFile, 'r') as particles_fd:
        benchmark_result: VaVsNoiseBenchmarkResult = json.load(particles_fd, object_hook=lambda d: VaVsNoiseBenchmarkResult.from_dict(d))

    fig = plt.figure(figsize=(16,8))
    ax = fig.add_subplot(1, 1, 1)

    ax.errorbar(np.arange(0, 2 * math.pi, benchmark_result.noiseStep), benchmark_result.vaMean, yerr=benchmark_result.vaStd, color='red', capsize=2)

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

