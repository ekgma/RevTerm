#!/bin/bash
start=`date +%s`

st=$(pwd)

chmod +x run.sh RevTerm.sh cpachecker/scripts/cpa.sh solvers/bclt solvers/z3 solvers/mathsat

mkdir -p jars
mkdir -p tmpFiles

declare -a linpol=("linear" "polynomial") 
declare -a parts=("part1" "part2")
for invType in "${linpol[@]}"; do
    for part in "${parts[@]}"; do
        cd $st
        cd code/$invType/$part/Main/src
        mkdir -p jar
        echo "building $invType $part"
        javac Main.java 

        cd jar

        jar cfm $invType-$part.jar ../META-INF/MANIFEST.MF ../*.class
        cp $invType-$part.jar $st/jars
        rm ../*.class
    done;
    cd $st
    cd code/prog_to_t2_$invType/src
    mkdir -p jar 
    echo "building prog_to_t2_$invType"
    javac prog_to_t2.java
    cd jar 
    jar cfm prog_to_t2_$invType.jar ../META-INF/MANIFEST.MF ../*.class
    cp prog_to_t2_$invType.jar $st/jars
    rm ../*.class
done;


cd $st
cd code/C_to_prog/src
mkdir -p jar 
echo "building C_to_prog"
javac C_to_prog.java
cd jar 
jar cfm C_to_prog.jar ../META-INF/MANIFEST.MF ../*.class
cp C_to_prog.jar $st/jars
rm ../*.class

end=`date +%s`
runtime=$((end-start))
echo "build finished in $runtime seconds"