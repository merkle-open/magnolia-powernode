package com.namics.oss.magnolia.powernode;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;

import javax.inject.Inject;
import java.util.Locale;
import java.util.Objects;

public class DefaultLocalizedPropertyNameProvider implements LocalizedPropertyNameProvider {
	private final I18nContentSupport i18nContentSupport;
	private final I18NAuthoringSupport i18NAuthoringSupport;

	@Inject
	public DefaultLocalizedPropertyNameProvider(
			final I18nContentSupport i18nContentSupport,
			final I18NAuthoringSupport i18NAuthoringSupport) {
		this.i18nContentSupport = i18nContentSupport;
		this.i18NAuthoringSupport = i18NAuthoringSupport;
	}

	@Override
	public String getLocalized(final String propertyName, final Locale locale) {
		final boolean isDefault = Objects.equals(locale, i18nContentSupport.getDefaultLocale());
		if (!isDefault) {
			return i18NAuthoringSupport.deriveLocalisedPropertyName(propertyName, locale);
		}
		return propertyName;
	}
}
