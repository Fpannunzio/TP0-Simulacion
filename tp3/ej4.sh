#!/bin/bash
echo "Preparando simulacion 🕐️"

mvn -f ../ install -DskipTests -q || exit

CONF="config/ej4_example.json"
DATA="output/ej4_out.json"

echo "Ejecutando simulacion 🕘️"

# Ejecutamos simulacion
mvn -f tp3_java exec:java -Dexec.mainClass=ar.edu.itba.simulacion.tp3.Ej4 -Dexec.args="$CONF" || exit

echo "Simulacion finalizada ✅️"

# echo "Renderizando 🚀️"

# # Renderizamos grafico
 python3 tp3_python/ej4.py $DATA || exit