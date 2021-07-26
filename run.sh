#!/bin/bash
dir=$1 #directory of the benchmarks
declare -a solvers=("bclt" "mathsat" "z3")
declare -a cons=("1" "2" "3" "4" "5" )
declare -a diss=("1" "2" "3" "4" "5")
declare -a parts=("part1" "part2")

mkdir -p output/RevTerm
mkdir -p output/VeryMax
mkdir -p output/Ultimate

for file in $dir/*.c; do
    fileName=$(basename $file)
    echo "running RevTerm on $fileName"
    echo "" > output/RevTerm/$fileName.out
    for part in "${parts[@]}"; do
        for solver in "${solvers[@]}"; do 
            for dis in "${diss[@]}"; do
                for con in "${cons[@]}"; do 
                    echo "RevTerm: linear $part $solver $con $dis" >> output/RevTerm/$fileName.out
                    echo "linear part=$part solver=$solver con=$con dis=$dis"
                    timeout 60 ./RevTerm.sh $file -linear $part $solver $con $dis >> output/RevTerm/$fileName.out

                    echo "Revterm: polynomial $part $solver $con $dis" >> output/RevTerm/$fileName.out
                    echo "polynomial part=$part solver=$solver con=$con dis=$dis degree=2"
                    timeout 60 ./RevTerm.sh $file -polynomial $part $solver $con $dis 2 >> output/RevTerm/$fileName.out
                done
            done
        done 
    done
    echo "running Ultimate on $fileName"
    timeout 60 ./Ultimate.sh $file > output/Ultimate/$fileName.out

    echo "running VeryMax on $fileName"
    timout 60 ./VeryMax.sh $file > output/VeryMax/$fileName.out
done