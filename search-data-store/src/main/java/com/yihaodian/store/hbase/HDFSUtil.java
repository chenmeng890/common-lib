package com.yihaodian.store.hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.LineReader;
import org.apache.log4j.Logger;

import com.yihaodian.store.conf.DataStoreConfig;

/**
 * this class is access hdfs interface
 *
 */
public class HDFSUtil {

	static Logger log = Logger.getLogger(HDFSUtil.class);
	public static String fs_name_dev = "hdfs://192.168.130.106:9000";
	public static String fs_name_stg = "hdfs://10.63.0.154:9000";
	public static String fs_name_prod = "hdfs://10.3.0.66:9000";
	FileSystem fs = null;

	/**
	 * constructor HDFSUtil
	 * @param fs_name, eg:hdfs://192.168.39.8:9000
	 * @throws IOException
	 */
	public HDFSUtil(String fs_name) throws IOException {
		fs = getFileSystem(fs_name);
		log.info("hbase: " + fs_name);
	}
	
	public HDFSUtil(Configuration conf) throws IOException {
		fs = FileSystem.get(conf);
	}

	/**
	 * 
	 * @param fs
	 */
	public HDFSUtil(FileSystem fs) {
		this.fs = fs;
	}

	/**
	 * @return local FileSystem
	 * @throws IOException
	 */
	public static FileSystem getLocalFileSystem() throws IOException {
		Configuration conf = new Configuration();
		return FileSystem.getLocal(conf);
	}

	/**
	 * @param fs_name
	 * @return hdfs fileSystem
	 * @throws IOException
	 */
	public static FileSystem getFileSystem(String fs_name) throws IOException {
		Configuration conf = new Configuration();
		String env=DataStoreConfig.getEnv();
		conf.set("fs.defaultFS", fs_name);
		conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
		conf.set("fs.file.impl",org.apache.hadoop.fs.LocalFileSystem.class.getName());
		log.info("env===="+env);
		if(env.equals("production") || env.equals("staging")){
			conf.set("fs.defaultFS", "hdfs://Search:8020");
			conf.set("dfs.client.failover.proxy.provider.Search",    
					 "org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider");
			conf.set("dfs.nameservices", "Search");
			conf.set("dfs.ha.namenodes.Search","NN1,NN2");
			conf.set("dfs.namenode.rpc-address.Search.NN1", "10.4.11.42:8020");
			conf.set("dfs.namenode.rpc-address.Search.NN2", "10.4.11.43:8020");
		}
		
		FileSystem dfs = FileSystem.get(conf);
		// ((DistributedFileSystem)
		// dfs).setSafeMode(FSConstants.SafeModeAction.SAFEMODE_LEAVE);
		return dfs;
	}

	/**
	 * read file(s) as string from path
	 * 
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public List<String> read(String path) {
		try {
			Path p = new Path(path);
			if (fs.isFile(p)) {
				return read(p);
			} else {
				List<String> ret = new ArrayList<String>();
				FileStatus[] childs = fs.listStatus(p);
				for (FileStatus child : childs) {
					Path childPath = child.getPath();
					String name = childPath.getName();

					if (fs.isFile(childPath) && !name.startsWith("_")) {
						ret.addAll(read(childPath));
					}
				}

				return ret;
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("read error " + path, e);
			throw new RuntimeException(e);
		}

	}
	
	public List<String> getChildPaths(String path){
		List<String> ret=new ArrayList<String>();
		try{
			Path p=new Path(path);
			FileStatus[] childs = fs.listStatus(p);
			for (FileStatus child : childs) {
				Path childPath = child.getPath();
				String name = childPath.getName();
                ret.add(path+"/"+name);
			}
		}catch(Exception e){
			log.error(e.getMessage(), e);
		}
		return ret;
	} 
	
	public List<String> getChildNames(String path){
		List<String> ret=new ArrayList<String>();
		try{
			Path p=new Path(path);
			FileStatus[] childs = fs.listStatus(p);
			for (FileStatus child : childs) {
				Path childPath = child.getPath();
				String name = childPath.getName();
                ret.add(name);
			}
		}catch(Exception e){
			log.error(e.getMessage(), e);
		}
		return ret;
	}

	/**
	 * read hdfs path's content 
	 * @param p
	 * @return
	 * @throws IOException
	 */
	public List<String> read(Path p) throws IOException {
		FSDataInputStream input = fs.open(p);
		List<String> ret = new ArrayList<String>();
		LineReader reader = new LineReader(input);
		Text text = new Text();
		int read = reader.readLine(text);
		while (read > 0) {
			ret.add(text.toString());
			read = reader.readLine(text);
		}

		return ret;
	}
	
	public void deletePath(String path) throws IllegalArgumentException, IOException{
		fs.delete(new Path(path),true);
	}
    
	/**
	 * 
	 * @return fileSystem
	 */
	public FileSystem getFileSystem() {
		return fs;
	}

	/**
	 * copy from local to same path
	 * 
	 * @param fs
	 * @param path
	 */
	public static void copyFromLocal(boolean delSrc, FileSystem fs, String path)
			throws IOException {
		log.info("upload  from " + path + " to " + path);

		Path src = new Path(path);
		Path dst = new Path(path);

		fs.copyFromLocalFile(delSrc, src, dst);
	}
	
	/**
	 * copy from local to same path
	 * 
	 * @param fs
	 * @param path
	 */
	public void copyFromLocal(boolean delSrc,String srcPath, String destPath)
			throws IOException {
		log.info("upload  from " + srcPath + " to " + destPath);

		Path src = new Path(srcPath);
		Path dst = new Path(destPath);
		fs.copyFromLocalFile(delSrc, src, dst);
	}
	

	/**
	 * get hdfs directory length
	 * 
	 * @param fs
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public static long getDirectorySize(FileSystem fs, Path path)
			throws IOException {
		int size = 0;
		FileStatus[] status = fs.listStatus(path);
		if (status != null && status.length > 0) {
			for (FileStatus s : status)
				size += s.getLen();
		}

		return size;
	}
}
