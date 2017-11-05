package me.fru1t.worddropper.settings

import android.content.Context
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations.initMocks

@RunWith(JUnit4::class)
class PreferenceManagerTest {
    companion object {
        private val TEST_RESOURCE_KEY = "test resource key"
        private val TEST_VALUE = "some value"
    }

    @Mock private lateinit var context: Context

    private lateinit var spyPref: PreferenceManager

    @Before
    fun setUp() {
        initMocks(this)
        `when`(context.getString(anyInt())).thenReturn(TEST_RESOURCE_KEY)

        spyPref = spy(FakePreferenceManager(context))
    }

    @Test
    fun getString() {
        spyPref.getString(1, TEST_VALUE)
        verify(spyPref, times(1)).getString(TEST_RESOURCE_KEY, TEST_VALUE)
    }

    @Test
    fun applyString() {
        spyPref.applyString(1, TEST_VALUE)
        verify(spyPref, times(1)).applyString(TEST_RESOURCE_KEY, TEST_VALUE)
    }

    @Test
    fun addChangeListener() {
        var calls = 0
        spyPref.addChangeListener { calls++ }
        spyPref.applyString(1, TEST_VALUE)
        assertThat(calls).isEqualTo(1)
    }

    @Test
    fun removeChangeListener() {
        var calls = 0
        val listener: (String) -> Unit = { calls++ }
        spyPref.addChangeListener(listener)
        spyPref.removeChangeListener(listener)
        spyPref.applyString(1, TEST_VALUE)
        assertThat(calls).isEqualTo(0)
    }
}

