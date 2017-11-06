package me.fru1t.worddropper.settings

import com.google.common.truth.Truth.assertThat
import me.fru1t.worddropper.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

@RunWith(JUnit4::class)
class PreferenceManagerTest {
    companion object {
        private val TEST_VALUE = "some value"
    }

    private lateinit var spyPref: FakePreferenceManager

    @Before
    fun setUp() {
        spyPref = spy(FakePreferenceManager())
        spyPref.sideload(R.string.pref_test, TEST_VALUE)
    }

    @Test
    fun getString() {
        spyPref.getString(R.string.pref_test, TEST_VALUE)
        verify(spyPref, times(1)).getString(R.string.pref_test, TEST_VALUE)
    }

    @Test
    fun applyString() {
        spyPref.applyString(R.string.pref_test, TEST_VALUE)
        verify(spyPref, times(1)).applyString(R.string.pref_test, TEST_VALUE)
    }

    @Test
    fun addChangeListener() {
        var calls = 0

        spyPref.addChangeListener { calls++ }
        spyPref.applyString(R.string.pref_test, TEST_VALUE)
        assertThat(calls).isEqualTo(1)
    }

    @Test
    fun removeChangeListener() {
        var calls = 0
        val listener: (String) -> Unit = { calls++ }
        spyPref.addChangeListener(listener)
        spyPref.removeChangeListener(listener)
        spyPref.applyString(R.string.pref_test, TEST_VALUE)
        assertThat(calls).isEqualTo(0)
    }
}
