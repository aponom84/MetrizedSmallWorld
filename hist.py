# -*- coding: utf-8 -*-
import csv

import matplotlib.mlab as mlab
import matplotlib.pyplot as plt
import numpy as np
#fldName = 'greedyWalkPathLenght'
#inFileName  = 'commonFeatures_erdesh.csv'
inFileName = 'commonFeatures_sp_erdesh.csv'
fldName = 'sp'
outFileName = fldName + "_distr.csv"

numbers=[]
#with open('commonFeatures_erdesh.csv', 'rb') as csvfile:
with open(inFileName) as csvfile:
   #spreader = csv.reader(csvfile, delimiter=';', quotechar='|')
   reader = csv.DictReader(csvfile, delimiter=';', quotechar='|');
   for row in reader:
      numbers.append(int( row[fldName]))

   print len(numbers)

print numbers[0:20]
myMaxNumber = np.amax(numbers)
print "myMaxNumber is %i" % myMaxNumber
#bins = array.array('i',(i for i in range(0,myMaxNumber)))
bins = [i for i in xrange(myMaxNumber+1)]

n, bins, patches = plt.hist(numbers, bins, normed=1, facecolor='green', alpha=0.5)


print n



with open(outFileName, 'wb') as csvWriterFile:
   fieldnames = ['lenght', 'probability']
   writer = csv.DictWriter(csvWriterFile, fieldnames=fieldnames,delimiter=';')
   writer.writeheader()
   for x,y in zip(bins, n):
      writer.writerow({'lenght': x, 'probability': str(y).replace(".",",")})
      #print "x: %i y: %.5f" % (x,y)




# add a 'best fit' line
#mu=5;
#sigma = 3;
#y = mlab.normpdf(bins, mu, sigma)
#plt.plot(bins, y, 'r--')

#plt.xlabel("Smart")
plt.xlabel("Lenght")
plt.ylabel('Probability')
plt.title(r'Histogram of IQ: $\mu=100$, $\sigma=15$')
# Tweak spacing to prevent clipping of ylabel
plt.subplots_adjust(left=0.15)
#plt.show()