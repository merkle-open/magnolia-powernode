package com.merkle.oss.magnolia.powernode.magnolia;

import com.merkle.oss.magnolia.powernode.LocalizedNameProvider;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.module.site.Site;
import info.magnolia.module.site.SiteManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class MagnoliaLanguageLocalizedNameProviderTest {
	private LocalizedNameProvider localizedNameProvider;

	@BeforeEach
	void setUp() {
		final I18nContentSupport i18nContentSupport = mock(I18nContentSupport.class);
		doReturn(Locale.CANADA).when(i18nContentSupport).getDefaultLocale();
		final Site site = mock(Site.class);
		doReturn(i18nContentSupport).when(site).getI18n();
		final SiteManager siteManager = mock(SiteManager.class);
		doReturn(site).when(siteManager).getCurrentSite();
		localizedNameProvider = new MagnoliaLanguageLocalizedNameProvider(siteManager, i18nContentSupport);
	}

	@Test
	void getLocalizedPropertyName() {
		assertEquals(
				"someKey",
				localizedNameProvider.getLocalizedPropertyName(null, "someKey", Locale.CANADA)
		);
		assertEquals(
				"someKey_fr",
				localizedNameProvider.getLocalizedPropertyName(null, "someKey", Locale.CANADA_FRENCH)
		);
	}

	@Test
	void getLocalizedNodeName() {
		assertEquals(
				"someNodeName",
				localizedNameProvider.getLocalizedNodeName(null, "someNodeName", Locale.CANADA)
		);
		assertEquals(
				"someNodeName_fr",
				localizedNameProvider.getLocalizedNodeName(null, "someNodeName", Locale.CANADA_FRENCH)
		);
	}
}