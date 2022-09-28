package com.namics.oss.magnolia.powernode;

import info.magnolia.cms.i18n.I18nContentSupport;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.jcr.Node;
import java.util.Locale;
import java.util.Objects;

public class DefaultLocalizedNameProvider implements LocalizedNameProvider {
	private final I18nContentSupport i18nContentSupport;

	@Inject
	public DefaultLocalizedNameProvider(final I18nContentSupport i18nContentSupport) {
		this.i18nContentSupport = i18nContentSupport;
	}

	@Override
	public String getLocalizedPropertyName(@Nullable final Node node, final String propertyName, final Locale locale) {
		final boolean isDefault = Objects.equals(locale, i18nContentSupport.getDefaultLocale());
		if (!isDefault) {
			return String.format("%s_%s", propertyName, locale.toString());
		}
		return propertyName;
	}

	@Override
	public String getLocalizedNodeName(@Nullable final Node node, final String nodeName, final Locale locale) {
		return getLocalizedPropertyName(node, nodeName, locale);
	}
}
