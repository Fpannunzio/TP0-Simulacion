#!/bin/bash
echo "Preparando simulacion 🕐️"

CONFIG=config/ej2_config.json
DATA="output/ej2.json"

mvn -f ../ install -DskipTests -q || exit

echo "Creando particulas 🕓️"

echo "Particluas creadas ✅️"

echo "Ejecutando simulacion 🕘️"

# Ejecutamos simulacion
mvn -f tp5_java exec:java -Dexec.mainClass=ar.edu.itba.simulacion.tp5.Ej2 -Dexec.args="$CONFIG" -q || exit

# Renderizamos grafico
python3 tp5_python/ej2.py $DATA || exit