package com.namics.oss.magnolia.powernode;

import com.namics.oss.magnolia.AbstractXmlRepositoryTest;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

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

		final I18NAuthoringSupport mockI18NAuthoringSupport = Mockito.mock(I18NAuthoringSupport.class);
		doAnswer(invocationOnMock -> {
			final String propertyName = invocationOnMock.getArgument(0);
			final Locale locale = invocationOnMock.getArgument(1);
			return propertyName + "_" + locale.getLanguage();
		}).when(mockI18NAuthoringSupport).deriveLocalisedPropertyName(any(), any());

		this.powerNodeService = new PowerNodeService(new DefaultLocalizedPropertyNameProvider(mockI18nContentSupport, mockI18NAuthoringSupport));
	}

	public String getRepositoryXmlPath() {
		return WEBSITE_WORKSPACE_TESTCASE_XML;
	}

	public String getWorkspaceName() {
		return RepositoryConstants.WEBSITE;
	}
}
