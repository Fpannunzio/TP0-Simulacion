#!/bin/bash
echo "Preparando simulacion 🕐️"

mvn -f ../ install -DskipTests -q || exit

CONF="config/ej4_example.json"
CONFB="config/ej4b_example.json"
DATA="output/ej4_out.json"
DATAB="output/ej4b_out.json"

echo "Ejecutando simulacion 🕘️"

# Ejecutamos simulacion
mvn -f tp3_java exec:java -Dexec.mainClass=ar.edu.itba.simulacion.tp3.Ej4 -Dexec.args="$CONF" -q || exit

echo "Ejecutando 2da simulacion 🕘️"
mvn -f tp3_java exec:java -Dexec.mainClass=ar.edu.itba.simulacion.tp3.Ej4b -Dexec.args="$CONFB" -q || exit

echo "Simulacion finalizada ✅️"

# echo "Renderizando 🚀️"

echo "Renderizamos grafico"
python3 tp3_python/ej4.py $DATA || exit
echo "Renderizamos 2do grafico"
python3 tp3_python/ej4b.py $DATAB || exit