#!/bin/bash
dir=$1 #directory of the benchmarks

mkdir -p output/VeryMax

for file in $dir/*.c; do
    fileName=$(basename $file)
    echo "running VeryMax on $fileName"
    timeout 60 ./VeryMax.sh $file > output/VeryMax/$fileName.out
done