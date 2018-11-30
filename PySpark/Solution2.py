import os
import sys
import re

os.environ['SPARK_HOME'] = '/usr/lib/spark'
os.environ['PYSPARK_PYTHON'] = '/usr/local/bin/python2.7'
os.environ['PYSPARK_SUBMIT_ARGS'] = ('--packages com.databricks:spark-csv_2.10:1.3.0 pyspark-shell')
os.environ['JAVA_TOOL_OPTIONS'] = "-Dhttps.protocols=TLSv1.2"



sys.path.append('/usr/lib/spark/python')
sys.path.append('/usr/lib/spark/python/lib/py4j-0.9-src.zip')

from pyspark import SparkContext
from pyspark import HiveContext

sc = SparkContext()
sqlContext = HiveContext(sc)

line = sc.textFile('file:///home/cloudera/Downloads/pg100.txt')

#cleaning data
def splittowords(s):
    words = s.encode('utf-8')
    words = words.lower()
    clean = re.sub('[^a-zA-Z ]+', '', words)
    clean = re.sub(r"\s+", " ", clean).lstrip().rstrip()
    clean = re.sub(r"\b[a-zA-Z]\b", "", clean)
    clean = clean.replace("\n\n"," ")
    return clean

from operator import add

#converting to single RDD
wordfromline = line.flatMap(lambda x: splittowords(x).split()).map(lambda x: (x[1]))
#counting number of each letter
finalcount = wordfromline.flatMap(lambda x:[(c,1) for c in x]).reduceByKey(add)
#soring and displaying in required format
sortedvalue = finalcount.map(lambda (x,y) : (y,x)).sortByKey(ascending = False).map(lambda (x,y) : (y,x)).collect()


for w in sortedvalue:
    print(w)


sc.stop()






