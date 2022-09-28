package com.namics.oss.magnolia.powernode;

import com.namics.oss.magnolia.AbstractXmlRepositoryTest;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import java.util.Locale;

public abstract class AbstractPowerNodeTest extends AbstractXmlRepositoryTest {

	private static final String WEBSITE_WORKSPACE_TESTCASE_XML = "/repositoryxml/powernode-testcase-website-workspace.xml";

	protected PowerNodeService powerNodeService;

	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();

		final I18nContentSupport mockI18nContentSupport = Mockito.mock(I18nContentSupport.class);
		Mockito.when(mockI18nContentSupport.toI18NURI(Mockito.anyString())).thenAnswer(invocation -> invocation.getArguments()[0]);
		Mockito.doReturn(Locale.GERMAN).when(mockI18nContentSupport).getDefaultLocale();
		ComponentsTestUtil.setInstance(I18nContentSupport.class, mockI18nContentSupport);

		this.powerNodeService = new PowerNodeService(new DefaultLocalizedNameProvider(mockI18nContentSupport));
	}

	public String getRepositoryXmlPath() {
		return WEBSITE_WORKSPACE_TESTCASE_XML;
	}

	public String getWorkspaceName() {
		return RepositoryConstants.WEBSITE;
	}
}
