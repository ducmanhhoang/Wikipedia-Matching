package constant;

public class Constant {
	public final static String INPUT_DIRECTORY = "data/input";
	public final static String TRANSLATION_DIRECTORY = "data/translation";
	public final static String OUTPUT_DIRECTORY = "data/output";
	
	public final static String PARAGRAPH_SPLIT_REGEX = "\\n";
	
	public final static String STOPWORD_DIRECTORY = "stopwords/english.stop";
	
	public final static int NUMBER_OF_SM = 9;
	
	public final static int STEMMING_REPEATE = 1;
	
	public final static String WIKI_API_URL = "https://%%.wikipedia.org/w/api.php";
	
	public final static String[] LANGUAGE = {"en", "it", "fr", "de"};
	
	public final static String[] REMOVING_PATTERN = {"\\{\\{((([a-zA-Z])+)|(([a-zA-Z])+( ([a-zA-Z])+))+)\\}\\}", "&#39", "&#34", "\\[[0-9]+\\]"};

}
