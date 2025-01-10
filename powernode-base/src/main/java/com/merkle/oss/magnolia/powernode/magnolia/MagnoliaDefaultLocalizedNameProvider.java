package com.merkle.oss.magnolia.powernode.magnolia;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.module.site.SiteManager;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;
import javax.jcr.Node;

import com.merkle.oss.magnolia.powernode.AbstractLocalizedNameProvider;

public class MagnoliaDefaultLocalizedNameProvider extends AbstractLocalizedNameProvider {

	@Inject
	public MagnoliaDefaultLocalizedNameProvider(
			final SiteManager siteManager,
			final I18nContentSupport i18nContentSupport
	) {
		super(siteManager, i18nContentSupport);
	}

	protected MagnoliaDefaultLocalizedNameProvider(final Function<Optional<Node>, I18nContentSupport> i18nContentSupportProvider) {
		super(i18nContentSupportProvider);
	}

	@Override
	protected boolean isDefaultLocale(final Locale defaultLocale, final Locale locale) {
		return Objects.equals(defaultLocale, locale);
	}

	@Override
	protected String appendLocaleSuffix(String propertyName, Locale locale) {
		return String.format("%s_%s", propertyName, locale.toString());
	}
}
