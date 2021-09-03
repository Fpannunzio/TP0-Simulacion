#!/bin/bash
echo "Preparando simulacion 🕐️"

# Ignore illegal access warnings
export MAVEN_OPTS="--add-opens java.base/java.lang=ALL-UNNAMED"

mvn -f ../ clean install -DskipTests -q || exit

echo "Ejecutando simulacion 🕘️"

# Calculamos vecinos
mvn -f tp2_java exec:java -Dexec.mainClass=ar.edu.itba.simulacion.tp2.VaVsStepBenchmark -Dexec.args="$1" -q|| exit

echo "Simulacion finalizada ✅️"

echo "Renderizando 🚀️"

# Renderizamos grafico
python3 tp2_python/va.py "$2" || exit

