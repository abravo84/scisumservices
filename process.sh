#!/bin/bash

POSITIONAL=()
while [[ $# -gt 0 ]]
do
key="$1"

case $key in
    --input)
    INPUT="$2"
    shift # past argument
    shift # past value
    ;;
    --output)
    OUTPUT="$2"
    shift # past argument
    shift # past value
    ;;
    *)    # unknown option
    POSITIONAL+=("$1") # save it in an array for later
    shift # past argument
    ;;
esac
done
set -- "${POSITIONAL[@]}" # restore positional parameters

echo INPUT  = "${INPUT}"
echo OUTPUT  = "${OUTPUT}"

cd /scisumservices
java -Xmx5g -jar target/scisumservices-jar-with-dependencies.jar ${INPUT} ${OUTPUT}

