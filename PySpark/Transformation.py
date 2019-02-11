import os

os.environ["PYSPARK_PYTHON"]="/home/jasbir/anaconda3/bin/python"


# Import required packages
from pyspark.sql import SparkSession
from pyspark.sql.types import DateType, TimestampType, IntegerType, FloatType, LongType, DoubleType,StringType
from pyspark.sql.types import StructType, StructField
# from org.apache.spark.expressions import *
# from org.apache.spark.functions import *
from pyspark.sql.utils import *
from pyspark.sql import functions as F


if __name__ == "__main__":
    spark = SparkSession.builder.appName("Hello world").master("local[*]").getOrCreate()

    sc= SparkSession.sparkContext

    custom_schema = StructType([StructField('id', StringType(), True),
                                StructField('Startdate', TimestampType(), True),
                                StructField('Enddate', TimestampType(), True)])

    df = spark.read.csv('/home/jasbir/Documents/user.tsv', header=False , schema=custom_schema, sep='\t')


    timeFmt = "yyyy-MM-dd HH:mm:ss"

    #print(df.show())

    timediff = (F.unix_timestamp('Enddate', format=timeFmt) - F.unix_timestamp('Startdate', format=timeFmt))/60


    df_minutes= df.withColumn("no_of_mins",timediff)
    print(df_minutes.show())

    # df_minutes=df_minutes.select('id', 'Startdate')

    # print([df[cols_of_interest].show()])

    def ToCSV(x):
        return ','.join([str(i) for i in x]) + '\n'
        # print(x)
        # return x

    # def func(x):
    #     for i in x:
    #         print(i)
    #         x=x*int(x[3])
    #         # ls.append(x[0:2])
    #     return  x
    #     # return x


    def func(x):
        ls=[]
        for i in range(0,int(x[3])):
            ls .append(x[0:2])
        # print(tuple(ls))
        return tuple(ls)

    dd_result = df_minutes.rdd.flatMap(lambda x: func(x))

    dd_result = dd_result.map(lambda x: ToCSV(x))

    print(dd_result.count())


    dd_result.saveAsTextFile("/home/jasbir/Documents/Pyspark")
