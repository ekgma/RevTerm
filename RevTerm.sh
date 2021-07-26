#!/bin/bash
# ./RevTerm [input file] [-linear/polynomial] [part] [solver] [con] [dis] {degree}

file=$1
invType=$2
part=$3
solver=$4
con=$5
dis=$6

fileAddress=$(dirname $file)
fileName=$(basename $file)

mkdir -p prog
mkdir -p T2
mkdir -p logs
mkdir -p tmpFiles
#convert from C to prog
java -jar jars/C_to_prog.jar $file prog/$fileName.prog
result=$?
if [[ $result == 0 ]]; then 
    echo "successfully converted from C to prog"
else 
    exit $result
fi 

#convert from prog to T2
java -jar jars/prog_to_t2_linear.jar prog/$fileName.prog T2/$fileName.t2 2> logs/prog_to_t2_polynomial.log
result=$? 
if [[ $result != 0 ]]; then 
    java -jar jars/prog_to_t2_polynomial.jar prog/$fileName.prog T2/$fileName.t2 2> logs/prog_to_t2_polynomial.log
    result=$?
    if [[ $result == 0 ]]; then
        echo "successfully converted from prog to T2 using polynomial converter"
    else 
        echo "program could not be converted to T2."
        echo "see prog_to_t2_polynomial.log and logs/prog_to_t2_polynomial.log for details"
        exit 1
    fi
else 
    echo "successfully converted from prog to T2 using linear converter"
fi
    
#run RevTerm
if [ "$invType" == "-polynomial" ] 
then
    degree=$7
    java -jar jars/polynomial-$part.jar $con $dis $degree $degree $solver T2/$fileName.t2 tmpFiles solvers/ cpachecker/
elif [ "$invType" == "-linear" ]
then 
    java -jar jars/linear-$part.jar $con $dis $solver T2/$fileName.t2 tmpFiles solvers/ cpachecker/
fi

