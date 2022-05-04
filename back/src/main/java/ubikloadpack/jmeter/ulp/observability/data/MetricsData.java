package ubikloadpack.jmeter.ulp.observability.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsData {
	
	private BufferedWriter bw;
	private BufferedReader br;
	private boolean isOpen = false;
	
	private static final Logger log = LoggerFactory.getLogger(MetricsData.class);
	
	public MetricsData(String path) {
		File dataFile = new File(path);
		String dataName = dataFile.getName();
		try(FileWriter fw = new FileWriter(dataFile,false)) {
			StringJoiner data = 
					new StringJoiner(";")
					.add(dataName+"_response_created")
					.add(dataName+"_response")
					.add(dataName+"_total")
					.add(dataName+"_error")
					.add(dataName+"_response_avg")
					.add(dataName+"_response_max")
					.add(dataName+"_response_pct1")
					.add(dataName+"_response_pct2")
					.add(dataName+"_response_pct3");
			fw.write(data.toString());
		} catch (IOException e) { }
		
		try {
			this.bw = new BufferedWriter(new FileWriter(dataFile,true));
			this.br = new BufferedReader(new FileReader(dataFile));
			this.isOpen = true;
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		
	}
	
	public String read() {
		if(isOpen) {
			StringBuilder sBuilder = new StringBuilder();	
			
		}
		return this.isOpen ? this.br.lines().collect(Collectors.joining(System.lineSeparator())) : "";
	}
	
	
	public void write(
			long timestamp,
			double current,
			double nRequests,
			double nErrors,
			double rAvg,
			double rMax,
			double rPct1,
			double rPct2,
			double rPct3
			) {	
		if(this.isOpen) {
			StringJoiner data = 
					new StringJoiner(";")
					.add(String.valueOf(timestamp))
					.add(String.valueOf(current))
					.add(String.valueOf(nRequests))
					.add(String.valueOf(nErrors))
					.add(String.valueOf(rAvg))
					.add(String.valueOf(rMax))
					.add(String.valueOf(rPct1))
					.add(String.valueOf(rPct2))
					.add(String.valueOf(rPct3));

			try {
				this.bw.newLine();
				this.bw.write(data.toString());
				this.bw.flush();
			} catch (IOException e) {
				log.error(e.getMessage());
			}
		}
		
	}
	
	public void close() {
		if(this.isOpen) {
			if(this.bw != null) {	
				try {
					this.bw.flush();
					this.bw.close();
				} catch (IOException e) { }
			}
			
			if(this.br != null) {
				try {
					this.br.close();
				} catch (IOException e) { }
			}
			
			this.isOpen = false;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		this.close();
		super.finalize();
	}

	

}
