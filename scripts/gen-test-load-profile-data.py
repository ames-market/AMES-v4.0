#!/usr/bin/env python

#A simple script to generate test data for the load profile test.
#Generates the data algorithmically, such that each entry is unique and coded to its 'postion'
# Each hour profile is coded as DAYHOURINDEX. e.g. Day 2, hour 0, index 1: 201

startDay = 2
endDay = 5
hoursPerDay = 24
numIdxs = 3

for d in range(startDay, endDay + 1):
    print "Day", repr(d)
    for h in range(0, hoursPerDay):
        lp = ""
        for i in range(0, numIdxs):
            lp = lp + repr(d) + repr(h) + repr(i) + "\t"

        print lp.strip()
