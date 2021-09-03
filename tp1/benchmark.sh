#!/bin/bash
echo "Ejecutando Simulacion 🕐️"

# Ignore illegal access warnings
export MAVEN_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED"

mvn -f tp0_java/ compile -q || exit

echo "Benchmarking... 💪🏼"

# Calculamos vecinos
mvn -f tp0_java/ exec:java -Dexec.mainClass=ar.edu.itba.simulacion.tp0.Simulation -q || exit

echo "Vecinos calculados ✅️"

echo "Renderizando 🚀️"

# Renderizamos grafico
python3 tp0_python/benchmark.py "test/test.json" || exit

