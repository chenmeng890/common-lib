package com.yihaodian.common.mail;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;

/**
 * find template from classpath using ClassLoader
 * 
 */
public class ClassPathTemplateLoader implements TemplateLoader {
	static Logger log = Logger.getLogger(ClassPathTemplateLoader.class);

	private TemplateLoader _loader;

	/**
	 * find template from classpath using ClassLoader
	 * 
	 * @param classLoader
	 * @param templateName
	 * @throws IOException
	 */
	public ClassPathTemplateLoader(ClassLoader classLoader,
			String... templateName) throws IOException {
		_loader = getTemplateLoader(classLoader, templateName);
	}

	TemplateLoader getTemplateLoader(ClassLoader classLoader,
			String... templateName) throws IOException {
		StringTemplateLoader loader = new StringTemplateLoader();
		if(templateName == null)
			return loader;
		
		for (String template : templateName) {
			log.info("loading template: " + template);

			InputStream input = classLoader.getResourceAsStream(template);
			String templateSource = IOUtils.toString(input,
					TemplateManager.encoding);
			loader.putTemplate(template, templateSource);
		}

		return loader;
	}

	@Override
	public Object findTemplateSource(String name) throws IOException {
		return _loader.findTemplateSource(name);
	}

	@Override
	public long getLastModified(Object templateSource) {
		return _loader.getLastModified(templateSource);
	}

	@Override
	public Reader getReader(Object templateSource, String encoding)
			throws IOException {
		return _loader.getReader(templateSource, encoding);
	}

	@Override
	public void closeTemplateSource(Object templateSource) throws IOException {
		_loader.closeTemplateSource(templateSource);
	}
}
