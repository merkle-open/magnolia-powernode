package com.merkle.oss.magnolia.powernode;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.module.site.SiteManager;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

import javax.annotation.Nullable;
import javax.jcr.Node;

public abstract class AbstractLocalizedNameProvider implements LocalizedNameProvider {
	private final Function<Optional<Node>, I18nContentSupport> i18nContentSupportProvider;

	protected AbstractLocalizedNameProvider(final SiteManager siteManager) {
		this.i18nContentSupportProvider = (node) -> node.map(siteManager::getAssignedSite).orElseGet(siteManager::getCurrentSite).getI18n();
	}

	protected AbstractLocalizedNameProvider(final Function<Optional<Node>, I18nContentSupport> i18nContentSupportProvider) {
		this.i18nContentSupportProvider = i18nContentSupportProvider;
	}

	@Override
	public String getLocalizedPropertyName(@Nullable final Node node, final String propertyName, final Locale locale) {
		final I18nContentSupport i18nContentSupport = i18nContentSupportProvider.apply(Optional.ofNullable(node));
		if (!isDefaultLocale(i18nContentSupport.getDefaultLocale(), locale)) {
			return appendLocaleSuffix(propertyName, locale);
		}
		return propertyName;
	}

	@Override
	public String getLocalizedNodeName(@Nullable final Node node, final String nodeName, final Locale locale) {
		return getLocalizedPropertyName(node, nodeName, locale);
	}

	protected abstract boolean isDefaultLocale(Locale defaultLocale, Locale locale);
	protected abstract String appendLocaleSuffix(String propertyName, Locale locale);
}
