package com.merkle.oss.magnolia.powernode.mock;

import com.merkle.oss.magnolia.powernode.LocalizedNameProvider;

import jakarta.annotation.Nullable;
import javax.jcr.Node;
import java.util.Locale;

public class LocalizedNameProviderMock implements LocalizedNameProvider {
	@Override
	public String getLocalizedPropertyName(@Nullable final Node node, final String propertyName, final Locale locale) {
		return propertyName + "_" + locale.toLanguageTag();
	}

	@Override
	public String getLocalizedNodeName(@Nullable final Node node, final String nodeName, final Locale locale) {
		return getLocalizedPropertyName(node, nodeName, locale);
	}
}
