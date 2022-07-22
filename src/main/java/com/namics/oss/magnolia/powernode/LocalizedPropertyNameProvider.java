package com.namics.oss.magnolia.powernode;

import javax.annotation.Nullable;
import javax.jcr.Node;
import java.util.Locale;

public interface LocalizedPropertyNameProvider {
	String getLocalized(@Nullable Node node, String propertyName, Locale locale);
}
