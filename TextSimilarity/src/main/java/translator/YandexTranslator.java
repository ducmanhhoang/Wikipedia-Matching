package translator;

import java.util.ArrayList;

import com.rmtheis.yandtran.ApiKeys;
import com.rmtheis.yandtran.language.Language;
import com.rmtheis.yandtran.translate.Translate;

import fileoperation.FileOperation;
import constant.Constant;

public class YandexTranslator {
	FileOperation fileOperation = new FileOperation();
	public void translateAllTopicVersionsToEnglish(String authorLang) {
		ArrayList<String> inputFolders = fileOperation.listFolder(Constant.INPUT_DIRECTORY + "/" + authorLang);
		for (String inputFolder : inputFolders) {
			ArrayList<String> inputFiles = fileOperation.listFiles(Constant.INPUT_DIRECTORY + "/" + authorLang + "/" + inputFolder);
			for (String inputFile : inputFiles) {
				switch (inputFile) {
				case "fr":
					translateEachLanguageVersion(authorLang, inputFolder, inputFile, Language.FRENCH);
					break;
				case "it":
					translateEachLanguageVersion(authorLang, inputFolder, inputFile, Language.ITALIAN);
					break;
				case "de":
					translateEachLanguageVersion(authorLang, inputFolder, inputFile, Language.GERMAN);
					break;
				case "en":
					fileOperation.copyFile(Constant.INPUT_DIRECTORY + "/" + authorLang + "/" + inputFolder + "/" + inputFile, Constant.TRANSLATION_DIRECTORY + "/" + authorLang + "/" + inputFolder + "/" + inputFile);
					break;
				}
			} // end for files
		} // end for folders
	}// end function
	
	public void translateEachLanguageVersion(String authorLang, String folder, String file, Language language) {
		try {
			String inputStrings[] = fileOperation.readFile(Constant.INPUT_DIRECTORY + "/" + authorLang + "/" + folder + "/" + file).split(Constant.PARAGRAPH_SPLIT_REGEX);
			Translate.setKey(ApiKeys.YANDEX_API_KEY);
			System.out.println(language.toString() + " version is translating...");
			
			String outputString = "";
			for (String inputString: inputStrings) {
				String translation = Translate.execute(inputString, language, Language.ENGLISH);
				outputString = outputString + translation + "\n";
			}
			
			fileOperation.createFolder(Constant.TRANSLATION_DIRECTORY + "/" + authorLang + "/" + folder);
			fileOperation.writeFile(Constant.TRANSLATION_DIRECTORY + "/" + authorLang + "/" + folder + "/" + file, outputString);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
