Getting Started Guide

0. VM credentials
Both username and password for the VM is "revterm" (without quotes).

1. Abstract

RevTerm is a static analysis tool for proving non-termination of integer C programs (possibly with non-determinism). Given an input program, it either outputs that non-termination has been proved, or that it could not prove non-termination.

RevTerm takes an integer C program as an input (syntax of input programs is described at http://termination-portal.org/wiki/C_Integer_Programs). The program is then converted to an equivalent T2 program (another input format which is common in termination analysis, for examples see https://github.com/mmjb/T2). RevTerm then tries to prove whether this program is non-terminating.

2. Dependencies 

RevTerm is written in Java. To run the tool, it is recommended to have Java 8 or a newer version installed.

In order to run RevTerm, one needs to install Barcelogic 1.2, which is free for academic purposes upon contacting the owners (see https://www.cs.upc.edu/~oliveras/bclt-main.html for details). The binary of Barcelogic 1.2 should be placed into the "solvers" directory and called "bclt". To run Barcelogic 1.2 one also needs to have libgmp installed.
To run configurations of RevTerm that do not invoke Barcelogic 1.2 (for details on configurations, see below) one does not need to have Barcelogic 1.2 installed.

Binaries for all other dependencies of the tool along with their licenses (see Directories section below) are provided with the artifact thus manual pre-installation is not needed. In particular, we provide binaries for the following tools:
1. CPA Checker https://github.com/sosy-lab/cpachecker#license-and-copyright
2. MathSAT5 (available only for research and evaluation purposes) https://mathsat.fbk.eu/download.html
3. Z3 https://github.com/Z3Prover/z3/blob/master/LICENSE.txt

This repository also contains the binaries of VeryMax and Ultimate Automizer (both obtained from https://www.starexec.org/starexec/secure/explore/spaces.jsp?id=334158), two tools that support non-termination analysis which we experimentally compared RevTerm with. Ultimate uses several Java libraries which work only with Java 8. Ultimate also depends on `at-spi2-core`.

3. Directories

(i)	Benchmarks: A folder containing two benchmark sets. "C-Integer" contains all 335 benchmarks considered in the paper.
(ii) 	code: A folder containing all the source codes of RevTerm.
(iii)	cpachecker and solvers: directories containing binaries of CPAchecker and two SMT-solvers: MathSAT5 and Z3.
(iv)	other_tools: contains binaries of Ultimate and VeryMax.
(v)	spreadsheets: A folder containing several spreadsheets on obtained results. (For the formula cells to open properly, open these files using spreadsheet viewers such as libreoffice)

4. Build

In order to build RevTerm simply run './build.sh'. This will create an additional directory named 'jars', containing 7 jar files. Four of the jar files represent the tool configurations, and the rest are tools for converting input files from C to T2.

5. Tool Configurations

The underlying algorithm of RevTerm consists of two independent checks that are used to detect non-termination bugs (Check 1 and Check 2 in the paper, see page 7-8). 

The algorithm is based on invariant generation and invariants at each program location are represented via DNF predicates over program variables, with each literal being a polynomial inequality over program variables. Thus, the algorithm is parametrized by the total number of disjunctions and the number of conjunctions in each clause, as well as the largest polynomial degree of literals in the predicate.

Our tool supports three solvers for SMT-solving: bclt, mathsat, and z3.

Finally, each configuration can be run either with polynomial settings, or in the special case when degree=1 we may also use special linear settings. Note that polynomial settings with degree=1 is equivalent to linear settings. However, in linear settings, RevTerm uses Farkas Lemma for constraint solving, which is more optimal than Putinar's positivstellensatz used in polynomial settings.

Thus, a single tool configuration consists of (i) the choice of Check, (ii) number of conjunctions, (iii) number of disjunctions, (iv) polynomial degree, (v) choice of a solver and (vi) the choice of polynomial/linear settings.

6. Tool Usage

To run the tool you can run the following command.
./RevTerm [input file] [-linear/-polynomial] [part] [solver] [conjunctions] [disjunctions] {degree}

(i)   [input file] is the input C program. 
(ii)  [-linear/-polynomial] enter either -linear if RevTerm should use linear settings and -polynomial if it should use polynomial settings.
(iii) [part] should be either 'part1' or 'part2' depending on what Check of the algorithm you want to run. 
(iv)  [solver] is supposed to be one of 'bclt', 'mathsat', or 'z3' depending on which SMT-solver you want the tool to use.
(v)   [conjunctions], [disjunctions] are the required number of conjunctions and disjunctions in the invariants.
(vi)  {degree} is the required polynomial degree, if -polynomial was used at (ii).

Note that when RevTerm is run in linear settings, the input program cannot contain non-linear expressions. If so, RevTerm will throw an exception.

An example command for running RevTerm looks like this:
./RevTerm.sh example.c -linear part1 bclt 2 1 

Once executed, the program first tries to convert the input C program to an equivalent T2 file, stored in (fresh) "T2" directory. Then, based on what settings is given to RevTerm, it will select the right jar file from the "jars" folder to use. 

If the input program is non-terminating and the tool is able to prove so, 'Non-Terminating' will be printed along with a line showing how much time (in milliseconds) the main algorithm used for proving non-termination. If the SMT solver or CPAchecker fail in their tasks, 'Could Not Prove Non-Termination' will be printed instead of 'Non-Terminating'.

Generated files during the process (such as the SMT file for feeding the SMT solvers) will be stored at "tmpFiles" directory for logging and debugging purposes.



Step-by-Step Instructions to Reproduce Experimental Results

To run RevTerm, VeryMax and Ultimate on a set of benchmarks at [benchmarks dir], one can run
./run.sh [benchmarks dir]

and to run only one of the mentioned tools, 
./run_revterm.sh [benchmarks dir]
./run_verymax [benchmarks dir]
./run_ultimate [benchmarks dir]

The complete set of benchmarks that we consider in our paper is available in "Benchmarks/C-Integer". The "run.sh" script runs all configurations with 1<=conjunctions<=5, 1<=disjunctions<=5, 1<=degree<=2, as was done in the experimental results that we reported in the paper. To change the template sizes, one can simply modify "run.sh". The timeout for each experiment is 60s.

However, running all configurations of RevTerm on all benchmarks will take days. The reason is that, if a tool configuration cannot prove non-termination of a given input program, the tool might diverge and thus run for the whole 60s. As we experiment with 300 configurations for each input program, programs for which non-termination cannot be proved significantly affect the total runtime.

Therefore, we gathered the list of the 107 benchmarks for which RevTerm can prove non-termination along with their fastest working configuration. The list is stored in "best_configs.csv" file. We also gathered all 112 non-terminating benchmarks to evaluate VeryMax and Ultimate. In order to run RevTerm on the benchmarks with its best configuration, as well as VeryMax and Ultimate on all 112 non-terminating benchmarks, one can run 
./run_best_configs.sh

This took less than 60 minutes to finish on our device (Platform 1 in our paper, specifications Debian, 128 GB RAM, Intel(R) Xeon(R) CPU E5-1650 v3 @ 3.50GHz).

Both "./run.sh" and "./run_best_configs.sh" will create an "output" directory for storing the output of the tool which proves non-termination for each benchmark.

Running the script "./run_best_configs.sh" can be used to reproduce the experimental results in Table 1 in the paper:

1. Table 1 - To show that our tool proves 107 non-terminations as well as to obtain best configuration times, number of proved non-terminations and runtimes of VeryMax and Ultimate, it suffices to run "./run_best_configs.sh". Running the script "results_nonterm.py" then creates a csv file which contains results for RevTerm, Ultimate and VeryMax on each of the 112 benchmarks, as well as Table 1 results at the end. The "table1.csv" file in the "spreadsheets" directory, contains the detailed results obtained for Table 1 on the first platform of our paper. Moreover, one can run "results_complete.py" to generate a csv file including a row for each of the 335 benchmarks. 

Running "./run.sh" can be used to reproduce the experimental results in Table 2 for RevTerm, as well as results on performance by tool configurations in Table 3, "Performance by configuration" paragraph in page 11 of our paper, and Table 4 in the Supplementary Material:

2. Table 2 - Table 2 contains results obtained on Platform 2 in our paper (see paragraph "Experimental results" in page 10 for details). On this platform, we ran all configurations that *do not* use the bclt solver. Thus, the number of proved benchmarks in Table 2 can be obtained by counting all programs and fastest configuration times for which the tool proves non-termination with a configuration that does not use bclt. The "table2.csv" file in the "spreadsheets" directory contains the detailed results for generating Table 2.

3. Performance by configuration (Table 3, "Performance by configuration" paragraph in page 11, and Table 4 in the Supplementary Material) - This information can be obtained by processing the output of "./run.sh" which reports results of each configuration on each input program. The "table3.csv" file in the "spreadsheets" directory contains a detailed list of all configurations of RevTerm that proved non-termination for each benchmark. The final results for table 3 of the paper are present in "CL109" to "CV109" cells of the spreadsheet.

To reproduce the experimental results in Table 2 for other tools, one needs to evaluate all tools on the StarExec platform. We needed to do this since we could not install all required dependencies for AProVE and since LoAT does not support our input format. AProVE is available at StarExec and can be run, and for LoAT we took the experimental results from [22] which ran it with the same timeout of 60s. For details on the results in Table 2 of other tools, see the paragraph "Experimental results", pages 10-11 in the paper.

The StarExec platform contains the results of the “Termination of C Integer Programs” TermComp’19 category (follow root/Termination/termcomp19/C_integer):
https://www.starexec.org/starexec/secure/explore/spaces.jsp?id=334158
There, one can find the competition results, benchmarks, as well as binaries for the 3 participating tools (VeryMax, Ultimate, AProVE).
To run a “job” on StarExec, one needs to (1) specify “solvers”, (2)  “benchmarks”, and (3) the CPU timeout. We uploaded RevTerm, and together with these 3 tools created a job with “Termination of C Integer Programs” TermComp’19 benchmarks and timeout of 60s to match [22], so we could use their results for LoAT.
For details on how to execute a job in StarExec, see the help page of StarExec.
