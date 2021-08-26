import json
import math
import sys
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

    plt.errorbar(np.arange(0, 2 * math.pi, benchmark_result.noiseStep), benchmark_result.vaMean, yerr=benchmark_result.vaStd, color='red', capthick=2)

    plt.title('CIM(M) vs Bruteforce - 500 particles', fontsize=20)
    plt.xlabel('M')
    plt.ylabel('Execution time [ms]')

    plt.grid()
    plt.show()


if __name__ == '__main__':
    if len(sys.argv) < 2:
        raise ValueError('Config path must be given by argument')
    try:
        main(sys.argv[1])
    except KeyboardInterrupt:
        pass

