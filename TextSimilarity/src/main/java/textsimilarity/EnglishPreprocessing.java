package textsimilarity;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import constant.Constant;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordPosTagger;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordSegmenter;
import de.tudarmstadt.ukp.dkpro.core.stopwordremover.StopWordRemover;
import de.tudarmstadt.ukp.dkpro.core.stanfordnlp.StanfordLemmatizer;

public class EnglishPreprocessing {

	AnalysisEngine segmenter = null;
	AnalysisEngine posTager = null;
	AnalysisEngine lemmatizer = null;
	AnalysisEngine swRemover = null;
	JCas cas = null;
	
	ArrayList<String[]> preprocessedContent = null;
	ArrayList<String> nonPreprocessedContent = null;

	public EnglishPreprocessing() {
		try {
			segmenter = AnalysisEngineFactory.createEngine(StanfordSegmenter.class);
			posTager = AnalysisEngineFactory.createEngine(StanfordPosTagger.class);
			lemmatizer = AnalysisEngineFactory.createEngine(StanfordLemmatizer.class);
			swRemover = AnalysisEngineFactory.createEngine(StopWordRemover.class, StopWordRemover.PARAM_MODEL_LOCATION, new String[]{Constant.STOPWORD_DIRECTORY});
			cas = JCasFactory.createJCas();
		} catch (Exception e) {

		}
	}

	public void preprocess(String args) {
		Pattern pattern = Pattern.compile("\\w");
		preprocessedContent = new ArrayList<String[]>();
		nonPreprocessedContent = new ArrayList<String>();

		try {
			cas.reset();
			cas.setDocumentText(args);
			cas.setDocumentLanguage("en");
			SimplePipeline.runPipeline(cas, segmenter, posTager, lemmatizer, swRemover);
			
			int d = 1;
			for (Sentence sentence : JCasUtil.select(cas, Sentence.class)) {
				String st = "";
				for (Lemma l : JCasUtil.selectCovered(cas, Lemma.class, sentence)) {
					if (pattern.matcher(l.getCoveredText()).find()) {
						st = st + l.getValue() + " ";
					}
				}
				if (!st.equalsIgnoreCase("")) {
					preprocessedContent.add(st.split(" "));
					if (sentence.getCoveredText().contains(System.getProperty("line.separator"))/*sentence.getCoveredText().contains("\n") || sentence.getCoveredText().contains("\r")*/){
						nonPreprocessedContent.add(sentence.getCoveredText().replace(System.getProperty("line.separator"), " "));
//						System.out.println("[" + d + "]" + st);
//						System.out.println("[" + d + "]" + sentence.getCoveredText().replace(System.getProperty("line.separator"), " "));
					} else {
						nonPreprocessedContent.add(sentence.getCoveredText());
					}
//					System.out.println("[" + d + "]" + st);
//					System.out.println("[" + d + "]" + sentence.getCoveredText());
					d = d + 1;
				}
			}
		} catch (Exception e) {
		}
	}

	public ArrayList<String[]> getPreprocessedContent() {
		return preprocessedContent;
	}

	public void setPreprocessedContent(ArrayList<String[]> preprocessedContent) {
		this.preprocessedContent = preprocessedContent;
	}

	public ArrayList<String> getNonPreprocessedContent() {
		return nonPreprocessedContent;
	}

	public void setNonPreprocessedContent(ArrayList<String> nonPreprocessedContent) {
		this.nonPreprocessedContent = nonPreprocessedContent;
	}

}