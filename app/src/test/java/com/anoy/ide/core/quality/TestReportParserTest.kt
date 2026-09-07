package com.anoy.ide.core.quality

import org.junit.Assert.assertEquals
import org.junit.Test

class TestReportParserTest {

    @Test
    fun parsesSingleSuite() {
        val xml = """
            <testsuite name="com.example.FooTest" tests="4" failures="1" errors="0" skipped="1" time="0.25">
            </testsuite>
        """.trimIndent()
        val result = TestReportParser.parse(xml)
        assertEquals("com.example.FooTest", result.name)
        assertEquals(4, result.tests)
        assertEquals(1, result.failures)
        assertEquals(1, result.skipped)
        assertEquals(2, result.passed)
        assertEquals(50.0, result.passedPercent, 0.001)
    }

    @Test
    fun parsesMultipleSuites() {
        val xml = """
            <testsuites>
                <testsuite name="A" tests="3" failures="0" errors="0" skipped="0" time="1.0"></testsuite>
                <testsuite name="B" tests="2" failures="1" errors="0" skipped="0" time="2.0"></testsuite>
            </testsuites>
        """.trimIndent()
        val result = TestReportParser.parseAll(xml)
        assertEquals(2, result.size)
        assertEquals(3, result[0].tests)
        assertEquals(1, result[1].failures)
    }
}
