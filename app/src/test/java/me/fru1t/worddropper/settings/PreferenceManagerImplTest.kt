package me.fru1t.worddropper.settings

import android.content.Context
import android.content.SharedPreferences
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations.initMocks

@RunWith(JUnit4::class)
class PreferenceManagerImplTest {
    companion object {
        val TEST_PREFERENCES_FILE_NAME = "test"
        val TEST_PREFERENCE_RETURN_STRING = "test value returned by the preferences"
        val TEST_PREFERENCE_KEY = "preference key"
        val TEST_VALUE = "test value"
    }

    @Mock private lateinit var mockContext: Context
    private lateinit var spySharedPreferences: SharedPreferences
    private lateinit var spyEditor: SharedPreferences.Editor

    private lateinit var preferences: PreferenceManagerImpl

    @Before
    fun setUp() {
        initMocks(this)

        spySharedPreferences = spy(mock(SharedPreferences::class.java))
        spyEditor = spy(SharedPreferences.Editor::class.java)

        `when`(spyEditor.putString(anyString(), anyString())).thenReturn(spyEditor)
        `when`(spySharedPreferences.getString(anyString(), anyString()))
                .thenReturn(TEST_PREFERENCE_RETURN_STRING)
        `when`(spySharedPreferences.edit()).thenReturn(spyEditor)
        `when`(mockContext.getString(anyInt())).thenReturn(TEST_PREFERENCE_KEY)
        `when`(mockContext.getSharedPreferences(anyString(), anyInt()))
                .thenReturn(spySharedPreferences)

        preferences = PreferenceManagerImpl(TEST_PREFERENCES_FILE_NAME, mockContext)
    }

    @Test
    fun getString() {
        preferences.getString(1, TEST_VALUE)
        verify(spySharedPreferences, times(1)).getString(TEST_PREFERENCE_KEY, TEST_VALUE)
    }

    @Test
    fun applyString() {
        preferences.applyString(1, TEST_VALUE)
        verify(spySharedPreferences, times(1)).edit()
        verify(spyEditor, times(1)).putString(TEST_PREFERENCE_KEY, TEST_VALUE)
        verify(spyEditor, times(1)).apply()
    }
}
