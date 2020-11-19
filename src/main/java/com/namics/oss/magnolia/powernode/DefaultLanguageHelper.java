package com.namics.oss.magnolia.powernode;

import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.objectfactory.Components;

import java.util.Locale;

public class DefaultLanguageHelper {

	private static final I18nContentSupport I18N_CONTENT_SUPPORT = Components.getComponent(I18nContentSupport.class);

	public Locale getDefaultLanguage() {
		return I18N_CONTENT_SUPPORT.getDefaultLocale();
	}
}
