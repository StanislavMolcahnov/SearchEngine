package main.services;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class LemmatizerService {

    private final Pattern russianPattern = Pattern.compile("[ёЁА-Яа-я]");
    private final Pattern englishPattern = Pattern.compile("[A-Za-z]");
    private TreeMap<String, Integer> countOfLemmas = new TreeMap<>();
    private Map<String, String> lemmaPlusWord = new TreeMap<>();

    public void countOfLemmas(String text) {
        countOfLemmas = new TreeMap<>();
        lemmaPlusWord = new LinkedHashMap<>();
        try {
            List<String> wordBaseForms;
            LuceneMorphology englishLuceneMorph = new EnglishLuceneMorphology();
            LuceneMorphology russianLuceneMorph = new RussianLuceneMorphology();
            String[] splitText = splitText(text);
            for (String word : splitText) {
                try {
                    if (word.equals("") || (russianPattern.matcher(word).find() && englishPattern.matcher(word).find())) {
                        continue;
                    }
                    wordBaseForms = russianLuceneMorph.getMorphInfo(word.toLowerCase());
                    if (checkWords(wordBaseForms.toString())) {
                        continue;
                    }
                    wordBaseForms = russianLuceneMorph.getNormalForms(word.toLowerCase());
                } catch (Exception ex) {
                    wordBaseForms = englishLuceneMorph.getMorphInfo(word.toLowerCase());
                    if (checkWords(wordBaseForms.toString())) {
                        continue;
                    }
                    wordBaseForms = englishLuceneMorph.getNormalForms(word.toLowerCase());
                }
                saveToLemmaPlusWord(wordBaseForms, word);
                saveToCountOfLemmas(wordBaseForms);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String[] splitText(String text) {
        text = text.replaceAll("[^ёЁА-Яа-яA-Za-z]", " ").replaceAll("[ёЁ]", "е");

        return text.split("\\s+");
    }

    private boolean checkWords(String word) {
        Pattern unnecessaryWords = Pattern.compile("(СОЮЗ)|(ПРЕДЛ)|(ЧАСТ)|(МЕЖД)|(МС)|" +
                "(PART)|(INT)|(PREP)|(CONJ)|(ARTICLE)|(PN)");
        return unnecessaryWords.matcher(word).find();
    }

    private void saveToCountOfLemmas(List<String> wordBaseForms) {
        String keyToMap = new TreeSet<>(wordBaseForms).first();
        if (countOfLemmas.containsKey(keyToMap)) {
            countOfLemmas.put(keyToMap, countOfLemmas.get(keyToMap) + 1);
        } else {
            countOfLemmas.put(keyToMap, 1);
        }
    }

    private void saveToLemmaPlusWord(List<String> wordBaseForms, String originalWord) {
        String keyToMap = new TreeSet<>(wordBaseForms).first();
        lemmaPlusWord.put(keyToMap, originalWord);
    }

    public TreeMap<String, Integer> getCountOfLemmas() {
        return countOfLemmas;
    }

    public Map<String, String> getLemmaPlusWord() {
        return lemmaPlusWord;
    }
}
