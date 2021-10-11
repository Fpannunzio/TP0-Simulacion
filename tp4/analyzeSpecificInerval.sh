#!/bin/bash
echo "Preparando simulacion 🕐️"

CONFIG=config/mars_mission_config.json
DATA=output/analyze_specific_interval.json

mvn -f ../ install -DskipTests -q || exit

echo "Creando particulas 🕓️"

echo "Particluas creadas ✅️"

echo "Ejecutando simulacion 🕘️"

# Ejecutamos simulacion
mvn -f tp4_java exec:java -Dexec.mainClass=ar.edu.itba.simulacion.tp4.marsMission.AnalyzeSpecificInterval -Dexec.args="$CONFIG" -q || exit

echo "Simulacion finalizada ✅️"
python3 tp4_python/analyzeInterval.py $DATA || exit