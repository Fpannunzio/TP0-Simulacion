#!/bin/bash
echo "Preparando simulacion 🕐️"

mvn -f ../ clean install -DskipTests -q || exit

CONF="config/ej1_example.json"
DATA="output/ej1_out.json"

echo "Ejecutando simulacion 🕘️"

# Ejecutamos simulacion
mvn -f tp3_java exec:java -Dexec.mainClass=ar.edu.itba.simulacion.tp3.Ej1 -Dexec.args="$CONF" -q || exit

echo "Simulacion finalizada ✅️"

echo "Renderizando 🚀️"

# Renderizamos grafico
python3 tp3_python/ej1.py $DATA || exit