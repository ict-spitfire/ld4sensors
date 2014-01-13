import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LogsHandler {

	private static final String path = "/home/iammyr/github/test2/ld4sensors/ld4s.log";

	public static void main(String[] args){
		File file = new File(path);
		StringBuilder contents = new StringBuilder();
		BufferedReader reader = null;
		String text = null;

		try {
			reader = new BufferedReader(new FileReader(file));
			// repeat until all lines is read
			while ((text = reader.readLine()) != null) {
				contents.append(text)
				.append(System.getProperty(
						"line.separator"));
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

		text = contents.toString();

		Pattern pattern = 
				Pattern.compile("INFO:\\s\\d{4}-\\d{2}-\\d{2}"
						+"\\s\\d{2}:\\d{2}:\\d{2}\\s\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}" 
						+".*\\d{4}\\s\\w{3,6}\\s(/\\w+){3,}"), 
						pdate = Pattern.compile("\\d{4}-\\d{2}-\\d{2}"),
						ptime = Pattern.compile("\\d{2}:\\d{2}:\\d{2}"),
						pip = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"),
						preq = Pattern.compile("\\w{3,6}\\s(/\\w+){3,}");

		Matcher matcher = 
				pattern.matcher(text), mdate = null, mtime = null, mip = null, mreq = null;
		int endDate = -1, endIp = -1, endRequest = -1, startDate = -1, startIp = -1, startRequest = -1, matcherSize = 0,
				dates = -1;
		LinkedList<String> date = new LinkedList<String>(), ip = new LinkedList<String>(), request = new LinkedList<String>(), 
				time = new LinkedList<String>();
		String current = null;

		while (matcher.find()) {	            
			matcherSize++;
			current = matcher.group();				
			System.out.println("current: "+current);
			//date
			mdate = pdate.matcher(current);
			while (mdate.find()) {
				date.add(mdate.group());
			}
			//time
			mtime = ptime.matcher(current);
			while (mtime.find()) {
				time.add(mtime.group());
			} 
			//ip
			mip = pip.matcher(current);
			while (mip.find()) {
				ip.add(mip.group());
			} 
			//request
			mreq = preq.matcher(current);
			while (mreq.find()) {
				request.add(mreq.group());
			} 
		}
		BufferedWriter out = null;
		try{
			// Create file 
			FileWriter fstream = new FileWriter("/home/iammyr/github/test2/ld4sensors/ld4s_log.out");
			out = new BufferedWriter(fstream);
			for (int ind = 0; ind < date.size(); ind++){
				out.write("\n"+date.get(ind)+" "+time.get(ind)+"\t"+ip.get(ind)+"\t"+request.get(ind));	
			}
			
		}catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
		finally{
			if (out != null){
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println("amount of accesses "+matcherSize);

	}
}
