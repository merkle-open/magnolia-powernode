package com.namics.oss.magnolia.powernode;

import info.magnolia.cms.i18n.I18nContentSupport;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

public class LanguageLocalizedPropertyNameProvider implements LocalizedPropertyNameProvider {
	private final I18nContentSupport i18nContentSupport;

	@Inject
	public LanguageLocalizedPropertyNameProvider(final  I18nContentSupport i18nContentSupport) {
		this.i18nContentSupport = i18nContentSupport;
	}

	@Override
	public String getLocalized(final String propertyName, final Locale locale) {
		final boolean isDefault = equalsLanguage(locale, i18nContentSupport.getDefaultLocale());
		if (!isDefault) {
			return propertyName + "_" + locale.getLanguage();
		}
		return propertyName;
	}

	private boolean equalsLanguage(@Nullable final Locale locale1, @Nullable final Locale locale2) {
		return Objects.equals(
				Optional.ofNullable(locale1).map(Locale::getLanguage),
				Optional.ofNullable(locale2).map(Locale::getLanguage)
		);
	}
}
