package com.merkle.oss.magnolia.powernode.magnolia;

import com.merkle.oss.magnolia.powernode.LocalizedNameProvider;
import info.magnolia.cms.i18n.I18nContentSupport;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.jcr.Node;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class MagnoliaLanguageLocalizedNameProvider implements LocalizedNameProvider {
	private final I18nContentSupport i18nContentSupport;

	@Inject
	public MagnoliaLanguageLocalizedNameProvider(final  I18nContentSupport i18nContentSupport) {
		this.i18nContentSupport = i18nContentSupport;
	}

	@Override
	public String getLocalizedPropertyName(@Nullable final Node node, final String propertyName, final Locale locale) {
		final boolean isDefault = equalsLanguage(locale, i18nContentSupport.getDefaultLocale());
		if (!isDefault) {
			return propertyName + "_" + locale.getLanguage();
		}
		return propertyName;
	}

	@Override
	public String getLocalizedNodeName(@Nullable final Node node, final String nodeName, final Locale locale) {
		return getLocalizedPropertyName(node, nodeName, locale);
	}

	private boolean equalsLanguage(@Nullable final Locale locale1, @Nullable final Locale locale2) {
		return Objects.equals(
				Optional.ofNullable(locale1).map(Locale::getLanguage),
				Optional.ofNullable(locale2).map(Locale::getLanguage)
		);
	}
}
