package com.yihaodian.search.nlp.util;

import java.util.List;

import com.yihaodian.search.nlp.model.Lexeme;

public class PrintSegmenter {

	public static void printList(String message,List<String> words){
		StringBuilder sb=new StringBuilder();
		sb.append(message).append(": ");
		for(String word:words){
			sb.append(word).append(" ,");
		}
		System.out.println(sb.toString());
	}
	
	public static void printLexemes(String message,List<Lexeme> lexemes){
		StringBuilder sb=new StringBuilder();
		sb.append(message).append(": ");
		for(Lexeme le:lexemes){
			sb.append(le.getText()).append("/").append(le.getType()).append(" ,");
		}
		System.out.println(sb.toString());
	}
}
