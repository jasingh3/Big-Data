import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Join {
	public static class Mapper1 extends
	Mapper<Object, Text, Text, Text> {
public void map(Object key, Text value, Context context)
		throws IOException, InterruptedException {
	String rec = value.toString();
	String[] parts = rec.split(",");
	context.write(new Text(parts[1]), new Text("first\t" + parts[3]));
}
}

public static class Mapper extends
	Mapper<Object, Text, Text, Text> {
public void map(Object key, Text value, Context context)
		throws IOException, InterruptedException {
	String rec = value.toString();
	String[] parts = rec.split(",");
	context.write(new Text(parts[0]), new Text("Second\t" + parts[1]));
}
}

public static class Reducer extends
	Reducer<Text, Text, Text, Text> {
public void reduce(Text key, Iterable<Text> values, Context context)
		throws IOException, InterruptedException {
	String likes = "";
	String date = "";

	for (Text t : values) {
		String parts[] = t.toString().split("\t");

		if (parts[0].equals("Second")) {

			date = parts[1];
			
		} else if (parts[0].equals("first")) {
			likes = parts[1];
		}
	}
	if (likes != "" & date !=""){
	String str = "\t"+ date + "\t"+likes;
	context.write(new Text(key), new Text(str));}
}
}

public static void main(String[] args) throws Exception {
Configuration conf = new Configuration();
Job job = new Job(conf, "Join");
job.setJarByClass(Join.class);
job.setReducerClass(Reducer.class);
job.setOutputKeyClass(Text.class);
job.setOutputValueClass(Text.class);
MultipleInputs.addInputPath(job, new Path(args[0]),TextInputFormat.class, Mapper1.class);
MultipleInputs.addInputPath(job, new Path(args[1]),TextInputFormat.class, Mapper2.class);
Path outputPath = new Path(args[2]);
FileOutputFormat.setOutputPath(job, outputPath);
outputPath.getFileSystem(conf).delete(outputPath);
System.exit(job.waitForCompletion(true) ? 0 : 1);
}

}
