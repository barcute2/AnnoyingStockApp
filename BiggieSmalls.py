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

def buildAverage(genes, wildTypeChromosomeReads, mutantChromosomeReads):
    overOnePointFive = []
    underOnePointFive = []
    overOnePointFiveCounter = []
    underOnePointFiveCounter = []
    #find where the gene should start
    for gene in genes:
        chromosome = gene.chromosome
        for counter in range(0, gene.rightBoundary - gene.leftBoundary):
            if (wildTypeChromosomeReads[][gene.leftBoundary + counter] / mutantChromosomeReads[gene.leftBoundary + counter] > 2):
                if counter not in overOnePointFive: 
                    overOnePointFive[counter] = 1
                    overOnePointFiveCounter[counter] = 1
                else: 
                    overOnePointFive[counter] = overOnePointFive[counter] + 1
                    overOnePointFiveCounter[counter] = overOnePointFiveCounter[counter] + 1
            if (mutantChromosomeReads[gene.leftBoundary + counter] / wildTypeChromosomeReads[gene.leftBoundary + counter] > 2):
                if counter not in underOnePointFive: 
                    underOnePointFive[counter] = 1
                    underOnePointFiveCounter[counter] = 1
                else: 
                    underOnePointFive[counter] = underOnePointFive[counter] + 1
                    underOnePointFiveCounter[counter] = underOnePointFiveCounter[counter] + 1
    for counter in overOnePointFive: overOnePointFive[counter] = overOnePointFive[counter] / overOnePointFiveCounter[counter]
    for counter in underOnePointFive: underOnePointFive[counter] = underOnePointFive[counter] / underOnePointFiveCounter[counter]            
    posAverageCurve = np.array(overOnePointFive)
    negAverageCurve = np.array(underOnePointFive)
    plt.plot(posAverageCurve, 'r')
    plt.plot(negAverageCurve, 'b')
    #plt.axvline(x=leftIntergenic, color='k')
    #plt.axvline(x=rightIntergenic, color='k')
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

def getFinalGenes(genes, wildTypeChromosomeReads, mutantChromosomeReads):
    finalGenes = []
    for gene in genes:
        chromosome = gene.chromosome
        canAdd = True
        for counter in range(gene.leftBoundary, gene.rightBoundary):
            if (chromosome + '1' not in wildTypeChromosomeReads) or (chromosome + '0' not in wildTypeChromosomeReads):
                canAdd = False
                break
            if (chromosome + '1' not in mutantChromosomeReads) or (chromosome + '0' not in mutantChromosomeReads):
                canAdd = False
                break
            if (counter not in wildTypeChromosomeReads[chromosome + '1']) or (counter not in mutantChromosomeReads[chromosome + '1']):
                canAdd = False
                break
            if (counter not in wildTypeChromosomeReads[chromosome + '0']) or (counter not in mutantChromosomeReads[chromosome + '0']):
                canAdd = False
                break
            if (wildTypeChromosomeReads[chromosome + '1'][counter] + wildTypeChromosomeReads[chromosome + '0'][counter] < 50):
                canAdd = False
                break
            if (mutantChromosomeReads[chromosome + '1'][counter] + mutantChromosomeReads[chromosome + '0'][counter] < 50):
                canAdd = False
                break
        if canAdd: finalGenes.append(gene)
    return finalGenes

def getReads(bedFileName):
    chromosomeReads = {}
    with open(bedFileName) as bedFile:
        oldChromosome = ""
        posReads = {}
        negReads = {}
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
                else:
                    reads = negReads
                start = int(start)
                end = int(end)    
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
    return chromosomeReads
                
#start point of the execution
#first, the reads
bedFileName = raw_input("Enter the name of the wild type reads file: ")
wildTypeChromosomeReads = getReads(bedFileName)
bedFileName = raw_input("Enter the name of the mutant reads file: ")
mutantChromosomeReads = getReads(bedFileName)
#second, store the reads for the genes   
genes = {}       
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
        if ((chromosome + '1') in wildTypeChromosomeReads) and ((chromosome + '0') in wildTypeChromosomeReads):
            posReads = wildTypeChromosomeReads[chromosome + '1']
            negReads = wildTypeChromosomeReads[chromosome + '0']
            for i in range(0, maxChromosomeReads[chromosome]):
                if i not in posReads: posReads[i] = 0
                if i not in negReads: negReads[i] = 0
        if ((chromosome + '1') in mutantChromosomeReads) and ((chromosome + '0') in mutantChromosomeReads):
            posReads = mutantChromosomeReads[chromosome + '1']
            negReads = mutantChromosomeReads[chromosome + '0']
            for i in range(0, maxChromosomeReads[chromosome]):
                if i not in posReads: posReads[i] = 0
                if i not in negReads: negReads[i] = 0
#write it to the file
nonOverlappingGenes = getNonOverlappingGenes(genes.values())
finalGenes = getFinalGenes(nonOverlappingGenes, wildTypeChromosomeReads, mutantChromosomeReads)
genesFileName = raw_input("Enter the name of the file you want to store your genes in: ")
genesFile = open(genesFileName, 'w')
for gene in finalGenes:
    print>>genesFile, gene.toString() 
buildAverage(finalGenes, wildTypeChromosomeReads, mutantChromosomeReads)  