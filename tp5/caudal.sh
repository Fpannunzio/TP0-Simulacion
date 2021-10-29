#!/bin/bash
echo "Preparando simulacion 🕐️"

CONFIG=config/caudal_config.json
DATA="output/caudal.json"

mvn -f ../ install -DskipTests -q || exit

echo "Creando particulas 🕓️"

echo "Particluas creadas ✅️"

echo "Ejecutando simulacion 🕘️"

# Ejecutamos simulacion
mvn -f tp5_java exec:java -Dexec.mainClass=ar.edu.itba.simulacion.tp5.Caudal -Dexec.args="$CONFIG" -q || exit

# Renderizamos grafico
python3 tp5_python/caudal.py $DATA || exit