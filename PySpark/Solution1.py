import os
import sys

os.environ['SPARK_HOME'] = '/usr/lib/spark'
os.environ['PYSPARK_PYTHON'] = '/usr/local/bin/python2.7'
os.environ['PYSPARK_SUBMIT_ARGS'] = ('--packages com.databricks:spark-csv_2.10:1.5.0 pyspark-shell')
os.environ['JAVA_TOOL_OPTIONS'] = "-Dhttps.protocols=TLSv1.2"

sys.path.append('/usr/lib/spark/python')
sys.path.append('/usr/lib/spark/python/lib/py4j-0.9-src.zip')


from pyspark import SparkContext
from pyspark import HiveContext


sc = SparkContext()
sqlContext = HiveContext(sc)

# template for importing csv file
df = sqlContext.read.format('com.databricks.spark.csv') \
    .options(header='true', inferschema='true') \
    .load('file:///home/cloudera/Downloads/tick_data.csv')


#############
df.registerTempTable('temp')


one_a_distinct=sqlContext.sql('select count(distinct DATE) from temp')
for ddate in one_a_distinct.collect():
    print(ddate)


one_b_distinct=sqlContext.sql('select count(distinct SYM_ROOT) from temp')
for stock in one_b_distinct.collect():
    print(stock)

dfTrade=sqlContext.sql('select *,hour(TIME_M) as TIME_H from temp ')
dfTrade.show()
dfTrade.registerTempTable('temp1')

dfTrade1=sqlContext.sql('select  DATE,TIME_H,SYM_ROOT, TRADE,SUM(TRADE) over (partition by SYM_ROOT,TIME_H)as SIZE_H from temp1')
dfTrade1.show(n=10)
dfTrade1.registerTempTable('temp2')

dfTrade2=sqlContext.sql('select FIRST(DATE) as DATE,TIME_H,FIRST(SYM_ROOT) as SYM_ROOT,FIRST(SIZE_H)AS SIZE_H, ((LAST(TRADE)-FIRST(TRADE))/FIRST(TRADE)) as return from temp2 group by SYM_ROOT,TIME_H order by SYM_ROOT,TIME_H')
dfTrade2.show(n=20)
dfTrade2.registerTempTable('temp3')

# template for exporting you data in csv format
dfTrade2.coalesce(1).write.format('com.databricks.spark.csv') \
    .option('header', 'true') \
    .save('file:///home/cloudera/Desktop/Output_Spark1.csv')



#############


# template for exporting you data in csv format
df_output.write.format('com.databricks.spark.csv') \
    .option('header', 'true') \
    .save(output_path)

# stop spark session at the end
sc.stop()
