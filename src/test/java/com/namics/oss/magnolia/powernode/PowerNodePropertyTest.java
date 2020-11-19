package com.namics.oss.magnolia.powernode;

import com.google.common.collect.ImmutableList;
import com.namics.oss.magnolia.powernode.exceptions.PowerNodeException;
import info.magnolia.jcr.predicate.StringPropertyValueFilterPredicate;
import info.magnolia.repository.RepositoryConstants;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class PowerNodePropertyTest extends AbstractPowerNodeTest {

	private static final String DEFAULT_TEST_NODE = "/blitzdings/de/property-test";
	private static final String INHERITANCE_TEST_NODE_01 = DEFAULT_TEST_NODE + "/property-test-child01";
	private static final String INHERITANCE_TEST_NODE_02 = DEFAULT_TEST_NODE + "/property-test-child01/property-test-child02";
	private static final String DEF_VAL = "defaultValue";

	private PowerNode getPowerNode(String nodePath) {
		Optional<Session> session = powerNodeService.getSystemSession(RepositoryConstants.WEBSITE);
		Assertions.assertTrue(session.isPresent());
		Optional<PowerNode> node = powerNodeService.getNodeByPath(nodePath, session.get());
		Assertions.assertTrue(node.isPresent());
		return node.get();
	}

	@Test
	public void getProperty_sunshine() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);
		Optional<Property> value = node.getPropertyOptional("string");
		Assertions.assertTrue(value.isPresent());

		Optional<Property> valueNotExisting = node.getPropertyOptional("string-nope");
		Assertions.assertFalse(valueNotExisting.isPresent());
	}

	@Test
	public void getPropertyValueDefault_sunshine() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);

		String value = node.getPropertyValue("string", DEF_VAL);
		Assertions.assertEquals("string-value", value);

		String empty = node.getPropertyValue("empty", DEF_VAL);
		Assertions.assertEquals(StringUtils.EMPTY, empty);

		LocalDateTime localDateTime = node.getPropertyValue("date", LocalDateTime.now());
		Assertions.assertEquals(1984, localDateTime.getYear());

		LocalDate localDate = node.getPropertyValue("date", LocalDate.now());
		Assertions.assertEquals(1984, localDate.getYear());

		Date date = node.getPropertyValue("date", new Date());
		Assertions.assertEquals(84, date.getYear());

		Calendar calendar = node.getPropertyValue("date", new GregorianCalendar());
		Assertions.assertEquals(1984, calendar.get(Calendar.YEAR));

		Long number = node.getPropertyValue("number", 0L);
		Assertions.assertEquals(Long.valueOf(12345), number);

		long numberPrimitive = node.getPropertyValue("number", 0L);
		Assertions.assertEquals(12345L, numberPrimitive);

		Boolean bool = node.getPropertyValue("boolean-flag", false);
		Assertions.assertEquals(true, bool);

		boolean boolPrimitive = node.getPropertyValue("boolean-flag", false);
		Assertions.assertTrue(boolPrimitive);
	}

	@Test
	public void getPropertyValueOptional_sunshine() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);

		Optional<String> value = node.getPropertyValue("string", String.class);
		Assertions.assertTrue(value.isPresent());
		Assertions.assertEquals("string-value", value.get());

		Optional<LocalDateTime> localDateTime = node.getPropertyValue("date", LocalDateTime.class);
		Assertions.assertTrue(localDateTime.isPresent());
		Assertions.assertEquals(1984, localDateTime.get().getYear());

		Optional<LocalDate> localDate = node.getPropertyValue("date", LocalDate.class);
		Assertions.assertTrue(localDate.isPresent());
		Assertions.assertEquals(1984, localDate.get().getYear());

		Optional<Date> date = node.getPropertyValue("date", Date.class);
		Assertions.assertTrue(date.isPresent());
		Assertions.assertEquals(84, date.get().getYear());

		Optional<Calendar> cal01 = node.getPropertyValue("date", GregorianCalendar.class);
		Assertions.assertTrue(cal01.isPresent());
		Assertions.assertEquals(1984, cal01.get().get(Calendar.YEAR));

		Optional<GregorianCalendar> cal02 = node.getPropertyValue("date", GregorianCalendar.class);
		Assertions.assertTrue(cal02.isPresent());
		Assertions.assertEquals(1984, cal02.get().get(Calendar.YEAR));

		Optional<Calendar> cal03 = node.getPropertyValue("date", Calendar.class);
		Assertions.assertTrue(cal03.isPresent());
		Assertions.assertEquals(1984, cal03.get().get(Calendar.YEAR));

		Optional<Long> number = node.getPropertyValue("number", Long.class);
		Assertions.assertTrue(number.isPresent());
		Assertions.assertEquals(Long.valueOf(12345), number.get());

		Optional<Boolean> bool = node.getPropertyValue("boolean-flag", Boolean.class);
		Assertions.assertTrue(bool.isPresent());
		Assertions.assertEquals(true, bool.get());
	}

	@Test
	public void getPropertyValueOptional_notExisting() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);

		Optional<String> value = node.getPropertyValue("string-nope", String.class);
		Assertions.assertFalse(value.isPresent());

		Optional<LocalDateTime> localDateTime = node.getPropertyValue("date-nope", LocalDateTime.class);
		Assertions.assertFalse(localDateTime.isPresent());

		Optional<LocalDate> localDate = node.getPropertyValue("date-nope", LocalDate.class);
		Assertions.assertFalse(localDate.isPresent());

		Optional<Date> date = node.getPropertyValue("date-nope", Date.class);
		Assertions.assertFalse(date.isPresent());

		Optional<Calendar> cal01 = node.getPropertyValue("date-nope", GregorianCalendar.class);
		Assertions.assertFalse(cal01.isPresent());

		Optional<GregorianCalendar> cal02 = node.getPropertyValue("date-nope", GregorianCalendar.class);
		Assertions.assertFalse(cal02.isPresent());

		Optional<Calendar> cal03 = node.getPropertyValue("date-nope", Calendar.class);
		Assertions.assertFalse(cal03.isPresent());

		Optional<Long> number = node.getPropertyValue("number-nope", Long.class);
		Assertions.assertFalse(number.isPresent());

		Optional<Boolean> bool = node.getPropertyValue("boolean-flag-nope", Boolean.class);
		Assertions.assertFalse(bool.isPresent());
	}

	@Test
	public void getPropertyValueDefault_notExisting() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);

		String value = node.getPropertyValue("string-nope", DEF_VAL);
		Assertions.assertEquals(DEF_VAL, value);

		LocalDateTime ldtDefault = LocalDateTime.now();
		LocalDateTime localDateTime = node.getPropertyValue("date-nope", ldtDefault);
		Assertions.assertEquals(ldtDefault, localDateTime);

		LocalDate ldDefault = LocalDate.now();
		LocalDate localDate = node.getPropertyValue("date-nope", ldDefault);
		Assertions.assertEquals(ldDefault, localDate);

		Date dateDefault = new Date();
		Date date = node.getPropertyValue("date-nope", dateDefault);
		Assertions.assertEquals(dateDefault, date);

		GregorianCalendar calDefault = new GregorianCalendar();
		Calendar calendar = node.getPropertyValue("date-nope", calDefault);
		Assertions.assertEquals(calDefault, calendar);

		Long number = node.getPropertyValue("number-nope", 0L);
		Assertions.assertEquals(Long.valueOf(0), number);

		long numberPrimitive = node.getPropertyValue("number-nope", 0L);
		Assertions.assertEquals(0L, numberPrimitive);

		Boolean bool = node.getPropertyValue("boolean-flag-nope", false);
		Assertions.assertEquals(false, bool);

		boolean boolPrimitive = node.getPropertyValue("boolean-flag-nope", false);
		Assertions.assertFalse(boolPrimitive);
	}

	@Test
	public void getPropertyValueDefault_wrongParams() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);
		String value01 = node.getPropertyValue(null, DEF_VAL);
		Assertions.assertEquals(DEF_VAL, value01);
	}

	@Test
	public void getPropertyInherit_sunshine() {
		PowerNode node01 = getPowerNode(INHERITANCE_TEST_NODE_01);
		// get value from node (no inheritance)
		String value = node01.getPropertyValue("string-inherit", DEF_VAL);
		// should not be present
		Assertions.assertEquals(DEF_VAL, value);

		// get value inherited
		String valueInherited = node01.getPropertyValueInherit("string-inherit", DEF_VAL);
		// should be present
		Assertions.assertEquals("string-value-parent", valueInherited);

		// property is present on an ancestor node
		PowerNode node02 = getPowerNode(DEFAULT_TEST_NODE);
		String parentValue = node02.getPropertyValueInherit("string-inherit", DEF_VAL);
		Assertions.assertEquals("string-value-parent", parentValue);

		// title property is present on descendant node
		PowerNode node03 = getPowerNode(INHERITANCE_TEST_NODE_02);
		String title = node03.getPropertyValueInherit("string-inherit", DEF_VAL);
		// property should not be inherited
		Assertions.assertEquals("string-value-child", title);
	}

	@Test
	public void getStringInherit_notExisting() {
		PowerNode node01 = getPowerNode(DEFAULT_TEST_NODE);
		String value = node01.getPropertyValueInherit("not-existing-anywhere", DEF_VAL);
		Assertions.assertEquals(DEF_VAL, value);
	}

	@Test
	public void getPropertyValueLocalized_sunshine() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);
		String german = node.getPropertyValueLocalized("localized", DEF_VAL, Locale.GERMAN);
		String french = node.getPropertyValueLocalized("localized", DEF_VAL, Locale.FRENCH);
		String italian = node.getPropertyValueLocalized("localized", DEF_VAL, Locale.ITALIAN);
		String english = node.getPropertyValueLocalized("localized", DEF_VAL, Locale.ENGLISH);

		Assertions.assertEquals("de-value", german);
		Assertions.assertEquals("fr-value", french);
		Assertions.assertEquals("it-value", italian);
		Assertions.assertEquals(DEF_VAL, english);
	}

	@Test
	public void getPropertyValueLocalized_notLocalized() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);
		String german = node.getPropertyValueLocalized("string", DEF_VAL, Locale.GERMAN);
		String french = node.getPropertyValueLocalized("string", DEF_VAL, Locale.FRENCH);
		String italian = node.getPropertyValueLocalized("string", DEF_VAL, Locale.ITALIAN);
		String english = node.getPropertyValueLocalized("string", DEF_VAL, Locale.ENGLISH);

		Assertions.assertEquals("string-value", german);
		Assertions.assertEquals(DEF_VAL, french);
		Assertions.assertEquals(DEF_VAL, italian);
		Assertions.assertEquals(DEF_VAL, english);
	}

	@Test
	public void getStringList_sunshine() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);
		List<String> stringList = node.getPropertyValueList("string-list", String.class);

		Assertions.assertEquals(3, stringList.size());
		Assertions.assertEquals("value00", stringList.get(0));
		Assertions.assertEquals("value01", stringList.get(1));
		Assertions.assertEquals("value02", stringList.get(2));
	}

	@Test
	public void getStringList_notAList() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);
		List<String> stringList = node.getPropertyValueList("string", String.class);

		Assertions.assertEquals(1, stringList.size());
		Assertions.assertEquals("string-value", stringList.get(0));
	}

	@Test
	public void getPropertyValueWithFallback_sunshine() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);
		// primary property exists
		String primary = node.getPropertyValueWithFallback("string", DEF_VAL, "string-inherit", "string-list");
		Assertions.assertEquals("string-value", primary);

		// primary property does not exist, first fallback
		String fallback01 = node.getPropertyValueWithFallback("nope-02", DEF_VAL, "string", "string-list");
		Assertions.assertEquals("string-value", fallback01);

		// primary property and fallback01 does not exist, second fallback
		String fallback02 = node.getPropertyValueWithFallback("nope-02", DEF_VAL, "nope-01", "string");
		Assertions.assertEquals("string-value", fallback02);

		// nothing exists, default value
		String fallback03 = node.getPropertyValueWithFallback("nope-02", DEF_VAL, "nope-01", "nope-00");
		Assertions.assertEquals(DEF_VAL, fallback03);
	}

	@Test
	public void getPropertyValueWithFallbackOptional_sunshine() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);
		// primary property exists
		Optional<String> primary = node.getPropertyValueWithFallback("string", String.class, "string-inherit", "string-list");
		Assertions.assertTrue(primary.isPresent());
		Assertions.assertEquals("string-value", primary.get());

		// primary property does not exist, first fallback
		Optional<String> fallback01 = node.getPropertyValueWithFallback("nope-02", String.class, "string", "string-list");
		Assertions.assertTrue(fallback01.isPresent());
		Assertions.assertEquals("string-value", fallback01.get());

		// primary property and fallback01 does not exist, second fallback
		Optional<String> fallback02 = node.getPropertyValueWithFallback("nope-02", String.class, "nope-01", "string");
		Assertions.assertTrue(fallback02.isPresent());
		Assertions.assertEquals("string-value", fallback02.get());

		// nothing exists, default value
		Optional<String> fallback03 = node.getPropertyValueWithFallback("nope-02", String.class, "nope-01", "nope-00");
		Assertions.assertFalse(fallback03.isPresent());
	}

	@Test
	public void getPropertyValue_wrongType() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);

		Optional<LocalDateTime> wrongType = node.getPropertyValue("boolean-flag", LocalDateTime.class);
		Assertions.assertFalse(wrongType.isPresent());
	}

	@Test
	public void allStringsAreRichText_sunshine() throws RepositoryException {
		PowerNode textImage = getPowerNode("/blitzdings/de/ocm-test/ContentArea/0/ContentArea/0");

		String rawLink = "<a href=\"${link:{uuid:{6cac875e-1eaf-4cab-9642-bcd47e40215a},repository:{website},path:{/root/de/lang-support-test}}}\">";
		String richTextLink = "<a href=\"/blitzdings/de/lang-support-test\">";

		// get raw property value
		String rawString = textImage.getProperty("text").getString();
		// raw text contains the link uuid pattern
		Assertions.assertTrue(rawString.contains(rawLink));
		// raw does not contain the converted link
		Assertions.assertFalse(rawString.contains(richTextLink));

		// get property via helper
		String richText = textImage.getPropertyValue("text", StringUtils.EMPTY);
		// rich text does not contain the link uuid pattern
		Assertions.assertFalse(richText.contains(rawLink));
		// rich text contains the converted link
		Assertions.assertTrue(richText.contains(richTextLink));
	}

	@Test
	public void removeProperty_sunshine() throws RepositoryException {
		PowerNode node01 = getPowerNode(DEFAULT_TEST_NODE);

		// property is present
		Assertions.assertTrue(node01.hasProperty("string"));
		// remove property
		node01.removeProperty("string");
		// property is gone
		Assertions.assertFalse(node01.hasProperty("string"));

		// get node in new session
		PowerNode node02 = getPowerNode(DEFAULT_TEST_NODE);
		// property is present again
		Assertions.assertTrue(node02.hasProperty("string"));
		// remove property
		node02.removeProperty("string");
		// property is gone
		Assertions.assertFalse(node02.hasProperty("string"));
		// save session
		node02.getSession().save();

		// get node in another session
		PowerNode node03 = getPowerNode(DEFAULT_TEST_NODE);
		// property is still gone
		Assertions.assertFalse(node03.hasProperty("string"));
	}

	@Test
	public void removeProperty_notExisting() {
		PowerNode node01 = getPowerNode(DEFAULT_TEST_NODE);

		// property is present
		Assertions.assertFalse(node01.hasProperty("string-nope"));
		// remove property, does not fail
		node01.removeProperty("string-nope");
	}

	@Test
	public void setProperty_sunshine() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);

		long propCount = node.getProperties().getSize();

		node.setPropertyValue("new-string", "new-string-value");
		Optional<String> newString = node.getPropertyValue("new-string", String.class);
		Assertions.assertTrue(newString.isPresent());
		Assertions.assertEquals("new-string-value", newString.get());

		Assertions.assertEquals(++propCount, node.getProperties().getSize());

		node.setPropertyValue("boolean-true", true);
		Optional<Boolean> newBool = node.getPropertyValue("boolean-true", Boolean.class);
		Assertions.assertTrue(newBool.isPresent());
		Assertions.assertEquals(true, newBool.get());

		Assertions.assertEquals(++propCount, node.getProperties().getSize());

		node.setPropertyValue("new-long", 123L);
		Optional<Long> newLong = node.getPropertyValue("new-long", Long.class);
		Assertions.assertTrue(newLong.isPresent());
		Assertions.assertEquals(Long.valueOf(123), newLong.get());

		Assertions.assertEquals(++propCount, node.getProperties().getSize());

		Calendar calendarNow = new GregorianCalendar();
		node.setPropertyValue("calendar-01", calendarNow);
		Optional<Calendar> newCal = node.getPropertyValue("calendar-01", GregorianCalendar.class);
		Assertions.assertTrue(newCal.isPresent());
		Assertions.assertEquals(calendarNow.getTime(), newCal.get().getTime());

		Assertions.assertEquals(++propCount, node.getProperties().getSize());

		Date dateNow = new Date();
		node.setPropertyValue("calendar-02", dateNow);
		Optional<Date> newDate = node.getPropertyValue("calendar-02", Date.class);
		Assertions.assertTrue(newDate.isPresent());
		Assertions.assertEquals(dateNow, newDate.get());

		Assertions.assertEquals(++propCount, node.getProperties().getSize());

		// Since Java 11 now() returns nanos which is not supported by the date object the repository is using internally
		LocalDateTime localDateTimeNow = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);
		node.setPropertyValue("calendar-03", localDateTimeNow);
		Optional<LocalDateTime> newLocalDateTime = node.getPropertyValue("calendar-03", LocalDateTime.class);
		Assertions.assertTrue(newLocalDateTime.isPresent());
		Assertions.assertEquals(localDateTimeNow, newLocalDateTime.get());

		Assertions.assertEquals(++propCount, node.getProperties().getSize());

		LocalDate localDateNow = LocalDate.now();
		node.setPropertyValue("calendar-04", localDateNow);
		Optional<LocalDate> newLocalDate = node.getPropertyValue("calendar-04", LocalDate.class);
		Assertions.assertTrue(newLocalDate.isPresent());
		Assertions.assertEquals(localDateNow, newLocalDate.get());

		Assertions.assertEquals(++propCount, node.getProperties().getSize());

		node.setPropertyValue("new-bigDecimal", BigDecimal.valueOf(123.456));
		Optional<BigDecimal> newBigDecimal = node.getPropertyValue("new-bigDecimal", BigDecimal.class);
		Assertions.assertTrue(newBigDecimal.isPresent());
		Assertions.assertEquals(BigDecimal.valueOf(123.456), newBigDecimal.get());

		Assertions.assertEquals(++propCount, node.getProperties().getSize());

		ImmutableList<String> stringList = ImmutableList.of("value01", "value02", "value03", "value04");
		node.setPropertyValue("new-stringList", stringList);
		List<String> newStringList = node.getPropertyValueList("new-stringList", String.class);
		Assertions.assertFalse(newStringList.isEmpty());
		Assertions.assertEquals(4, newStringList.size());
		Assertions.assertEquals("value03", newStringList.get(2));

		Assertions.assertEquals(++propCount, node.getProperties().getSize());

		ImmutableList<Integer> integerList = ImmutableList.of(1, 2, 3, 4);
		node.setPropertyValue("new-integerList", integerList);
		List<Long> newIntegerList = node.getPropertyValueList("new-integerList", Long.class);
		Assertions.assertFalse(newIntegerList.isEmpty());
		Assertions.assertEquals(4, newIntegerList.size());
		Assertions.assertEquals(Long.valueOf(3), newIntegerList.get(2));

		Assertions.assertEquals(++propCount, node.getProperties().getSize());
	}

	@Test
	public void setProperty_unsupportedType() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);
		PowerNodeException e = Assertions.assertThrows(
				PowerNodeException.class,
				() -> {
					UUID uuid = new UUID(1, 5);
					node.setPropertyValue("notSupportedType", uuid);
				});

		Assertions.assertEquals(PowerNodeException.Type.DEFAULT, e.getType());
		Assertions.assertEquals(PowerNodeException.class, e.getCause().getCause().getClass());
		Assertions.assertEquals("Value type not supported: 'java.util.UUID' (Type: JCR_REPOSITORY)", e.getCause().getCause().getMessage());
	}

	@Test
	public void setPropertyIfNotPreset_sunshine() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);

		// set new property
		node.setPropertyValue("new-string", "new-string-value");
		Optional<String> newString = node.getPropertyValue("new-string", String.class);
		Assertions.assertTrue(newString.isPresent());
		// property is present
		Assertions.assertEquals("new-string-value", newString.get());

		// overwrite new property
		node.setPropertyValue("new-string", "overwritten-string-value");
		Optional<String> overwrittenString = node.getPropertyValue("new-string", String.class);
		Assertions.assertTrue(overwrittenString.isPresent());
		// property has new value now
		Assertions.assertEquals("overwritten-string-value", overwrittenString.get());

		// set property only if not present
		node.setPropertyValueIfNotPresent("new-string", "should-not-be-written");
		Optional<String> notWritten = node.getPropertyValue("new-string", String.class);
		Assertions.assertTrue(notWritten.isPresent());
		// property not changed, since it was already present
		Assertions.assertEquals("overwritten-string-value", notWritten.get());

		// set property only if not present
		node.setPropertyValueIfNotPresent("newer-string", "very-new-string");
		Optional<String> newerString = node.getPropertyValue("newer-string", String.class);
		Assertions.assertTrue(newerString.isPresent());
		// property is there, since it was not already present
		Assertions.assertEquals("very-new-string", newerString.get());
	}

	@Test
	public void hasProperty_emptyString() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);
		node.setProperty("new-prop", "new-value");
		boolean hasNewProp = node.hasProperty("new-prop");
		Assertions.assertTrue(hasNewProp);

		boolean hasProp = node.hasProperty("not-existing");
		Assertions.assertFalse(hasProp);

		boolean hasEmpty = node.hasProperty("");
		Assertions.assertFalse(hasEmpty);

		boolean hasNull = node.hasProperty(null);
		Assertions.assertFalse(hasNull);
	}

	@Test
	public void getPropertyMap_sunshine() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);
		node.setPropertyValue("new-value-long", 123L);
		node.setPropertyValue("new-value-list", List.of("a", "b", "c"));

		Map<String, Object> propertyMap = node.getPropertyMap();
		Assertions.assertFalse(propertyMap.isEmpty());

		Assertions.assertTrue(propertyMap.containsKey("new-value-long"));
		Assertions.assertTrue(propertyMap.get("new-value-long") instanceof Long);
		Assertions.assertEquals(123L, propertyMap.get("new-value-long"));

		Assertions.assertTrue(propertyMap.containsKey("new-value-list"));
		Assertions.assertTrue(propertyMap.get("new-value-list") instanceof List);
		Assertions.assertEquals("a", ((List) propertyMap.get("new-value-list")).get(0));
	}

	@Test
	public void getPropertyMapFilterByType_sunshine() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);
		node.setPropertyValue("new-value-long", 123L);
		node.setPropertyValue("new-string-list", List.of("a", "b", "c"));
		node.setPropertyValue("new-long-list", List.of(1L, 2L, 3L));

		Map<String, String> propertyMap = node.getPropertyMap(String.class);
		Assertions.assertFalse(propertyMap.isEmpty());

		// filtering by collections is not really useful
		Map<String, List> propertyMapList = node.getPropertyMap(List.class);
		Assertions.assertFalse(propertyMapList.isEmpty());
	}

	@Test
	public void getPropertyMapFilterWithPredicate_sunshine() {
		PowerNode node = getPowerNode(DEFAULT_TEST_NODE);
		node.setPropertyValue("new-value-string", "please filter me!");

		Map<String, Object> propertyMap = node.getPropertyMap(new StringPropertyValueFilterPredicate("please filter me!"));

		Assertions.assertFalse(propertyMap.isEmpty());
		Assertions.assertTrue(propertyMap.containsKey("new-value-string"));
		Assertions.assertEquals("please filter me!", propertyMap.get("new-value-string"));
	}

}
