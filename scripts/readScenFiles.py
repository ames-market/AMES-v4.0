#!/usr/bin/env python

'''
A quick and dirty script to be able to generate data
for the ARPAe 1C milestone.
'''

import re
import sys

def formatAsCSV(data):
    line = ""
    numElems = len(data)

    for i, e in enumerate(data):
        line = line + str(e)
        if i < (numElems - 1):
            line = line + ", "

    return line + "\n"


def formatAsJavaArrayLit(dataType, arrayElems, comment=None):
    '''
    Convert the data to a java array declaration.
    '''
    arrayDecl = "new " + dataType

    arrayDecl = arrayDecl + "{"

    numElems = len(arrayElems)
    for i, e in enumerate(arrayElems):
        arrayDecl = arrayDecl + str(e)
        if i < (numElems - 1):
            arrayDecl = arrayDecl + ", "

    arrayDecl =  "%s}//%s\n" % (arrayDecl, comment)

    return arrayDecl

def formatOutput(data, lineformatter):
    '''Some redundancy with formatAllAsJavaArray.'''
    csv = ''
    for k in sorted(data.keys()):
        day = data[k]
        hourLit = []
        for hk in sorted(day.keys()):
            csv = csv + lineformatter(day[hk])

    return csv

def formatAllAsJavaArrayLit(data, ):
    '''
    Convert the data to java double[][][] declaration
    '''
    dayLit = []
    f = formatAsJavaArrayLit
    for k in sorted(data.keys()):
        day = data[k]
        hourLit = []
        for hk in sorted(day.keys()):
            hourLit.append( f('double[]', day[hk], ("d:%ih:%i" % (k,hk) )) )
        dayLit.append(f('double[][]', hourLit, ("d:%i") % k  ))

    return f('double[][][]', dayLit)

def scaleAll(scalefactor, data):
    '''
    Apply the scaling factor to the data
    '''
    for k in data.keys():
        day = data[k]
        for hk in day.keys():
            scaled = map(lambda x: scalefactor * x , day[hk])
            day[hk] = scaled


def ReadSplitDataScenario(inputfile):
    '''
    Read a scenario from multiple files.
    '''

    def ReadSingleDay(inputFile) :
        '''Read a file in the split/single day format.'''

        HOUR_IDX = 0
        sys.stderr.write("reading %s\n" % str(inputFile))
        with open(inputFile) as f:
            #read until 'header' line
            day = -1
            foundHeader = False
            for line in f:
                line = line.strip()
                if line is None:
                    continue
                if not foundHeader:
                    if line.startswith("Day") :
                        p = line.split(":")
                        day = int(p[1])
                    elif line.startswith("Hour") :
                        foundHeader = True
                else:
                    #nothing like duplicating code...
                    if day < 1 :
                        raise Exception("Invalid day %i." % day)
                    elems = re.split('\s+', line)
                    hour = int(elems[HOUR_IDX]);
                    if data.has_key(day) :
                        dayProfiles = data[day]
                    else:
                        dayProfiles = dict()
                        data[day] = dayProfiles

                    hours = elems[HOUR_IDX + 1:]
                    dayProfiles[hour] = map(lambda x : float(x), hours)


    d = 1
    dm = 364
    data = dict()
    while d <= dm:
        ReadSingleDay("%s_%i.dat" %(inputfile, d))
        d = d + 1

    return data

def ReadAllIn1ScenarioData(inputfile):
    '''
    Read a scenario in 1 file
    '''
    DAY_IDX = 0
    HOUR_IDX = 1
    foundData = False
    data = dict()
    with open(inputfile) as f:
        for read_data in f:
            read_data = read_data.strip()
            if read_data is not None:
                if foundData :
                    elems = re.split('\s+', read_data)
                    day = int(elems[DAY_IDX].strip())
                    hour = int(elems[HOUR_IDX].strip())
                    if data.has_key(day) :
                        dayProfiles = data[day]
                    else:
                        dayProfiles = dict()
                        data[day] = dayProfiles
                    dayProfiles[hour] = map(lambda x : float(x), elems[2:])
                #otherwise, still looking for the header
                else :
                    if 'Day' in read_data:
                        foundData = True

    return data


if __name__ == "__main__":

    run8Bus = True
    #run8Bus = False

    if run8Bus :
        eightBusBase = "../TEST-DATA/Ames_scenarios/AMESscen1"
        scalefactor = 0.02616777936897704
        data = ReadSplitDataScenario(eightBusBase)
    else:
        inputfile = "test.dat"
        scalefactor = 0.3831897415347626
        data = ReadAllIn1ScenarioData(inputfile)

    scaleAll(scalefactor, data)

    if run8Bus:
        print formatOutput(data, formatAsCSV)
    else:
        print formatAllAsJavaArrayLit(data)
