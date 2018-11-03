
import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class WordCountExample {

    public static class TokenizerMapper
    extends Mapper<Object, Text, Text, IntWritable>{

        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        public void map(Object key, Text value, Context context
                ) throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(value.toString());
            while (itr.hasMoreTokens()) {
                word.set(itr.nextToken());
                context.write(word, one);               
            }
        }
    }
       
    public static class IntSumReducer
    extends Reducer<Text,IntWritable,Text,IntWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values,
                Context context
                ) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }

    public static class TheSecondLetterTokenizerMapper
    extends Mapper<Object, Text,Text,IntWritable>{

        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        public void map(Object key, Text value, Context context
                ) throws IOException, InterruptedException {
            String[] kv=value.toString().split("\t");
            String word= kv[0].toLowerCase().replaceAll("[^a-z]", "");
            int count=Integer.parseInt(kv[1]);
            if(word.length()>1)
                context.write(new Text(word.substring(1, 2)),new IntWritable(count));
            }
        }

       
        public static class SortTokenizerMapper
        extends Mapper<Object,Text,IntWritable,Text>{

        private final static IntWritable one = new IntWritable(1);
        private String word = new String();

        public void map(Object key, Text value, Context context
                ) throws IOException, InterruptedException {
            String[] kv=value.toString().split("\t");
             word= kv[0];
            int count=Integer.parseInt(kv[1]);
                context.write(new IntWritable(-count),new Text(word));
           
        }
    }
       
        public static class SortReducer
        extends Reducer<IntWritable,Text,Text,IntWritable> {
        private IntWritable result = new IntWritable();
        private Text word =new Text();

            public void reduce(IntWritable key, Iterable<Text> values,
                    Context context
                    ) throws IOException, InterruptedException {
                int sum = -key.get();
           
                Iterator<Text> ite = values.iterator();
            while(ite.hasNext())
            {
                 Text t=ite.next();
                 String str =t.toString();
                 word.set(str);
            }
                result.set(sum);
                context.write(new Text(word), new IntWritable(sum));
            }
        }
   
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "word count");
        job.setJarByClass(WordCountExample.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        job.waitForCompletion(true);
       
        Configuration conf2 = new Configuration();
        Job job2 = Job.getInstance(conf2, "Second letter count with reverse key value");
        job2.setJarByClass(WordCountExample.class);
        job2.setMapperClass(TheSecondLetterTokenizerMapper.class);
        job2.setCombinerClass(IntSumReducer.class);
        job2.setReducerClass(IntSumReducer.class);       
        job2.setOutputKeyClass(Text.class);
        job2.setOutputValueClass(IntWritable.class);       
        FileInputFormat.addInputPath(job2, new Path(args[1]));
        FileOutputFormat.setOutputPath(job2, new Path(args[2]));
        job2.waitForCompletion(true);
       
        Configuration conf3 = new Configuration();
        Job job3 = Job.getInstance(conf3, "Sorted Result");
        job3.setJarByClass(WordCountExample.class);
        job3.setMapperClass(SortTokenizerMapper.class);   
        job3.setReducerClass(SortReducer.class);   
        job3.setMapOutputKeyClass(IntWritable.class);
        job3.setMapOutputValueClass(Text.class);       
        job3.setOutputKeyClass(Text.class );
        job3.setOutputValueClass(IntWritable.class);       
        FileInputFormat.addInputPath(job3, new Path(args[2]));
        FileOutputFormat.setOutputPath(job3, new Path(args[3]));       
        System.exit(job3.waitForCompletion(true) ? 0 : 1);
    }
}
