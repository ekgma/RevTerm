import glob
import os
import statistics as stats

results = {}
totalNO = {'RevTerm':0, 'VeryMax':0, 'Ultimate':0}
totalYES = {'RevTerm':0, 'VeryMax':0, 'Ultimate':0}
totalMAYBE = {'RevTerm':0, 'VeryMax':0, 'Ultimate':0}

uniqueNO = {'RevTerm':0, 'VeryMax':0, 'Ultimate':0}
RevTermNO=[]
VeryMaxNO=[]
UltimateNO=[]
RevTermAll=[]
VeryMaxAll=[]
UltimateAll=[]


def readFromResults(benchmark): #read results from output/benchmark.out 
    ret={'RevTerm':'', 'VeryMax':'', 'Ultimate':'','output':''}
    
    ##RevTerm##
    file_dir = "output/RevTerm/"+benchmark+".out"
    if os.path.isfile(file_dir):
        reader = open(file_dir, "r")
        nonTerm=False
        term=False
        for x in reader:
            if x=="Non-Terminating\n":
                nonTerm=True
                ret['output']='NO'
                totalNO['RevTerm']+=1
            if x.startswith("total time used") & nonTerm:
                ret['RevTerm']=int(x.split(' ')[-1])/1000.000
                RevTermNO.append(ret['RevTerm'])
                RevTermAll.append(ret['RevTerm'])
                break
    
    ##VeryMax##
    file_dir = "output/VeryMax/"+benchmark+".out"
    if os.path.isfile(file_dir):
        reader = open(file_dir, "r")
        nonTerm=False
        term=False
        for x in reader:
            if x=="NO\n":
                nonTerm=True
                ret['output']='NO'
                totalNO['VeryMax']+=1
            elif x=="YES\n":
                term=True
                ret['output']='YES'
                totalYES['VeryMax']+=1
            if x.startswith("total time used") & (nonTerm | term):
                ret['VeryMax']=int(x.split(' ')[-1])/1000.000
                VeryMaxAll.append(ret['VeryMax'])
                if nonTerm:
                    VeryMaxNO.append(ret['VeryMax'])
    ##Ultimate##
    file_dir = "output/Ultimate/"+benchmark+".out"
    if os.path.isfile(file_dir):
        reader = open(file_dir, "r")
        nonTerm=False
        for x in reader:
            if x=="NO\n":
                nonTerm=True
                ret['output']='NO'
                totalNO['Ultimate']+=1
            elif x=="YES\n":
                term=True
                ret['output']='YES'
                totalYES['Ultimate']+=1
            if x.startswith("total time used") & (nonTerm | term):
                ret['Ultimate']=int(x.split(' ')[-1])/1000.000
                UltimateAll.append(ret['Ultimate'])
                if nonTerm:
                    UltimateNO.append(ret['Ultimate'])
    return ret


benchmarks = glob.glob("Benchmarks/C-Integer/*.c")



for filename in benchmarks:
    bench = os.path.basename(filename)
    out=readFromResults(bench)
    results[bench] = out

    if out['output']=='NO' and out['RevTerm']!='' and out['VeryMax']=='' and out['Ultimate']=='':
        uniqueNO['RevTerm']+=1
    elif out['output']=='NO' and out['RevTerm']=='' and out['VeryMax']!='' and out['Ultimate']=='':
        uniqueNO['VeryMax']+=1
    elif out['output']=='NO' and out['RevTerm']=='' and out['VeryMax']=='' and out['Ultimate']!='':
        uniqueNO['Ultimate']+=1

r=len(results)
totalMAYBE['RevTerm']=r-totalNO['RevTerm']-totalYES['RevTerm']
totalMAYBE['Ultimate']=r-totalNO['Ultimate']-totalYES['Ultimate']
totalMAYBE['VeryMax']=r-totalNO['VeryMax']-totalYES['VeryMax']

f = open('results.csv','w')
print("Benchmark, RevTerm, Ultimate, VeryMax",file=f)


for bench in sorted(results):
    print(bench,",",results[bench]['RevTerm'], ",",results[bench]['Ultimate'], ",", results[bench]['VeryMax'],",",results[bench]['output'],file=f)

print('',file=f)
print('NO,',totalNO['RevTerm'],',',totalNO['Ultimate'],',',totalNO['VeryMax'],file=f)
print('YES,',totalYES['RevTerm'],',',totalYES['Ultimate'],',',totalYES['VeryMax'],file=f)
print('MAYBE,',totalMAYBE['RevTerm'],',',totalMAYBE['Ultimate'],',',totalMAYBE['VeryMax'],file=f)
print('Unique NO,',uniqueNO['RevTerm'],',',uniqueNO['Ultimate'],',',uniqueNO['VeryMax'],file=f)

print('AVERAGE,',stats.mean(RevTermAll),',',stats.mean(UltimateAll),',',stats.mean(VeryMaxAll),file=f)
print('STDEV,',stats.stdev(RevTermAll),',',stats.stdev(UltimateAll),',',stats.stdev(VeryMaxAll),file=f)
print('AVERAGE NO,',stats.mean(RevTermNO),',',stats.mean(UltimateNO),',',stats.mean(VeryMaxNO),file=f)
print('STDEV,',stats.stdev(RevTermNO),',',stats.stdev(UltimateNO),',',stats.stdev(VeryMaxNO),file=f)

# print('AVERAGE,=AVERAGE(B2:B'+str(r+1)+'),=AVERAGE(C2:C'+str(r+1)+'),=AVERAGE(D2:D'+str(r+1)+')',file=f)
# print('STDEV,=STDEV(B2:B'+str(r+1)+'),=STDEV(C2:C'+str(r+1)+'),=STDEV(D2:D'+str(r+1)+')',file=f)


