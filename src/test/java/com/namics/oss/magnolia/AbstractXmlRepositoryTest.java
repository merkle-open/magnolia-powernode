package com.namics.oss.magnolia;

import info.magnolia.importexport.DataTransporter;
import info.magnolia.test.RepositoryTestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.InputStream;

public abstract class AbstractXmlRepositoryTest extends RepositoryTestCase {

	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		InputStream is = this.getClass().getResourceAsStream(getRepositoryXmlPath());
		DataTransporter.importXmlStream(is,
				getWorkspaceName(),
				"/",
				getRepositoryXmlPath(),
				false,
				false,
				0,
				true,
				true
		);
	}

	@AfterEach
	public void tearDown() throws Exception {
		super.tearDown();
	}

	public abstract String getRepositoryXmlPath();

	public abstract String getWorkspaceName();

}