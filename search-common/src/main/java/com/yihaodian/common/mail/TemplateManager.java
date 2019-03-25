package com.yihaodian.common.mail;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.apache.log4j.Logger;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Freemarker template
 */
public class TemplateManager {
	static Logger log = Logger.getLogger(TemplateManager.class);

	public static String encoding = "UTF-8";

	public static String FTL_SUCCESS = "success.ftl";

	Configuration cfg;

	public TemplateManager(TemplateLoader loader) {
		cfg = new Configuration();

		cfg.setTemplateLoader(loader);

		cfg.setObjectWrapper(new DefaultObjectWrapper());
	}

	Template getTemplate(String name) throws IOException {
		return cfg.getTemplate(name, encoding);
	}

	public String process(String name, Map rootMap) throws TemplateException,
			IOException {
		Template temp = getTemplate(name);
		StringWriter out = new StringWriter();

		temp.process(rootMap, out);

		out.flush();

		return out.toString();
	}
}
