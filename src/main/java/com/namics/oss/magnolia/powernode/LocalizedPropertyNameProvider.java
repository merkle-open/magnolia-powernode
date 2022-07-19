package com.namics.oss.magnolia.powernode;

import java.util.Locale;

public interface LocalizedPropertyNameProvider {
	String getLocalized(String propertyName, Locale locale);
}
