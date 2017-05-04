import csv
import matplotlib.mlab as mlab
import matplotlib.pyplot as plt
import numpy as np


numbers=[]
with open('commonFeatures_sp.csv', 'rb') as csvfile:
   spreader = csv.reader(csvfile, delimiter=';', quotechar='|')
   for row in spreader:
      numbers.append(int(row[0]))

   print len(numbers)

print numbers[0:20]

n, bins, patches = plt.hist(numbers, 20, normed=1, facecolor='green', alpha=0.5)

# add a 'best fit' line
mu=5;
sigma = 3;
y = mlab.normpdf(bins, mu, sigma)
plt.plot(bins, y, 'r--')

plt.xlabel("Smart")
plt.ylabel('Probability')
plt.title(r'Histogram of IQ: $\mu=100$, $\sigma=15$')
# Tweak spacing to prevent clipping of ylabel
plt.subplots_adjust(left=0.15)
plt.show()