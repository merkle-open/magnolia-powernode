package com.namics.oss.magnolia.powernode;

import com.namics.oss.magnolia.AbstractXmlRepositoryTest;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.module.site.i18n.SiteI18nContentSupport;
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
		DefaultLanguageHelper languageService = Mockito.mock(DefaultLanguageHelper.class);
		Mockito.when(languageService.getDefaultLanguage()).thenReturn(Locale.GERMAN);
		this.powerNodeService = new PowerNodeService(languageService);

		SiteI18nContentSupport mockSiteI18nContentSupport = Mockito.mock(SiteI18nContentSupport.class);
		Mockito.when(mockSiteI18nContentSupport.toI18NURI(Mockito.anyString())).thenAnswer(invocation -> invocation.getArguments()[0]);
		ComponentsTestUtil.setInstance(I18nContentSupport.class, mockSiteI18nContentSupport);
	}

	public String getRepositoryXmlPath() {
		return WEBSITE_WORKSPACE_TESTCASE_XML;
	}

	public String getWorkspaceName() {
		return RepositoryConstants.WEBSITE;
	}
}
