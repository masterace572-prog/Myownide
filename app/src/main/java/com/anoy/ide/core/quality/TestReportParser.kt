package com.anoy.ide.core.quality

import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory

data class TestSuiteResult(
    val name: String,
    val tests: Int,
    val failures: Int,
    val errors: Int,
    val skipped: Int,
    val durationSeconds: Double
) {
    val passed: Int get() = tests - failures - errors - skipped
    val passedPercent: Double
        get() = if (tests == 0) 0.0 else passed * 100.0 / tests
}

/**
 * Parses JUnit XML reports emitted by Gradle
 * (`build/test-results/testDebugUnitTest/*.xml`). Standard parser used so the
 * unit-test explorer can render a project's results without a device.
 */
object TestReportParser {

    fun parse(xml: String): TestSuiteResult {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = false
        val builder = factory.newDocumentBuilder()
        val document = builder.parse(ByteArrayInputStream(xml.toByteArray(Charsets.UTF_8)))
        val suite = document.documentElement

        val attr = { name: String, fallback: String -> suite.getAttribute(name).ifBlank { fallback } }
        return TestSuiteResult(
            name = attr("name", "Tests"),
            tests = attr("tests", "0").toIntOrNull() ?: 0,
            failures = attr("failures", "0").toIntOrNull() ?: 0,
            errors = attr("errors", "0").toIntOrNull() ?: 0,
            skipped = attr("skipped", "0").toIntOrNull() ?: 0,
            durationSeconds = attr("time", "0").toDoubleOrNull() ?: 0.0
        )
    }

    fun parseAll(xml: String): List<TestSuiteResult> {
        val factory = DocumentBuilderFactory.newInstance()
        factory.isNamespaceAware = false
        val builder = factory.newDocumentBuilder()
        val document = builder.parse(ByteArrayInputStream(xml.toByteArray(Charsets.UTF_8)))
        val nodes = document.documentElement.getElementsByTagName("testsuite")
        return (0 until nodes.length).mapNotNull { index ->
            val suite = nodes.item(index)
            val element = suite as? org.w3c.dom.Element ?: return@mapNotNull null
            runCatching {
                TestSuiteResult(
                    name = element.getAttribute("name").ifBlank { "Tests" },
                    tests = element.getAttribute("tests").toIntOrNull() ?: 0,
                    failures = element.getAttribute("failures").toIntOrNull() ?: 0,
                    errors = element.getAttribute("errors").toIntOrNull() ?: 0,
                    skipped = element.getAttribute("skipped").toIntOrNull() ?: 0,
                    durationSeconds = element.getAttribute("time").toDoubleOrNull() ?: 0.0
                )
            }.getOrNull()
        }
    }
}
