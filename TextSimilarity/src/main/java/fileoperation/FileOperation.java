package fileoperation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

public class FileOperation {
	
	public void writeMatrix(String language1, String language2, double[][] matrix, String file) {
		String matrixString = language1 + "." + language2;
		for (int i = 0; i < matrix[0].length; i ++) {
			matrixString = matrixString + ","	+ (i + 1);
		}
		matrixString = matrixString + "\n";
		
		
		for (int i = 0; i < matrix.length; i ++) {
			matrixString = matrixString + (i + 1);
			for (int j = 0; j < matrix[0].length; j ++) {
				matrixString = matrixString + "," + matrix[i][j];
			}
			matrixString = matrixString + "\n";
		}
		try {
			writeCSV(matrixString, file);
		} catch (Exception e) {
			
		}
	}
	
	public void copyFile(String input, String output) {
		InputStream inStream = null;
		OutputStream outStream = null;
		try {
			File ifile = new File(input);
			File ofile = new File(output);

			inStream = new FileInputStream(ifile);
			outStream = new FileOutputStream(ofile);

			byte[] buffer = new byte[1024];

			int length;
			// copy the file content in bytes
			while ((length = inStream.read(buffer)) > 0) {
				outStream.write(buffer, 0, length);
			}

			inStream.close();
			outStream.close();

			System.out.println("File " + input + " is copied successful!");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void createFolder(String path) {
		File files = new File(path);
        if (!files.exists()) {
            if (files.mkdirs()) {
                System.out.println("Multiple directories are created!");
            } else {
                System.out.println("Failed to create multiple directories!");
            }
        }
	}
	
	public void writeCSV(String csvString, String file) throws FileNotFoundException {
		PrintWriter pw = new PrintWriter(new File(file));
		pw.write(csvString);
        pw.close();
	}
	
	public ArrayList<String> listFolder(String folder) {
		ArrayList<String> folders = new ArrayList<String>();
		for (File f : new File(folder).listFiles()) {
			if (f.isDirectory()) {
				folders.add(f.getName());
			}
		}
		return folders;
	}

	public ArrayList<String> listFiles(String folder) {
		ArrayList<String> files = new ArrayList<String>();
		for (File f : new File(folder).listFiles()) {
			if (f.isFile()) {
				files.add(f.getName());
			}
		}
		return files;
	}

	public String readFile(String file) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(file));
		String everything = null;
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			everything = sb.toString();
		} finally {
			br.close();
		}
		return everything;
	}

	public void writeFile(String file, String text) {
		try {
			PrintWriter writer = new PrintWriter(file, "UTF-8");
			writer.print(text);
			writer.close();
		} catch (Exception e) {

		}
	}

}
