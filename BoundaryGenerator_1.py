import numpy as np
import matplotlib.pyplot as plt

class Gene:
    def __init__(self, geneName, chromosome, sense, leftBoundary, rightBoundary):
        self.geneName = geneName
        self.chromosome = chromosome
        self.sense = sense
        self.leftBoundary = leftBoundary
        self.rightBoundary = rightBoundary
        self.leftIntergenicBoundary = leftBoundary
        self.rightIntergenicBoundary = rightBoundary
    def toString(self):
        toString = self.geneName + ' ' + self.chromosome + ' ' + str(self.sense) + ' ' #metadata
        toString = toString + str(self.leftIntergenicBoundary) + ' ' + str(self.leftBoundary) + ' ' #left boudaries
        toString = toString + str(self.rightBoundary) + ' ' + str(self.rightIntergenicBoundary) #right boundaries
        return toString

def gene_type(gene, chromosomeReads):
    if ((gene.chromosome + '1') not in chromosomeReads) or ((gene.chromosome + '0') not in chromosomeReads): return -1
    leftSum = 0
    rightSum = 0
    negReads = {}
    if gene.sense == 1:
        negReads = chromosomeReads[gene.chromosome + '0']
    else:
        negReads = chromosomeReads[gene.chromosome + '1']
    for i in range(gene.leftIntergenicBoundary, gene.leftBoundary + (gene.rightBoundary - gene.leftBoundary)/2):
        leftSum = leftSum + negReads[i]
    for i in range(gene.leftBoundary + (gene.rightBoundary - gene.leftBoundary)/2, gene.rightIntergenicBoundary):
        rightSum = rightSum + negReads[i]
    if (rightSum == 0) or (leftSum / rightSum > 1.5): return 2
    if (leftSum == 0) or (rightSum / leftSum > 1.5): return 1
    return 3
    
def buildAverage(genes, chromosomeReads):
    posAverageCurve = {}
    negAverageCurve = {}
    averageCounters = {}
    #find where the gene should start
    leftIntergenic = 0
    rightIntergenic = 0
    for gene in genes:
        if (gene_type(gene, chromosomeReads) == 1):
            if (gene.leftBoundary - gene.leftIntergenicBoundary > leftIntergenic):
                leftIntergenic = gene.leftBoundary - gene.leftIntergenicBoundary
            if (gene.rightBoundary - gene.leftBoundary > rightIntergenic):
                rightIntergenic = gene.rightBoundary - gene.leftBoundary
    rightIntergenic = rightIntergenic + leftIntergenic
    for gene in genes:
        if (gene_type(gene, chromosomeReads) == 1) and ((gene.chromosome + '1') in chromosomeReads):
            posReads = {}
            negReads = {}
            #get the reads based on sense
            if gene.sense == 1:
                posReads = chromosomeReads[gene.chromosome + '1']
                negReads = chromosomeReads[gene.chromosome + '0']
            else:
                posReads = chromosomeReads[gene.chromosome + '0']
                negReads = chromosomeReads[gene.chromosome + '1']
            #map the left intergenic boundary
            counter = gene.leftBoundary - 1
            curveCounter = leftIntergenic - 1
            while counter >= gene.leftIntergenicBoundary:
                if (curveCounter in posAverageCurve):
                    posAverageCurve[curveCounter] = posAverageCurve[curveCounter] + posReads[counter]
                    negAverageCurve[curveCounter] = negAverageCurve[curveCounter] + negReads[counter]
                    averageCounters[curveCounter] = averageCounters[curveCounter] + 1
                else:
                    posAverageCurve[curveCounter] = posReads[counter]
                    negAverageCurve[curveCounter] = negReads[counter]
                    averageCounters[curveCounter] = 1
                counter = counter - 1
                curveCounter = curveCounter - 1
            #map the average of the genes
            curveCounter = leftIntergenic
            for i in range(gene.leftBoundary, gene.rightBoundary):
                if curveCounter in posAverageCurve:
                    if (not i in posReads) or (not i in negReads):
                        print i
                    posAverageCurve[curveCounter] = posAverageCurve[curveCounter] + posReads[i]   
                    negAverageCurve[curveCounter] = negAverageCurve[curveCounter] + negReads[i]
                    averageCounters[curveCounter] = averageCounters[curveCounter] + 1
                else:
                    posAverageCurve[curveCounter] = posReads[i]
                    negAverageCurve[curveCounter] = negReads[i]
                    averageCounters[curveCounter] = 1     
                curveCounter = curveCounter + 1 
            #map the right intergenic boundary
            curveCounter = rightIntergenic
            for i in range(gene.leftBoundary + rightIntergenic - leftIntergenic, gene.rightIntergenicBoundary):
                if i in posAverageCurve:
                    posAverageCurve[curveCounter] = posAverageCurve[curveCounter] + posReads[counter]   
                    negAverageCurve[curveCounter] = negAverageCurve[curveCounter] + negReads[counter]
                    averageCounters[curveCounter] = averageCounters[curveCounter] + 1
                else:
                    posAverageCurve[curveCounter] = posReads[counter]
                    negAverageCurve[curveCounter] = negReads[counter]
                    averageCounters[curveCounter] = 1 
                curveCounter = curveCounter + 1
    for i in posAverageCurve:
        posAverageCurve[i] = posAverageCurve[i] / averageCounters[i]
        negAverageCurve[i] = negAverageCurve[i] / averageCounters[i] 
    posAverageCurve = np.array(posAverageCurve.values())
    negAverageCurve = np.array(negAverageCurve.values())
    plt.plot(posAverageCurve, 'r')
    plt.plot(negAverageCurve, 'b')
    plt.axvline(x=leftIntergenic, color='k')
    plt.axvline(x=rightIntergenic, color='k')
    plt.show()
    
def getNonOverlappingGenes(genes):
    nonOverlappingGenes = []
    for gene1 in genes:
        canAdd = True
        for gene2 in genes:
            if (gene1.chromosome == gene2.chromosome) and (gene1.leftBoundary < gene2.rightBoundary) and (gene1.leftBoundary >  gene2.leftBoundary):
                canAdd = False
                break
            if (gene1.chromosome == gene2.chromosome) and (gene1.rightBoundary > gene2.leftBoundary) and (gene1.rightBoundary < gene2.rightBoundary):
                canAdd = False
                break
        if canAdd: nonOverlappingGenes.append(gene1)
    return nonOverlappingGenes
            
#start point of the execution
chromosomeReads = {}
genes = {}
threads = []
#first, the reads
bedFileName = raw_input("Enter the name of the reads file: ")
with open(bedFileName) as bedFile:
    oldChromosome = ""
    posReads = {}
    negReads = {}
    maxRead = 0
    sense = -1
    fileHasLines = True
    while fileHasLines:
        line = bedFile.readline()
        if line == "":
            fileHasLines = False
        data = line.split()
        if len(data) > 0:
            chromosome = data[0]
        else:
            chromosome = ""
        if oldChromosome == "":
            oldChromosome = chromosome
        if chromosome == oldChromosome:
            start =  data[1]
            end = data[2] 
            reads = None
            if data[5] == "+":
                reads = posReads
                sense = '1'
            else:
                reads = negReads
                sense = '0'
            start = int(start)
            end = int(end)    
            #if end > maxRead:
            #    maxRead = end 
            for i in range(start, end+1):
                if (i not in reads): #first read
                    reads[i] = 1
                else: #at least one other read
                    reads[i] = reads[i] + 1
        elif ((not fileHasLines) or len(posReads) > 0 or len(negReads) > 0):
            chromosomeReads[oldChromosome + '1'] = posReads
            chromosomeReads[oldChromosome + '0'] = negReads
            print oldChromosome
            oldChromosome = chromosome
#second, store the reads for the genes          
bedFileName = raw_input("Enter the name of the genes file: ")
with open(bedFileName) as bedFile:
    while True:
        line = bedFile.readline()
        if line == "":
            break
        data = line.split()
        if len(data) == 0:
            break #quit the loop early if there is no more data to be read
        gene = data[0]
        chromosome = data[1]
        if data[2] == '+':
            sense = 1
        else:
            sense = 0
        leftBoundary = int(data[3])
        rightBoundary = int(data[4])
        genes[gene] = Gene(gene, chromosome, sense, leftBoundary, rightBoundary)
#third, read the intergenic
maxChromosomeReads = {}
bedFileName = raw_input("Enter the name of the intergenic file: ")
with open(bedFileName) as bedFile:
    while True:
        line = bedFile.readline()
        if line == "":
            break
        data = line.split()
        if len(data) == 0:
            break
        chromosome = data[0]
        leftIntergenicBoundary = int(data[1])
        rightIntegenicBoundary = int(data[2])
        if chromosome not in maxChromosomeReads:
            maxChromosomeReads[chromosome] = rightIntegenicBoundary
        elif rightIntegenicBoundary > maxChromosomeReads[chromosome]:
            maxChromosomeReads[chromosome] = rightIntegenicBoundary
        for geneName, gene in genes.iteritems():
            if gene.chromosome == chromosome:
                if gene.rightBoundary + 1 == leftIntergenicBoundary:
                    gene.rightIntergenicBoundary = rightIntegenicBoundary
                if gene.leftBoundary -1 == rightIntegenicBoundary:
                    gene.leftIntergenicBoundary = leftIntergenicBoundary
    for chromosome in maxChromosomeReads:
        if ((chromosome + '1') in chromosomeReads) and ((chromosome + '0') in chromosomeReads):
            posReads = chromosomeReads[chromosome + '1']
            negReads = chromosomeReads[chromosome + '0']
            for i in range(0, maxChromosomeReads[chromosome]):
                if i not in posReads: posReads[i] = 0
                if i not in negReads: negReads[i] = 0
#write it to the file
nonOverlappingGenes = getNonOverlappingGenes(genes.values())
genesFileName = raw_input("Enter the name of the file you want to store your genes in: ")
genesFile = open(genesFileName, 'w')
for gene in nonOverlappingGenes:
    print>>genesFile, gene.toString() 
buildAverage(nonOverlappingGenes, chromosomeReads) 
#finally, map the gene
geneName = ''
while (geneName != 'STOP'):
    geneName = raw_input("Enter the name of the gene, or STOP to stop: ")
    if ((geneName != 'STOP') and (geneName in genes)):
        gene = genes[geneName]
        if gene.sense == 1:
            posReads = chromosomeReads[gene.chromosome + '1']
            negReads = chromosomeReads[gene.chromosome + '0']
        else:
            posReads = chromosomeReads[gene.chromosome + '0']
            negReads = chromosomeReads[gene.chromosome + '1']
        #get the reads for this specific gene
        posGeneReads = []
        negGeneReads = []
        numRange = []
        for i in range(gene.leftIntergenicBoundary, gene.rightIntergenicBoundary + 1):
            posGeneReads.append(posReads[i])
            negGeneReads.append(negReads[i])
            numRange.append(i)
        posReads = None
        negReads = None
        numRange = np.array(numRange)
        posGeneReads = np.array(posGeneReads)
        negGeneReads = np.array(negGeneReads)
        markers_on = [gene.leftBoundary, gene.rightBoundary]
        plt.plot(numRange, posGeneReads, 'r', markevery=markers_on)
        plt.plot(numRange, negGeneReads, 'b', markevery=markers_on)
        plt.axvline(x=gene.leftBoundary, color='k')
        plt.axvline(x=gene.rightBoundary, color='k')
        plt.show()