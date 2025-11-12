package com.merkle.oss.magnolia.powernode;

import jakarta.annotation.Nullable;
import javax.jcr.Node;
import java.util.Locale;

public interface LocalizedNameProvider {
	String getLocalizedPropertyName(@Nullable Node node, String propertyName, Locale locale);
	String getLocalizedNodeName(@Nullable Node node, String nodeName, Locale locale);
}
