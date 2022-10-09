package com.auto.accessibility;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.deque.html.axecore.results.Results;
import com.deque.html.axecore.results.Rule;
import com.deque.html.axecore.selenium.AxeBuilder;
import com.deque.html.axecore.selenium.AxeReporter;

import io.github.bonigarcia.wdm.WebDriverManager;

public class TestAccessibility {

	WebDriver driver;
	private static final String shadowErrorPage = "src/test/resources/html/shadow-error.html";
	private static final String includeExcludePage = "src/test/resources/html/include-exclude.html";
	private static final String normalPage = "src/test/resources/html/normal.html";
	private static final String nestedIframePage = "src/test/resources/html/nested-iframes.html";
	private static final String violationPage = "src/test/resources/html/violation.html";

	/** Instantiate the WebDriver */
	@BeforeMethod
	public void setUp() {
		WebDriverManager.chromedriver().setup();
		driver = new ChromeDriver();
	}

	/** Basic test */
	@Test
	public void testAccessibility() {
		driver.get("file:///" + new File(normalPage).getAbsolutePath());
		AxeBuilder builder = new AxeBuilder();
		Results results = builder.analyze(driver);
		List<Rule> violations = results.getViolations();
		AxeReporter.writeResultsToJsonFile("testAccessibility", results);
		Assert.assertEquals(violations.size(), 0, "No violations found");
	}

	/** Test with frames/ iframes */
	@Test
	public void testAccessibilityWithFrames() {
		driver.get("file:///" + new File(nestedIframePage).getAbsolutePath());
		AxeBuilder builder = new AxeBuilder();
		Results results = builder.withOnlyRules(Arrays.asList("frame-title")).analyze(driver);
		List<Rule> violations = results.getViolations();
		AxeReporter.writeResultsToJsonFile("testAccessibilityWithFrames", results);
		Assert.assertEquals(violations.size(), 1, "'frame-title' passed");
		Assert.assertEquals(violations.get(0).getNodes().size(), 3, "3 nodes found");
	}

	/** Test with options and violations */
	@Test
	public void testAccessibilityWithOptionsAndViolations() {
		driver.get("file:///" + new File(violationPage).getAbsolutePath());
		AxeBuilder builder = new AxeBuilder();
		builder.setOptions("{ \"rules\": { \"object-alt\": { \"enabled\": false } } }");
		Results results = builder.analyze(driver);
		List<Rule> violations = results.getViolations();
		AxeReporter.writeResultsToJsonFile("testAccessibilityWithOptionsAndViolations", results);
		Assert.assertEquals(violations.size(), 1, "violations found");
	}

	/** Test with options */
	@Test
	public void testAccessibilityWithOptions() {
		driver.get("file:///" + new File(violationPage).getAbsolutePath());
		AxeBuilder builder = new AxeBuilder();
		builder.setOptions("{ \"rules\": { \"image-alt\": { \"enabled\": false } } }");
		Results results = builder.analyze(driver);
		List<Rule> violations = results.getViolations();
		AxeReporter.writeResultsToJsonFile("testAccessibilityWithOptions", results);
		Assert.assertEquals(violations.size(), 0, "No violations found");
	}

	/** Test a specific selector or selectors (include, exclude) */
	@Test
	public void testAccessibilityWithSelectors() {
		driver.get("file:///" + new File(normalPage).getAbsolutePath());
		Results result = new AxeBuilder().include(Arrays.asList("title")).exclude(Collections.singletonList("p"))
				.analyze(driver);
		List<Rule> violations = result.getViolations();
		Assert.assertEquals(violations.size(), 0, "No violations found");
		AxeReporter.writeResultsToJsonFile("testAccessibilityWithSelectors", result);
	}

	/** Test with WebElement/'s */
	@Test
	public void testAccessibilityWithWebElement() {
		driver.get("file:///" + new File(includeExcludePage).getAbsolutePath());
		AxeBuilder builder = new AxeBuilder();
		Results results = builder.analyze(driver, driver.findElement(By.tagName("p")),
				driver.findElement(By.tagName("h1")));
		List<Rule> violations = results.getViolations();
		AxeReporter.writeResultsToJsonFile("testAccessibilityWithWebElement", results);
		Assert.assertEquals(violations.size(), 1, "violations found");
	}

	/** Test a page with Shadow DOM violations */
	@Test
	public void testAccessibilityWithShadowElement() {
		driver.get("file:///" + new File(shadowErrorPage).getAbsolutePath());
		Results results = new AxeBuilder().analyze(driver);
		List<Rule> violations = results.getViolations();
		AxeReporter.writeResultsToJsonFile("testAccessibilityWithShadowElement", results);
		Assert.assertEquals(violations.size(), 1, "violations found");
	}

	/** Ensure we close the WebDriver after finishing */
	@AfterMethod
	public void tearDown() {
		driver.quit();
	}
}
