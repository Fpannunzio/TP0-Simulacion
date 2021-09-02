#!/bin/bash
echo "Preparando simulacion 🕐️"

# Ignore illegal access warnings
export MAVEN_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED"

CONF1=config/particle_gen_conf_example.json
CONF2=config/off_lattice_benchmark.json

mvn -f ../ clean install -DskipTests -q || exit

echo "Ejecutando simulacion 🕘️"

# Calculamos vecinos
mvn -f tp2_java exec:java -Dexec.mainClass=ar.edu.itba.simulacion.tp2.VaVsNoiseBenchmark -Dexec.args="$CONF2"|| exit

echo "Simulacion finalizada ✅️"

echo "Renderizando 🚀️"

# Renderizamos grafico
python3 tp2_python/va_vs_noise_benchmark.py "$CONF2" || exit

