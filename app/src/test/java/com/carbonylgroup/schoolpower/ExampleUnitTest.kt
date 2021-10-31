package com.carbonylgroup.schoolpower

import com.carbonylgroup.schoolpower.utils.Utils
import org.junit.Test

import org.assertj.core.api.Assertions.assertThat

class UtilsTest {
    @Test
    @Throws(Exception::class)
    fun `sort terms works`() {
        val terms = arrayListOf("L1", "F2", "E1", "F1", "S1", "F3", "E2", "F4", "L2", "X2")
        assertThat(Utils.sortTerm(terms))
            .containsExactlyElementsOf(mutableListOf("F4", "F3", "F2", "L2", "E2", "X2", "F1", "L1", "E1", "S1"))
        assertThat(Utils.sortTermsByLatest(terms.toSet()))
            .containsExactlyElementsOf(mutableListOf("F4", "F3", "F2", "F1", "L2", "L1", "E2", "E1", "S1", "X2"))
    }
}