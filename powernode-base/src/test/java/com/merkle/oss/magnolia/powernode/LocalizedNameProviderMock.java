package com.merkle.oss.magnolia.powernode;

import javax.annotation.Nullable;
import javax.jcr.Node;
import java.util.Locale;

class LocalizedNameProviderMock implements LocalizedNameProvider {
	@Override
	public String getLocalizedPropertyName(@Nullable final Node node, final String propertyName, final Locale locale) {
		return propertyName + "_" + locale.toLanguageTag();
	}

	@Override
	public String getLocalizedNodeName(@Nullable final Node node, final String nodeName, final Locale locale) {
		return getLocalizedPropertyName(node, nodeName, locale);
	}
}
