#!/bin/bash

# Estimates D_KL(P || Q) (K-L Divergence of P over Q)

# Arguments:
# $1 = dir to work in
# $2 = CM "P"
# $3 = CM "Q"
# $4 = Number of samples to use

# Setup our working environment
mkdir -p $1/CMs
mkdir -p $1/emit
mkdir -p $1/align
cp $2 $1/CMs/
cp $3 $1/CMs/
P=$(basename $2)
Q=$(basename $3)
numSamples=$4
workingDir=$1

# Emit samples
Pemit="${P}.emit.fasta"
./utilities/cmemit -N ${numSamples} -o ${workingDir}/emit/${Pemit} ${workingDir}/CMs/${P}

# Align samples to both CMs
PemitaligntoP="${Pemit}.alignto.${P}.sto"
PemitaligntoQ="${Pemit}.alignto.${Q}.sto"
PemitaligntoPdata="${Pemit}.alignto.${P}.sto.data"
PemitaligntoQdata="${Pemit}.alignto.${Q}.sto.data"
./utilities/cmalign -o ${workingDir}/align/${PemitaligntoP} ${workingDir}/CMs/${P} ${workingDir}/emit/${Pemit} > ${workingDir}/align/${PemitaligntoPdata}
./utilities/cmalign -o ${workingDir}/align/${PemitaligntoQ} ${workingDir}/CMs/${Q} ${workingDir}/emit/${Pemit} > ${workingDir}/align/${PemitaligntoQdata}

# Read the bitscores of all alignments and calculate score
scoresum=0
# read a file line by line:
while IFS= read -r -u3 line; do
    if [[ ${line} != \#* ]]; then
        # Ignore the comment lines in data output
        Plinearr=(${line})
        Qlinearr=($(grep ${Plinearr[1]} ${workingDir}/align/${PemitaligntoQdata}))
        # Bit score is arr[6] in the whitespace-sv
        #echo "P ${Plinearr[6]} Q ${Qlinearr[6]}"
        PQ=$(echo "(${Plinearr[6]})-(${Qlinearr[6]})" | bc)
        #echo "Elem score: ${PQ}"
        scoresum=$(echo "${scoresum}+${PQ}" | bc)
    fi
done 3< "${workingDir}/align/${PemitaligntoPdata}"

# Average scores (using sum and sample count)
estimatedKLDivergence=$(echo "${scoresum}/${numSamples}" | bc -l)
#echo "sum ${scoresum} num ${numSamples}"
echo ${estimatedKLDivergence}


