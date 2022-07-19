package com.namics.oss.magnolia.powernode;

import info.magnolia.cms.i18n.I18nContentSupport;

import javax.inject.Inject;
import java.util.Locale;
import java.util.Objects;

public class DefaultLocalizedPropertyNameProvider implements LocalizedPropertyNameProvider {
	private final I18nContentSupport i18nContentSupport;

	@Inject
	public DefaultLocalizedPropertyNameProvider(final I18nContentSupport i18nContentSupport) {
		this.i18nContentSupport = i18nContentSupport;
	}

	@Override
	public String getLocalized(final String propertyName, final Locale locale) {
		final boolean isDefault = Objects.equals(locale, i18nContentSupport.getDefaultLocale());
		if (!isDefault) {
			return propertyName + "_" + locale; // same as AbstractI18nContentSupport.getProperty(Node node, String name, Locale locale)
		}
		return propertyName;
	}
}
