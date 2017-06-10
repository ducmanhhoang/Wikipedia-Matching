package application;

import crawler.WikiCrawler;
import textsimilarity.TextSimilarity;
import translator.YandexTranslator;
import dkpro.similarity.algorithms.api.TextSimilarityMeasure;

public class App {
	public static void main(String[] args) {
		String[] authors = {
				"Financier73",
				"Lancasterspotting",
				"Mandalorian",
				"K.Weise"
		};
		
		String[] languages = {
				"fr",
				"fr",
				"it",
				"it"
		};
		
		String startTime = "2014-01-01T20:39:12.000Z";
		String endTime = "2017-05-01T20:39:12.000Z";
		
		/*
		WikiCrawler wikicrawler = new WikiCrawler();
		for (int i = 0; i < authors.length; i ++) {
			wikicrawler.writeData(wikicrawler.getContentOfArticles(authors[i], languages[i], startTime, endTime), authors[i] + "." + languages[i]);
		}
		*/
		
		/*
		YandexTranslator yandexTranslator = new YandexTranslator();
		for (int i = 0; i < authors.length; i ++) {
			yandexTranslator.translateAllTopicVersionsToEnglish(authors[i] + "." + languages[i]);
		}
		*/
		
		TextSimilarity textSimilarity = new TextSimilarity();
		for (int i = 0; i < authors.length; i ++) {
			textSimilarity.computeTextSimilarityScore(authors[i], languages[i]);
		}
	}
}
