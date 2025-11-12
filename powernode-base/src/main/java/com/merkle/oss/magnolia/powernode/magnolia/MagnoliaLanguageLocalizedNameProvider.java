package com.merkle.oss.magnolia.powernode.magnolia;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.module.site.SiteManager;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import javax.jcr.Node;

import com.merkle.oss.magnolia.powernode.AbstractLocalizedNameProvider;

public class MagnoliaLanguageLocalizedNameProvider extends AbstractLocalizedNameProvider {

    @Inject
	public MagnoliaLanguageLocalizedNameProvider(
			final SiteManager siteManager,
			final I18nContentSupport i18nContentSupport
	) {
        super(siteManager, i18nContentSupport);
	}

	protected MagnoliaLanguageLocalizedNameProvider(final Function<Optional<Node>, I18nContentSupport> i18nContentSupportProvider) {
		super(i18nContentSupportProvider);
	}

	@Override
	protected boolean isDefaultLocale(final Locale defaultLocale, final Locale locale) {
		return equalsLanguage(defaultLocale, locale);
	}

	@Override
	protected String appendLocaleSuffix(final String propertyName, final Locale locale) {
		return String.format("%s_%s", propertyName, locale.getLanguage());
	}

	private boolean equalsLanguage(@Nullable final Locale locale1, @Nullable final Locale locale2) {
		return Objects.equals(
				Optional.ofNullable(locale1).map(Locale::getLanguage),
				Optional.ofNullable(locale2).map(Locale::getLanguage)
		);
	}
}
