# -*- coding: utf-8 -*-
from __future__ import division
import csv
import matplotlib.mlab as mlab
import matplotlib.pyplot as plt
import numpy as np
fldName = 'greedyWalkPathLenght'
#inFileName  = 'commonFeatures_erdesh.csv'
inFileName  = 'commonFeatures nn=3.csv'
#inFileName = 'commonFeatures_sp_erdesh.csv'
#fldName = 'sp'
#outFileName = fldName + "_distr.csv"
outFileName= "succeed.csv"

n=0
maxNumber=0
lenDistr=[0 for i in xrange(100)]
goodLenDistr=[0 for i in xrange(100)]

#with open('commonFeatures_erdesh.csv', 'rb') as csvfile:
with open(inFileName) as csvfile:
   #spreader = csv.reader(csvfile, delimiter=';', quotechar='|')
   reader = csv.DictReader(csvfile, delimiter=';', quotechar='|');
   for row in reader:
      n+=1
      x = int(row[fldName])
      lenDistr[x]+=1
      if x > maxNumber:
         maxNumber = x
      if int(row['graphDistance']) == 0:
         goodLenDistr[x]+=1


with open(outFileName, 'wb') as csvWriterFile:
   fieldnames = ['lenght', 'probability', 'succeed']
   writer = csv.DictWriter(csvWriterFile, fieldnames=fieldnames,delimiter=';')
   writer.writeheader()
   for i in xrange(maxNumber):
      writer.writerow({'lenght': i, 'probability': str(float(lenDistr[i])/n).replace(".",","), 'succeed': str(float(goodLenDistr[i])/n).replace(".",",")  })

