#!/bin/bash
dir=$1 #directory of the benchmarks

mkdir -p output/Ultimate

for file in $dir/*.c; do
    fileName=$(basename $file)
    echo "running Ultimate on $fileName"
    timeout 60 ./Ultimate.sh $file > output/Ultimate/$fileName.out
done