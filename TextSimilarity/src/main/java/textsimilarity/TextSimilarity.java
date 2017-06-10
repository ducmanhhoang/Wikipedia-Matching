package textsimilarity;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import constant.Constant;
import dkpro.similarity.algorithms.api.TextSimilarityMeasure;

import dkpro.similarity.algorithms.lexical.ngrams.WordNGramJaccardMeasure;
import dkpro.similarity.algorithms.lexical.string.CosineSimilarity;
import fileoperation.FileOperation;

public class TextSimilarity {
	FileOperation fileOperation = null;
	EnglishPreprocessing englishPreprocessing = null;
	TextSimilarityMeasure wordNGramJaccardMeasure = null;
	TextSimilarityMeasure cosineSimilarity = null;
	String jaccardEvaluation = "";
	String cosineEvaluation = "";
	String jaccardHTML = "";
	String[] LPP = new String[3];

	public TextSimilarity() {
		fileOperation = new FileOperation();
		englishPreprocessing = new EnglishPreprocessing();
		wordNGramJaccardMeasure = new WordNGramJaccardMeasure(1);
		cosineSimilarity = new CosineSimilarity();
	}

	public void computeTextSimilarityScore(String author, String lang) {
		System.out.println("Starting author: " + author + "...");
		try {
			// creating string for printing the evaluation tables.
			jaccardEvaluation = "Topic, Languages, Max value, Length of L1, Length of L2, %L1, %L2, Diagonal>=2, Diagonal >=3, Diagonal>=4, Diagonal>=5\n";
			cosineEvaluation = "Topic, Languages, Max value, Length of L1, Length of L2, %L1, %L2, Diagonal>=2, Diagonal >=3, Diagonal>=4, Diagonal>=5\n";
			// creating string for printing the index.html page
			jaccardHTML = "<html><body style=\"background-color: AliceBlue\"><b><h3><center><u>Author: " + author + ", " + lang + "</u></center></h3></b><strong><h4>Annotation Table</h4></strong><table style=\"width: 100%\" border=\"1\"><tr><th>Topic</th><th>Main Article</th><th>Reference Articles</th><th>Percentage</th><th>Prediction</th></tr>";
			// scanning topics in a author's folder.
			ArrayList<String> topicFolders = fileOperation.listFolder(Constant.TRANSLATION_DIRECTORY + "/" + author + "." + lang);
			for (String topicFolder : topicFolders) {
				System.out.println("Starting topic: " + topicFolder + "...");
				// scanning files in an author's topic.
				ArrayList<String> files = fileOperation.listFiles(Constant.TRANSLATION_DIRECTORY + "/" + author + "." + lang + "/" + topicFolder);
				// supporting for storing preprocessed texts to compute text similarity.
				ArrayList<ArrayList<String[]>> preprocessedContents = new ArrayList<ArrayList<String[]>>();
				// supporting for storing non-preprocessed texts to generate HTML pages.
				ArrayList<ArrayList<String>> nonPreprocessedContents = new ArrayList<ArrayList<String>>();
				// aiming to identify main article for making pairs supporting for comparison.
				int mainArticle = 0;
				// reading and preprocessing texts.
				for (int i = 0; i < files.size(); i++) {
					System.out.println("Reading content of \"" + files.get(i) + "\" file...");
					englishPreprocessing.preprocess((fileOperation.readFile(Constant.TRANSLATION_DIRECTORY + "/" + author + "." + lang + "/" + topicFolder + "/" + files.get(i))));
					preprocessedContents.add(englishPreprocessing.getPreprocessedContent());
					nonPreprocessedContents.add(englishPreprocessing.getNonPreprocessedContent());
					if (files.get(i).equalsIgnoreCase(lang)) {
						mainArticle = i;
					}
					System.out.println("Finished reading content of \"" + files.get(i) + "\" file");
				}
				System.out.println();
				// instantiating a array for storing pairs.
				ArrayList<Pair> pairs = new ArrayList<Pair>();
				// creating the output folder supporting for storing resulting matrices, HTML files, and evaluation table.
				fileOperation.createFolder(Constant.OUTPUT_DIRECTORY + "/" + author + "." + lang + "/" + topicFolder);
				
				
				jaccardHTML = jaccardHTML + "<tr><td>" + topicFolder + "</td><td><a href=\"" + topicFolder + "/" + lang + ".html\">" + lang + "</a></td>";
				
				LPP[0] = "";
				LPP[1] = "";
				LPP[2] = "";
				
				LPP[0] = LPP[0] + "<td>";
				LPP[1] = LPP[1] + "<td>";
				LPP[2] = LPP[2] + "<td>";
				// computing text similarity and post-processing for each pair created by associating main article with others.
				for (int i = 0; i < files.size(); i++) {
					if(mainArticle != i) {
						System.out.println("Computing text similarity between " + files.get(mainArticle) + "." + files.get(i) + "...");
						// getting the size of the matrices.
						int rowLength = preprocessedContents.get(mainArticle).size();
						int columnLength = preprocessedContents.get(i).size();
						// initialize two matrices which store Word N-Gram Jaccard measure and Cosine similarity.
						double[][] mj = new double[rowLength][columnLength];
						double[][] mc = new double[rowLength][columnLength];
						// computing text similarity for each sentence pair existing in the two documents and storing them into the above matrices.
						for (int s1 = 0; s1 < rowLength; s1++) {
							for (int s2 = 0; s2 < columnLength; s2++) {
								double wordNGramJaccardMeasureScore = wordNGramJaccardMeasure.getSimilarity(preprocessedContents.get(mainArticle).get(s1), preprocessedContents.get(i).get(s2));
								double cosineSimilarityScore = cosineSimilarity.getSimilarity(preprocessedContents.get(mainArticle).get(s1), preprocessedContents.get(i).get(s2));
								mj[s1][s2] = wordNGramJaccardMeasureScore;
								mc[s1][s2] = cosineSimilarityScore;
							}
						}
						System.out.println("Finished computing text similarity between " + files.get(mainArticle) + "." + files.get(i) + "...");
						System.out.println();
						System.out.println("Looking for the diagonals in the matrices of " + files.get(mainArticle) + "." + files.get(i) + "...");
						// looking for the diagonals using standard deviation.
						/*
						double mjStandardDeviation = getStandardDeviationOfMatrix(mj);
						double mcStandardDeviation = getStandardDeviationOfMatrix(mc);
						double mjThreshold = mjStandardDeviation * 3;
						double mcThreshold = mcStandardDeviation * 3;
						double[][] sdtmj = sweepMatrix(mj, mjThreshold);
						double[][] sdtmc = sweepMatrix(mc, mcThreshold);
						ArrayList<ArrayList<Point>> dsdtmj = findChains(sdtmj);
						ArrayList<ArrayList<Point>> dsdtmc = findChains(sdtmc);
						*/
						// looking for the diagonals using highest value.
						double[][] hvtmj = sweepMatrix(mj);
						double[][] hvtmc = sweepMatrix(mc);
						ArrayList<ArrayList<Point>> dhvtmj = findChains(hvtmj);
						ArrayList<ArrayList<Point>> dhvtmc = findChains(hvtmc);
						System.out.println("Finished looking for the diagonals in the matrices of " + files.get(mainArticle) + "." + files.get(i) + "...");
						System.out.println();
						// writing for diagonals matrices.
						fileOperation.createFolder(Constant.OUTPUT_DIRECTORY + "/" + author + "." + lang + "/" + topicFolder + "/" + files.get(mainArticle) + "." + files.get(i));
						writeDiagonalMatrix(files.get(mainArticle), files.get(i), rowLength, columnLength, dhvtmj, Constant.OUTPUT_DIRECTORY + "/" + author + "." + lang + "/" + topicFolder + "/" + files.get(mainArticle) + "." + files.get(i) + "/dhvtmj.csv" /*"/dsdtmj.csv"*/);
						writeDiagonalMatrix(files.get(mainArticle), files.get(i), rowLength, columnLength, dhvtmc, Constant.OUTPUT_DIRECTORY + "/" + author + "." + lang + "/" + topicFolder + "/" + files.get(mainArticle) + "." + files.get(i) + "/dhvtmc.csv" /*"/dsdtmc.csv"*/);
						// writing for evaluation.
						Pair pair = writeEvaluation(topicFolder, files.get(mainArticle), files.get(i), dhvtmj, dhvtmc, rowLength, columnLength);
						// adding content of the pair.
						pair.setContent1(nonPreprocessedContents.get(mainArticle));
						pair.setContent2(nonPreprocessedContents.get(i));
						// adding diagonals
						pair.setDhvtmj(dhvtmj);
						// adding pair in the array.
						pairs.add(pair);
					}
				}
				LPP[0] = LPP[0] + "</td>";
				LPP[1] = LPP[1] + "</td>";
				LPP[2] = LPP[2] + "</td>";
				jaccardHTML = jaccardHTML + LPP[0];
				jaccardHTML = jaccardHTML + LPP[1];
				jaccardHTML = jaccardHTML + LPP[2];
				
				jaccardHTML = jaccardHTML + "</tr>";
				
				printHTMLPages(pairs, author, files.get(mainArticle), topicFolder);
				System.out.println("Finished topic: " + topicFolder + "...");
				System.out.println();
			}
			jaccardHTML = jaccardHTML + "</table></body></html>";
			fileOperation.writeCSV(cosineEvaluation, Constant.OUTPUT_DIRECTORY + "/" + author + "." + lang + "/cosine-evaluation.csv");
			fileOperation.writeCSV(jaccardEvaluation, Constant.OUTPUT_DIRECTORY + "/" + author + "." + lang + "/jaccard-evaluation.csv");
			fileOperation.writeFile(Constant.OUTPUT_DIRECTORY + "/" + author + "." + lang + "/" + "index.html", jaccardHTML);
		} catch (Exception e) {
			System.out.println(e);
		} // end try catch
		System.out.println("Finished author: " + author + ".");
		System.out.println();
	} // end function
	
	public void printHTMLPages(ArrayList<Pair> pairs, String author, String language, String topicFolder) {
		String[] colors = {"LightGrey", "LightGreen", "LightPink", "LightSalmon", "LightSeaGreen", "LightSkyBlue", "LightYellow", "AliceBlue"};
		String[] note = new String[8];
		
		int[][] count = new int[pairs.get(0).getLength1()][3];
		for (int i = 0; i < pairs.size(); i ++) {
			for (int j = 0; j < pairs.get(i).getLength1(); j ++) {
				if (pairs.get(i).getSimilars1().contains(j)) {
					count[j][i] = 1;
				}
			}
			
		}
		
		
		for (int i = 0; i < count.length; i ++) {
			if ((count[i][0] == 1) && (count[i][1] != 1) && (count[i][2] != 1)) {
				pairs.get(0).getContent1().set(i, "<tr><td>" + i + "</td><td style=\"background-color: " + colors[0] + "\">" + pairs.get(0).getContent1().get(i) + "</td></tr>");
				for (int j = 0; j < pairs.get(0).getDhvtmj().size(); j ++) {
					for (int d = 0; d < pairs.get(0).getDhvtmj().get(j).size();  d ++) {
						if (pairs.get(0).getDhvtmj().get(j).get(d).getRow() == i) {
							pairs.get(0).getContent2().set(pairs.get(0).getDhvtmj().get(j).get(d).getColumn(), "<tr><td>" + pairs.get(0).getDhvtmj().get(j).get(d).getColumn() + "</td><td style=\"background-color: " + colors[0] + "\">" + pairs.get(0).getContent2().get(pairs.get(0).getDhvtmj().get(j).get(d).getColumn()) + "</td></tr>");
						}
					}
				}
				note[0] = pairs.get(0).getName2();
			}
			if ((count[i][0] != 1) && (count[i][1] == 1) && (count[i][2] != 1)) {
				pairs.get(0).getContent1().set(i, "<tr><td>" + i + "</td><td style=\"background-color: " + colors[1] + "\">" + pairs.get(0).getContent1().get(i) + "</td></tr>");
				for (int j = 0; j < pairs.get(1).getDhvtmj().size(); j ++) {
					for (int d = 0; d < pairs.get(1).getDhvtmj().get(j).size();  d ++) {
						if (pairs.get(1).getDhvtmj().get(j).get(d).getRow() == i) {
							pairs.get(1).getContent2().set(pairs.get(1).getDhvtmj().get(j).get(d).getColumn(), "<tr><td>" + pairs.get(1).getDhvtmj().get(j).get(d).getColumn() + "</td><td style=\"background-color: " + colors[1] + "\">" + pairs.get(1).getContent2().get(pairs.get(1).getDhvtmj().get(j).get(d).getColumn()) + "</td></tr>");
						}
					}
				}
				note[1] = pairs.get(1).getName2();
			}
			if ((count[i][0] != 1) && (count[i][1] != 1) && (count[i][2] == 1)) {
				pairs.get(0).getContent1().set(i, "<tr><td>" + i + "</td><td style=\"background-color: " + colors[2] + "\">" + pairs.get(0).getContent1().get(i) + "</td></tr>");
				for (int j = 0; j < pairs.get(2).getDhvtmj().size(); j ++) {
					for (int d = 0; d < pairs.get(2).getDhvtmj().get(j).size();  d ++) {
						if (pairs.get(2).getDhvtmj().get(j).get(d).getRow() == i) {
							pairs.get(2).getContent2().set(pairs.get(2).getDhvtmj().get(j).get(d).getColumn(), "<tr><td>" + pairs.get(2).getDhvtmj().get(j).get(d).getColumn() + "</td><td style=\"background-color: " + colors[2] + "\">" + pairs.get(2).getContent2().get(pairs.get(2).getDhvtmj().get(j).get(d).getColumn()) + "</td></tr>");
						}
					}
				}
				note[2] = pairs.get(2).getName2();
			}
			
			
			if ((count[i][0] == 1) && (count[i][1] == 1) && (count[i][2] != 1)) {
				pairs.get(0).getContent1().set(i, "<tr><td>" + i + "</td><td style=\"background-color: " + colors[3] + "\">" + pairs.get(0).getContent1().get(i) + "</td></tr>");
				for (int j = 0; j < pairs.get(0).getDhvtmj().size(); j ++) {
					for (int d = 0; d < pairs.get(0).getDhvtmj().get(j).size();  d ++) {
						if (pairs.get(0).getDhvtmj().get(j).get(d).getRow() == i) {
							pairs.get(0).getContent2().set(pairs.get(0).getDhvtmj().get(j).get(d).getColumn(), "<tr><td>" + pairs.get(0).getDhvtmj().get(j).get(d).getColumn() + "</td><td style=\"background-color: " + colors[3] + "\">" + pairs.get(0).getContent2().get(pairs.get(0).getDhvtmj().get(j).get(d).getColumn()) + "</td></tr>");
						}
					}
				}
				for (int j = 0; j < pairs.get(1).getDhvtmj().size(); j ++) {
					for (int d = 0; d < pairs.get(1).getDhvtmj().get(j).size();  d ++) {
						if (pairs.get(1).getDhvtmj().get(j).get(d).getRow() == i) {
							pairs.get(1).getContent2().set(pairs.get(1).getDhvtmj().get(j).get(d).getColumn(), "<tr><td>" + pairs.get(1).getDhvtmj().get(j).get(d).getColumn() + "</td><td style=\"background-color: " + colors[3] + "\">" + pairs.get(1).getContent2().get(pairs.get(1).getDhvtmj().get(j).get(d).getColumn()) + "</td></tr>");
						}
					}
				}
				note[3] = pairs.get(0).getName2() + "-" + pairs.get(1).getName2();
			}
			if ((count[i][0] == 1) && (count[i][1] != 1) && (count[i][2] == 1)) {
				pairs.get(0).getContent1().set(i, "<tr><td>" + i + "</td><td style=\"background-color: " + colors[4] + "\">" + pairs.get(0).getContent1().get(i) + "</td></tr>");
				for (int j = 0; j < pairs.get(0).getDhvtmj().size(); j ++) {
					for (int d = 0; d < pairs.get(0).getDhvtmj().get(j).size();  d ++) {
						if (pairs.get(0).getDhvtmj().get(j).get(d).getRow() == i) {
							pairs.get(0).getContent2().set(pairs.get(0).getDhvtmj().get(j).get(d).getColumn(), "<tr><td>" + pairs.get(0).getDhvtmj().get(j).get(d).getColumn() + "</td><td style=\"background-color: " + colors[4] + "\">" + pairs.get(0).getContent2().get(pairs.get(0).getDhvtmj().get(j).get(d).getColumn()) + "</td></tr>");
						}
					}
				}
				for (int j = 0; j < pairs.get(2).getDhvtmj().size(); j ++) {
					for (int d = 0; d < pairs.get(2).getDhvtmj().get(j).size();  d ++) {
						if (pairs.get(2).getDhvtmj().get(j).get(d).getRow() == i) {
							pairs.get(2).getContent2().set(pairs.get(2).getDhvtmj().get(j).get(d).getColumn(), "<tr><td>" + pairs.get(2).getDhvtmj().get(j).get(d).getColumn() + "</td><td style=\"background-color: " + colors[4] + "\">" + pairs.get(2).getContent2().get(pairs.get(2).getDhvtmj().get(j).get(d).getColumn()) + "</td></tr>");
						}
					}
				}
				note[4] = pairs.get(0).getName2() + "-" + pairs.get(2).getName2();
			}
			if ((count[i][0] != 1) && (count[i][1] == 1) && (count[i][2] == 1)) {
				pairs.get(0).getContent1().set(i, "<tr><td>" + i + "</td><td style=\"background-color: " + colors[5] + "\">" + pairs.get(0).getContent1().get(i) + "</td></tr>");
				for (int j = 0; j < pairs.get(1).getDhvtmj().size(); j ++) {
					for (int d = 0; d < pairs.get(1).getDhvtmj().get(j).size();  d ++) {
						if (pairs.get(1).getDhvtmj().get(j).get(d).getRow() == i) {
							pairs.get(1).getContent2().set(pairs.get(1).getDhvtmj().get(j).get(d).getColumn(), "<tr><td>" + pairs.get(1).getDhvtmj().get(j).get(d).getColumn() + "</td><td style=\"background-color: " + colors[5] + "\">" + pairs.get(1).getContent2().get(pairs.get(1).getDhvtmj().get(j).get(d).getColumn()) + "</td></tr>");
						}
					}
				}
				for (int j = 0; j < pairs.get(2).getDhvtmj().size(); j ++) {
					for (int d = 0; d < pairs.get(2).getDhvtmj().get(j).size();  d ++) {
						if (pairs.get(2).getDhvtmj().get(j).get(d).getRow() == i) {
							pairs.get(2).getContent2().set(pairs.get(2).getDhvtmj().get(j).get(d).getColumn(), "<tr><td>" + pairs.get(2).getDhvtmj().get(j).get(d).getColumn() + "</td><td style=\"background-color: " + colors[5] + "\">" + pairs.get(2).getContent2().get(pairs.get(2).getDhvtmj().get(j).get(d).getColumn()) + "</td></tr>");
						}
					}
				}
				note[5] = pairs.get(1).getName2() + "-" + pairs.get(2).getName2();
			}
			
			
			if ((count[i][0] == 1) && (count[i][1] == 1) && (count[i][2] == 1)) {
				pairs.get(0).getContent1().set(i, "<tr><td>" + i + "</td><td style=\"background-color: " + colors[6] + "\">" + pairs.get(0).getContent1().get(i) + "</td></tr>");
				for (int j = 0; j < pairs.get(0).getDhvtmj().size(); j ++) {
					for (int d = 0; d < pairs.get(0).getDhvtmj().get(j).size();  d ++) {
						if (pairs.get(0).getDhvtmj().get(j).get(d).getRow() == i) {
							pairs.get(0).getContent2().set(pairs.get(0).getDhvtmj().get(j).get(d).getColumn(), "<tr><td>" + pairs.get(0).getDhvtmj().get(j).get(d).getColumn() + "</td><td style=\"background-color: " + colors[6] + "\">" + pairs.get(0).getContent2().get(pairs.get(0).getDhvtmj().get(j).get(d).getColumn()) + "</td></tr>");
						}
					}
				}
				for (int j = 0; j < pairs.get(1).getDhvtmj().size(); j ++) {
					for (int d = 0; d < pairs.get(1).getDhvtmj().get(j).size();  d ++) {
						if (pairs.get(1).getDhvtmj().get(j).get(d).getRow() == i) {
							pairs.get(1).getContent2().set(pairs.get(1).getDhvtmj().get(j).get(d).getColumn(), "<tr><td>" + pairs.get(1).getDhvtmj().get(j).get(d).getColumn() + "</td><td style=\"background-color: " + colors[6] + "\">" + pairs.get(1).getContent2().get(pairs.get(1).getDhvtmj().get(j).get(d).getColumn()) + "</td></tr>");
						}
					}
				}
				for (int j = 0; j < pairs.get(2).getDhvtmj().size(); j ++) {
					for (int d = 0; d < pairs.get(2).getDhvtmj().get(j).size();  d ++) {
						if (pairs.get(2).getDhvtmj().get(j).get(d).getRow() == i) {
							pairs.get(2).getContent2().set(pairs.get(2).getDhvtmj().get(j).get(d).getColumn(), "<tr><td>" + pairs.get(2).getDhvtmj().get(j).get(d).getColumn() + "</td><td style=\"background-color: " + colors[6] + "\">" + pairs.get(2).getContent2().get(pairs.get(2).getDhvtmj().get(j).get(d).getColumn()) + "</td></tr>");
						}
					}
				}
				note[6] = pairs.get(0).getName2() + "-" + pairs.get(1).getName2() + "-" + pairs.get(2).getName2();
			}
		}
		note[7] = "Non-similar";
		
		//String mainHTML = "<html><body style=\"background-color: AliceBlue\"><b><h3><center><u>Author: " + author + ". " + language + "</u></center></h3></b><strong><h4>Article: " + topicFolder + ", " + language + "</h4></strong><table style=\"width: 100%\" border=\"1\"><tr><th>No.</th><th>Content</th></tr>";
		String mainHTML = "<html><body style=\"background-color: AliceBlue\"><b><h3><center><u>Author: " + author + ". " + language + "</u></center></h3></b>"
				+ "<table style=\"width: 100%\" border=\"0\"><tr><th align=\"right\"><strong><h4>Article: " + topicFolder + ", " + language + "</h4></strong></th>"
						+ "<th align=\"right\"><strong><h4>Note:</h4></strong><br>"
						+ ((note[0] != null)? note[0] + ":<span style=\"background-color: " + colors[0] + "\">" + colors[0] + "</span><br>" : "")
						+ ((note[1] != null)? note[1] + ":<span style=\"background-color: " + colors[1] + "\">" + colors[1] + "</span><br>" : "")
						+ ((note[2] != null)? note[2] + ":<span style=\"background-color: " + colors[2] + "\">" + colors[2] + "</span><br>" : "")
						+ ((note[3] != null)? note[3] + ":<span style=\"background-color: " + colors[3] + "\">" + colors[3] + "</span><br>" : "")
						+ ((note[4] != null)? note[4] + ":<span style=\"background-color: " + colors[4] + "\">" + colors[4] + "</span><br>" : "")
						+ ((note[5] != null)? note[5] + ":<span style=\"background-color: " + colors[5] + "\">" + colors[5] + "</span><br>" : "")
						+ ((note[6] != null)? note[6] + ":<span style=\"background-color: " + colors[6] + "\">" + colors[6] + "</span><br>" : "")
						+ ((note[7] != null)? note[7] + ":<span style=\"background-color: " + colors[7] + "\">" + colors[7] + "</span><br>" : "")
						+ "</th></tr></table>"
						+ "<table style=\"width: 100%\" border=\"1\"><tr><th>No.</th><th>Content</th></tr>";
		for (int i = 0; i < pairs.get(0).getLength1(); i ++) {
			if (pairs.get(0).getContent1().get(i).contains("<tr><td>")) {
				mainHTML = mainHTML + pairs.get(0).getContent1().get(i);
			} else {
				mainHTML = mainHTML + "<tr><td>" + i + "</td><td style=\"background-color: " + colors[7] + "\">" + pairs.get(0).getContent1().get(i) + "</td></tr>";
			}
		}
		mainHTML = mainHTML + "</table></body></html>";
		fileOperation.writeFile(Constant.OUTPUT_DIRECTORY + "/" + author + "." + language + "/" + topicFolder + "/" + language + ".html", mainHTML);
		
		
		String[] htmls = new String[3];
		for (int i = 0; i < pairs.size(); i ++) {
			htmls[i] = "<html><body style=\"background-color: AliceBlue\"><b><h3><center><u>Author: " + author + ". " + language + "</u></center></h3></b>"
			+ "<table style=\"width: 100%\" border=\"0\"><tr><th align=\"right\"><strong><h4>Article: " + topicFolder + ", " + pairs.get(i).getName2() + "</h4></strong></th>"
					+ "<th align=\"right\"><strong><h4>Note:</h4></strong><br>"
					+ ((note[0] != null)? note[0] + ":<span style=\"background-color: " + colors[0] + "\">" + colors[0] + "</span><br>" : "")
					+ ((note[1] != null)? note[1] + ":<span style=\"background-color: " + colors[1] + "\">" + colors[1] + "</span><br>" : "")
					+ ((note[2] != null)? note[2] + ":<span style=\"background-color: " + colors[2] + "\">" + colors[2] + "</span><br>" : "")
					+ ((note[3] != null)? note[3] + ":<span style=\"background-color: " + colors[3] + "\">" + colors[3] + "</span><br>" : "")
					+ ((note[4] != null)? note[4] + ":<span style=\"background-color: " + colors[4] + "\">" + colors[4] + "</span><br>" : "")
					+ ((note[5] != null)? note[5] + ":<span style=\"background-color: " + colors[5] + "\">" + colors[5] + "</span><br>" : "")
					+ ((note[6] != null)? note[6] + ":<span style=\"background-color: " + colors[6] + "\">" + colors[6] + "</span><br>" : "")
					+ ((note[7] != null)? note[7] + ":<span style=\"background-color: " + colors[7] + "\">" + colors[7] + "</span><br>" : "")
					+ "</th></tr></table>"
					+ "<table style=\"width: 100%\" border=\"1\"><tr><th>No.</th><th>Content</th></tr>";
			for (int j = 0; j < pairs.get(i).getLength2(); j ++) {
				if (pairs.get(i).getContent2().get(j).contains("<tr><td>")) {
					htmls[i] = htmls[i] + pairs.get(i).getContent2().get(j);
				} else {
					htmls[i] = htmls[i] + "<tr><td>" + j + "</td><td style=\"background-color: " + colors[7] + "\">" + pairs.get(i).getContent2().get(j) + "</td></tr>";
				}
			}
			htmls[i] = htmls[i] + "</table></body></html>";
			fileOperation.writeFile(Constant.OUTPUT_DIRECTORY + "/" + author + "." + language + "/" + topicFolder + "/" + pairs.get(i).getName2() + ".html", htmls[i]);
		}
	}
	
	public Pair writeEvaluation(String topic, String language1, String language2, ArrayList<ArrayList<Point>> wordNGramJaccardMeasureScoresChains, ArrayList<ArrayList<Point>> cosineSimilarityScoresChains, int rowLength, int columnLength) throws FileNotFoundException {
		double maxJaccard = 0;
		double minJaccard = 1;
		double averageJaccard = 0;
		int d = 0;
		List<Integer> rows = new ArrayList<Integer>();
		List<Integer> columns = new ArrayList<Integer>();
		int jaccardMaxDiagonal = 0;
		for (ArrayList<Point> wordNGramJaccardMeasureScoresChain : wordNGramJaccardMeasureScoresChains) {
			if ((wordNGramJaccardMeasureScoresChain.size() >= 2) && (isExistingDiagonalNeighbor(wordNGramJaccardMeasureScoresChain))) {
				for (Point point : wordNGramJaccardMeasureScoresChain) {
					averageJaccard = averageJaccard + point.getValue();
					if (maxJaccard < point.getValue()) {
						maxJaccard = point.getValue();
					}
					if (minJaccard > point.getValue()) {
						minJaccard = point.getValue();
					}
					rows.add(point.getRow());
					columns.add(point.getColumn());
					d = d + 1;
				}
				if (jaccardMaxDiagonal < findDiagonalSize(wordNGramJaccardMeasureScoresChain)) {
					jaccardMaxDiagonal = findDiagonalSize(wordNGramJaccardMeasureScoresChain);
				}
			}
		}
		if (d == 0) {
			averageJaccard = 0;
		} else {
			averageJaccard = (double)averageJaccard/d;
		}
		Set<Integer> hashsetRows = new HashSet<Integer>(rows);
		Set<Integer> hashsetColumns = new HashSet<Integer>(columns);
		double jaccardL1Percent = ((double)hashsetRows.size()/rowLength) * 100;
		double jaccardL2Percent = ((double)hashsetColumns.size()/columnLength) * 100;
		
		Pair pair = new Pair(language1, rowLength, hashsetRows, jaccardL1Percent, language2, columnLength, hashsetColumns, jaccardL2Percent);
		
		double maxCosine = 0;
		double minCosine = 1;
		double averageCosine = 0;
		d = 0;
		rows = new ArrayList<Integer>();
		columns = new ArrayList<Integer>();
		int cosineMaxDiagonal = 0;
		for (ArrayList<Point> cosineSimilarityScoresChain : cosineSimilarityScoresChains) {
			if ((cosineSimilarityScoresChain.size() >= 2) && (isExistingDiagonalNeighbor(cosineSimilarityScoresChain))) {
				for (Point point : cosineSimilarityScoresChain) {
					averageCosine = averageCosine + point.getValue();
					if (maxCosine < point.getValue()) {
						maxCosine = point.getValue();
					}
					if (minCosine > point.getValue()) {
						minCosine = point.getValue();
					}
					rows.add(point.getRow());
					columns.add(point.getColumn());
					d = d + 1;
				}
				if (cosineMaxDiagonal < findDiagonalSize(cosineSimilarityScoresChain)) {
					cosineMaxDiagonal = findDiagonalSize(cosineSimilarityScoresChain);
				}
			}
		}
		if (d == 0) {
			averageCosine = 0;
		} else {
			averageCosine = (double)averageCosine/d;
		}
		hashsetRows = new HashSet<Integer>(rows);
		hashsetColumns = new HashSet<Integer>(columns);
		double cosineL1Percent = ((double)hashsetRows.size()/rowLength) * 100;
		double cosineL2Percent = ((double)hashsetColumns.size()/columnLength) * 100;
		
		cosineEvaluation = cosineEvaluation + topic + "," + language1 + "." + language2 + "," + maxCosine + "," + rowLength + "," + columnLength + "," + cosineL1Percent + "," + cosineL2Percent + "," + (cosineMaxDiagonal>=2? "yes":"no") + "," + (cosineMaxDiagonal>=3? "yes":"no") + "," + (cosineMaxDiagonal>=4? "yes":"no") + "," + (cosineMaxDiagonal>=5? "yes":"no") + "\n";
		jaccardEvaluation = jaccardEvaluation + topic + "," + language1 + "." + language2 + "," + maxJaccard + "," + rowLength + "," + columnLength + "," + jaccardL1Percent + "," + jaccardL2Percent + "," + (jaccardMaxDiagonal>=2? "yes":"no") + "," + (jaccardMaxDiagonal>=3? "yes":"no") + "," + (jaccardMaxDiagonal>=4? "yes":"no") + "," + (jaccardMaxDiagonal>=5? "yes":"no") + "\n";
		
		
		LPP[0] = LPP[0] + "<a href=\"" + topic + "/" + language2 + ".html\">" + language2 + "</a><br>";
		LPP[1] = LPP[1] + jaccardL1Percent + "%<br>";
		LPP[2] = LPP[2] + (jaccardMaxDiagonal>=5? "yes":"no") +"<br>";
		
		
		return pair;
	}
	
	public void writeDiagonalMatrix(String language1, String language2, int rowLength, int columnLength, ArrayList<ArrayList<Point>> chains, String file){
		double[][] matrix = new double[rowLength][columnLength];
		int d = 0;
		for (ArrayList<Point> chain : chains) {
			if ((chain.size() >= 2) && (isExistingDiagonalNeighbor(chain))) {
				for (Point point : chain) {
					matrix[point.getRow()][point.getColumn()] = point.getValue();
				}
				d = d + 1;
			}
		}
		fileOperation.writeMatrix(language1, language2, matrix, file);
	}
	
	public double[][] findMatrixWithRectagleData(double[][] matrix, ArrayList<ArrayList<Point>> chains) {
		double[][] result = new double[matrix.length][matrix[0].length];
		for (ArrayList<Point> chain: chains) {
			if ((chain.size() >= 2) && (isExistingDiagonalNeighbor(chain))) {
				Point lowest = findLowestPoint(chain);
				Point highest = findHighestPoint(chain);
				System.out.println("from: [" + lowest.getRow() + "," + lowest.getColumn() + "] to: [" + highest.getRow() + "," + highest.getColumn() + "]");
				for (int row = lowest.getRow(); row <= highest.getRow(); row ++) {
					for (int column = lowest.getColumn(); column <= highest.getColumn(); column ++) {
						result[row][column] = matrix[row][column];
					}
				}
			}
			
		}
		return result;
	}
	
	public int findDiagonalSize(ArrayList<Point> chain) {
		List<Integer> rows = new ArrayList<Integer>();
		List<Integer> columns = new ArrayList<Integer>();
		for (Point point : chain) {
			rows.add(point.getRow());
			columns.add(point.getColumn());
		}
		Set<Integer> hashsetRows = new HashSet<Integer>(rows);
		Set<Integer> hashsetColumns = new HashSet<Integer>(columns);
		return Math.min(hashsetRows.size(), hashsetColumns.size());
	}
	
	public Point findLowestPoint(ArrayList<Point> chain) {
		Point lowestPoint = new Point(0, 0, 0);
		lowestPoint.setRow(chain.get(0).getRow());
		lowestPoint.setColumn(chain.get(0).getColumn());
		for (Point point : chain) {
			if (lowestPoint.getRow() > point.getRow()) {
				lowestPoint.setRow(point.getRow());
			}
			if (lowestPoint.getColumn() > point.getColumn()) {
				lowestPoint.setColumn(point.getColumn());
			}
		}
		return lowestPoint;
	}
	
	public Point findHighestPoint(ArrayList<Point> chain) {
		Point highestPoint = new Point(0, 0, 0);
		highestPoint.setRow(chain.get(0).getRow());
		highestPoint.setColumn(chain.get(0).getColumn());
		for (Point point : chain) {
			if (highestPoint.getRow() < point.getRow()) {
				highestPoint.setRow(point.getRow());
			}
			if (highestPoint.getColumn() < point.getColumn()) {
				highestPoint.setColumn(point.getColumn());
			}
		}
		return highestPoint;
	}
	
	public ArrayList<ArrayList<Point>> findChains(double[][] matrix) {

		int rowLength = matrix.length;
		int columnLength = matrix[0].length;
		
		List<Point> points = new CopyOnWriteArrayList<Point>();
		for (int row = 0; row < rowLength; row++) {
			for (int column = 0; column < columnLength; column++) {
				if(matrix[row][column] != 0) {
					Point point = new Point(row, column, matrix[row][column]);
					points.add(point);
				}
			}
		}
		
		
		ArrayList<ArrayList<Point>> chains = new ArrayList<ArrayList<Point>>();
		
		while (points.size() > 0) {
			ArrayList<Point> chain = new ArrayList<Point>();
			chain.add(points.remove(0));
			boolean flag = true;
			while(flag) {
				int exit = 0;
				for (int j = 0; j < chain.size(); j ++) {
					for (int i = 0; i < points.size(); i ++) {
						if (isNeighbor(chain.get(j), points.get(i)) && !isExistingPoint(points.get(i), chain)) {
							chain.add(points.remove(i));
						} else {
							exit = exit + 1;
						}
					}
				}
				
				if ((points.size() == 0) || (exit == (points.size() * chain.size()))) {
					flag = false;
				}
			}
			chains.add(chain);
		}
		
		
		return chains;
	}
	
	
	public boolean isExistingPoint(Point point, ArrayList<Point> points) {
		return points.contains(point);
	}
	
	
	public Point[] remove(int i, Point[] points) {
		Point[] newPoints = new Point[points.length - 1];
		int d = 0;
		for (int j = 0; j < points.length; j ++) {
			if (j != i) {
				newPoints[d] = points[j];
				d = d + 1;
			}
		}
		return newPoints;
	}
	
	public boolean isExistingDiagonalNeighbor(ArrayList<Point> chain) {
		for (int i = 0; i < chain.size(); i ++) {
			for (int j = 0; j < chain.size(); j ++) {
				if (isDiagonal(chain.get(i), chain.get(j))) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public boolean isDiagonal(Point a, Point b) {
				
		// Row - 1
		if ((a.getRow() == (b.getRow() - 1)) && ((a.getColumn() >= (b.getColumn() - 2)) && (a.getColumn() <= (b.getColumn() - 1)))) {
			return true;
		}
		
		if ((a.getRow() == (b.getRow() - 1)) && ((a.getColumn() >= (b.getColumn() + 1)) && (a.getColumn() <= (b.getColumn() + 2)))) {
			return true;
		}
		
		// Row + 1
		if ((a.getRow() == (b.getRow() + 1)) && ((a.getColumn() >= (b.getColumn() - 2)) && (a.getColumn() <= (b.getColumn() - 1)))) {
			return true;
		}
		
		if ((a.getRow() == (b.getRow() + 1)) && ((a.getColumn() >= (b.getColumn() + 1)) && (a.getColumn() <= (b.getColumn() + 2)))) {
			return true;
		}
		
		// Row - 2
		if ((a.getRow() == (b.getRow() - 2)) && ((a.getColumn() >= (b.getColumn() - 2)) && (a.getColumn() <= (b.getColumn() - 1)))) {
			return true;
		}
		
		if ((a.getRow() == (b.getRow() - 2)) && ((a.getColumn() >= (b.getColumn() + 1)) && (a.getColumn() <= (b.getColumn() + 2)))) {
			return true;
		}
		
		// Row + 2
		if ((a.getRow() == (b.getRow() + 2)) && ((a.getColumn() >= (b.getColumn() - 2)) && (a.getColumn() <= (b.getColumn() - 1)))) {
			return true;
		}
		
		if ((a.getRow() == (b.getRow() + 2)) && ((a.getColumn() >= (b.getColumn() + 1)) && (a.getColumn() <= (b.getColumn() + 2)))) {
			return true;
		}
		
		return false;
	}
	
	
	public boolean isNeighbor(Point a, Point b) {
		// Row 0;
		if (a.getRow() == b.getRow() && ((a.getColumn() == (b.getColumn() + 1)) || a.getColumn() == (b.getColumn() - 1))) {
			return true;
		}
		
		// Row - 1
		if ((a.getRow() == (b.getRow() - 1)) && ((a.getColumn() >= (b.getColumn() - 2)) && (a.getColumn() <= (b.getColumn() + 2)))) {
			return true;
		}
		
		// Row + 1
		if ((a.getRow() == (b.getRow() + 1)) && ((a.getColumn() >= (b.getColumn() - 2)) && (a.getColumn() <= (b.getColumn() + 2)))) {
			return true;
		}
		
		// Row - 2
		if ((a.getRow() == (b.getRow() - 2)) && ((a.getColumn() >= (b.getColumn() - 2)) && (a.getColumn() <= (b.getColumn() - 1)))) {
			return true;
		}
		
		if ((a.getRow() == (b.getRow() - 2)) && ((a.getColumn() >= (b.getColumn() + 1)) && (a.getColumn() <= (b.getColumn() + 2)))) {
			return true;
		}
		
		// Row + 2
		if ((a.getRow() == (b.getRow() + 2)) && ((a.getColumn() >= (b.getColumn() - 2)) && (a.getColumn() <= (b.getColumn() - 1)))) {
			return true;
		}
		
		if ((a.getRow() == (b.getRow() + 2)) && ((a.getColumn() >= (b.getColumn() + 1)) && (a.getColumn() <= (b.getColumn() + 2)))) {
			return true;
		}
		
		return false;
	}
	
	
	public double[][] sweepMatrix(double[][] matrix) {
		int rowLength = matrix.length;
		int columnLength = matrix[0].length;
		
		double[][] m1 = new double[rowLength][columnLength];

		for (int row = 0; row < rowLength; row++) {
			double max = 0;
			for (int column = 0; column < columnLength; column++) {
				if (max < matrix[row][column]) {
					max = matrix[row][column];
				}
			}
			for (int column = 0; column < columnLength; column++) {
				if (matrix[row][column] == max) {
					m1[row][column] = max;
				}
			}
		}
		
		double[][] m2 = new double[rowLength][columnLength];
		
		for (int column = 0; column < columnLength; column++) {
			double max = 0;
			for (int row = 0; row < rowLength; row++) {
				if (max < matrix[row][column]) {
					max = matrix[row][column];
				}
			}
			for (int row = 0; row < rowLength; row++) {
				if (matrix[row][column] == max) {
					m2[row][column] = max;
				}
			}
		}
		
		for (int row = 0; row < rowLength; row++) {
			for (int column = 0; column < columnLength; column++) {
				if (m1[row][column] != m2[row][column]) {
					m1[row][column] = 0;
				}
			}
		}
		
		return m1;
	}
	

	public double[][] sweepMatrix(double[][] matrix, double threshold) {
				
		int rowLength = matrix.length;
		int columnLength = matrix[0].length;

		for (int row = 0; row < rowLength; row++) {
			for (int column = 0; column < columnLength; column++) {
				if (matrix[row][column] < (threshold)) {
					matrix[row][column] = 0;
				}
			}
		}
		
		return matrix;
	}
	
	

	public double getStandardDeviationOfMatrix(double[][] matrix) {
		int rowLength = matrix.length;
		int columnLength = matrix[0].length;
		double sum = 0;
		for (int row = 0; row < rowLength; row++) {
			for (int column = 0; column < columnLength; column++) {
				sum = sum + matrix[row][column];
			}
		}
		double average = sum / (rowLength * columnLength);

		sum = 0;
		for (int row = 0; row < rowLength; row++) {
			for (int column = 0; column < columnLength; column++) {
				sum = sum + Math.pow((matrix[row][column] - average), 2);
			}
		}

		return Math.sqrt(sum / (rowLength * columnLength));
	}

}
