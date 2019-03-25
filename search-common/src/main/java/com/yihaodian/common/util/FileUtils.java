package com.yihaodian.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.PriorityQueue;

import org.apache.log4j.Logger;

public class FileUtils {
	private static final Logger log = Logger.getLogger(FileUtils.class);
	/**
	 * 递归删除文件或目录
	 * 
	 * @param f
	 */
	public static void deleteFile(File f) {
		if (!f.exists()) {
			return;
		}

		if (f.isDirectory()) {
			File[] delFiles = f.listFiles();
			if(delFiles != null) {
				for (int i = 0; i < delFiles.length; i++) {
					deleteFile(delFiles[i]);
				}
			}
			f.delete();
		} else {
			f.delete();
		}
	}
	
	public static boolean writeToFile(File file, String content){
		new File(file.getParent()).mkdirs();
		file.delete();
		FileWriter fw = null;
		try {
			file.createNewFile();
			fw = new FileWriter(file);
			fw.write(content);
		} catch(IOException e) {
			log.warn("Write file error.", e);
			return false;
		} finally {
			if (fw != null) {
				try{
					fw.close();
				} catch(Exception e){
					log.warn("Close file writer error.", e);
				}
			}
		}
		return true;
	}
	
	/**
	 * 删除dir下的文件，只保留restoreNum个
	 * 保留规则：文件名倒排
	 * @param dir
	 * @return
	 */
	public static boolean deleteOldVersion(String dirPath, int restoreNum){
		File dir = new File(dirPath);
		if (!dir.isDirectory()) {
			return false;
		}
		
		File[] chiFiles = dir.listFiles();
		PriorityQueue<String> queue = new PriorityQueue<String>();
		
		for (File file : chiFiles) {
			queue.add(file.getName());
		}

		for (int i = 0; i < chiFiles.length - restoreNum; i++) {
			queue.poll();
		}
		
		for (File file : chiFiles) {
			if (!queue.contains(file.getName())) {
				deleteFile(file);
			}
		}
		
		return true;
	}
	
	public static boolean writeToFile(String fileName, String content){
		return writeToFile(new File(fileName), content);
	}
	
	public static String readFileContent(String filePath) throws IOException{
		StringBuilder contentBuilder = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filePath));
			String line = null;
			while ((line = reader.readLine()) != null) {
				contentBuilder.append(line);
				contentBuilder.append("\r\n");
			}
			contentBuilder.delete(contentBuilder.length() - 2,
					contentBuilder.length() - 1);
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return contentBuilder.toString();
	}
	
	/**
	 * 获取文件(夹)大小  bytes
	 * @param f
	 * @return The file size in bytes.
	 * @throws IOException
	 */
	public static long getFileSize(File f){
		long size = 0;
		if (f.isDirectory()) {
			for (File child : f.listFiles()) {
				size += getFileSize(child);
			}
		} else {
			size = f.length();
		}
		
		return size;
	}
	
	/**
	 * 更新文件
	 * 1. 文件不存在则直接写
	 * 2. 文件存在则先重命名原文件，写文件，再删除被重命名后的文件
	 * @param fileName
	 * @param content
	 * @return
	 */
	public static boolean updateFile(String fileName, String content){
		File oldFile = new File(fileName);
		if (!oldFile.exists()) {
			return writeToFile(oldFile, content);
		}
		
		File backFile = new File(fileName + ".bak");
		if (backFile.exists()) {
			deleteFile(backFile);
		}
		
		if (!oldFile.renameTo(backFile)) {
			log.error("oldFile rename fail");
			return false;
		}
		
		if (writeToFile(fileName, content)) {
			deleteFile(backFile);
		} else {
			if (!backFile.renameTo(oldFile)) {
				log.error("backFile rename fail");
				return false;
			}
		}
		return true;
	}
	
	public static void main(String[] args) throws Exception{
		System.out.println(getFileSize(new File("/var/www/data/mars_index/index/ads/20140708_113944/s0")));
	}
}
