package com.yihaodian.store.zk;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

public class ZkFileUtilTest {

	@Test
	public void test() {
		String path = "test/zkFileUtilTest";
		List<String> data = Arrays.asList("1", "one", "2", "two");
		ZkFileUtil.getInstance().save(path, data);

		List<String> loadData = ZkFileUtil.getInstance().load(path);

		Assert.assertEquals(data, loadData);
	}

	@Test
	public void testSlash() {
		String path = "test/zkFileUtilTest";

		String pathSlash = "/test/zkFileUtilTest";
		List<String> data = Arrays.asList("1", "one", "2", "two");
		ZkFileUtil.getInstance().save(path, data);

		List<String> loadData = ZkFileUtil.getInstance().load(pathSlash);

		Assert.assertEquals(data, loadData);
	}

	@Test
	public void testFileSync() throws IOException {
		String localPath = "zkFileUtilTest";
		String path = "test/zkFileUtilTest";

		List<String> data = Arrays.asList("1", "one", "2", "two");
		ZkFileUtil.getInstance().save(path, data);
		List<String> loadData = ZkFileUtil.getInstance().load(path);

		Assert.assertEquals(data, loadData);

		// sync from remote
		ZkFileUtil.getInstance().syncFromZk(localPath, path);
		File localFile = new File(localPath);
		List<String> fileData = FileUtils.readLines(localFile);
		Assert.assertEquals(data, fileData);

		// sync to remote
		FileUtils.deleteQuietly(localFile);
		localFile = new File(localPath);
		data = new ArrayList<String>(data);
		data.add("3");
		data.add("three");
		FileUtils.writeLines(localFile, data);
		ZkFileUtil.getInstance().syncToZk(localPath, path);
		loadData = ZkFileUtil.getInstance().load(path);		
		Assert.assertEquals(data, loadData);

	}
}
