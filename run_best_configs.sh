index=1
mkdir -p output/RevTerm
mkdir -p output/VeryMax
mkdir -p output/Ultimate

while read p; do
    b=($p)
    fileName=${b[0]}
    part=${b[1]}
    if [[ $part != "-" ]]; then
        con=${b[2]}
        dis=${b[3]}
        solver=${b[4]}
        invType=${b[5]}
        echo "$index. running RevTerm on $fileName $invType $part $solver $con $dis"
        echo "RevTerm: $invType $part $solver $con $dis" > output/RevTerm/$fileName.out
        if [[ $invType == "linear" ]]; then
            ./RevTerm.sh Benchmarks/C-Integer/$fileName -$invType $part $solver $con $dis >> output/RevTerm/$fileName.out
        else 
            ./RevTerm.sh Benchmarks/C-Integer/$fileName -$invType $part $solver $con $dis 2 >> output/RevTerm/$fileName.out
        fi
    fi
    echo "running Ultimate on $fileName"
    timeout 60 ./Ultimate.sh Benchmarks/C-Integer/$fileName > output/Ultimate/$fileName.out

    echo "running VeryMax on $fileName"
    timeout 60 ./VeryMax.sh Benchmarks/C-Integer/$fileName > output/VeryMax/$fileName.out

    index=$((index+1))
done <best_configs.csv


