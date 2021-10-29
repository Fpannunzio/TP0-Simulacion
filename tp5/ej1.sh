#!/bin/bash
echo "Preparando simulacion 🕐️"

CONFIG=config/multi_pedestrian_dynamics_config.json
DATA="output/ej1.json"

mvn -f ../ install -DskipTests -q || exit

echo "Creando particulas 🕓️"

echo "Particluas creadas ✅️"

echo "Ejecutando simulacion 🕘️"

# Ejecutamos simulacion
mvn -f tp5_java exec:java -Dexec.mainClass=ar.edu.itba.simulacion.tp5.Ej1 -Dexec.args="$CONFIG" -q || exit

# Renderizamos grafico
python3 tp5_python/ej1.py $DATA || exit