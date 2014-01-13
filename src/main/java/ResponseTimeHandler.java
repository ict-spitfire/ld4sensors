import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;


public class ResponseTimeHandler {

	private static final String path = "/home/iammyr/experiment/post-ldrem-time.out";

	public static double findMean(double array[]){
		double total = 0;
		for(int i = 0; i < array.length; i++){
			total = total + array[i];
		}
		double mean = total/array.length;
		return mean;
	}
	public static double findStandardDeviation(double array[], double mean){
		System.out.println("Mean is: "+mean);
		double d1 = 0, d2 = 0, d3 = 0;
		for(int i = 0; i < array.length; i++){
			d2 = (mean - array[i])*(mean - array[i]);
			d1 = d2 + d1;
		}
		d3 = Math.sqrt((d1/(array.length-1)));
		System.out.println("Standard Deviation: " + d3);
		return d3;
	}


	public static void main(String[] args){
		File file = new File(path);
		BufferedReader reader = null;
		String text = null;
		LinkedList<Double> avg = new LinkedList<Double>(), 
				dvst = new LinkedList<Double>();
		double[] darr = new double[10];
		double davg = -1, ddvst = -1;
		int ind = 0, x = 0;
		String commands = "set output '/home/iammyr/experiment/ld4s-response_time_errorplot.eps'\n"
				+"set terminal postscript eps 30\n"
				+"set xlabel 'x'\n"
				+"set ylabel 'y'\n"
				+"plot '-' notitle with lines, \\"
				+"\n'-' notitle with error\n\n",
				toRepeat = "";

		try {
			reader = new BufferedReader(new FileReader(file));

			while ((text = reader.readLine()) != null) {
				try{
					darr[ind] = Double.valueOf(text);
					ind++;
				}catch(NumberFormatException nf){
					System.err.println("The following text is not a double: "+text);
				}
				if (ind == darr.length){
					ind = 0;
					davg = findMean(darr);
					avg.add(Double.valueOf(davg));
					ddvst = findStandardDeviation(darr,  davg);
					dvst.add(Double.valueOf(ddvst));
				}
				//				contents.append(text)
				//				.append(System.getProperty(
				//						"line.separator"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Iterator<Double> itavg = avg.iterator(), itdvst = dvst.iterator();

		BufferedWriter out = null;
		try{
			// Create file 
			FileWriter fstream = new FileWriter("/home/iammyr/experiment/ld4s_response_time.plt");
			out = new BufferedWriter(fstream);
			out.write(commands);
			while (itavg.hasNext()){
				davg = itavg.next().doubleValue();
				ddvst = itdvst.next().doubleValue();
				x += 10;
				toRepeat += "\n"+x+" "+davg+" "+ddvst;
				out.write("\n"+x+" "+davg+" "+ddvst);
			}
			out.write("\ne\n\n"+toRepeat+"\ne\n\n");
			
		}catch (Exception e){
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
		finally{
			if (out != null){
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
