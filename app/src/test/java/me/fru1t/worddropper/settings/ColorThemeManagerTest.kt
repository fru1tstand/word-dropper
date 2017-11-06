package me.fru1t.worddropper.settings

import android.content.Context
import android.view.View
import com.google.common.truth.Truth.assertThat
import me.fru1t.worddropper.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations.initMocks

@RunWith(JUnit4::class)
class ColorThemeManagerTest {
    @Mock private lateinit var mockContext: Context
    private lateinit var spyPref: FakePreferenceManager
    private lateinit var manager: ColorThemeManager

    @Before
    fun setUp() {
        initMocks(this)
        spyPref = spy(FakePreferenceManager())
        spyPref.sideload(R.string.pref_colorTheme, ColorTheme.INVERSE_ORANGE.name)
        manager = ColorThemeManager(spyPref, mockContext)
    }

    @Test
    fun setColorTheme() {
        manager.setColorTheme(ColorTheme.ORANGE)
        assertThat(manager.currentColorTheme).isEqualTo(ColorTheme.ORANGE)
        verify(spyPref, times(1))
                .applyString(R.string.pref_colorTheme, ColorTheme.ORANGE.name)

        manager.setColorTheme(ColorTheme.PURPLE)
        assertThat(manager.currentColorTheme).isEqualTo(ColorTheme.PURPLE)
        verify(spyPref, times(1))
                .applyString(R.string.pref_colorTheme, ColorTheme.PURPLE.name)
    }

    @Test
    fun addChangeListener() {
        var calls = 0
        manager.addChangeListener { calls++ }
        assertThat(calls).isEqualTo(1)

        manager.setColorTheme(ColorTheme.PURPLE)
        assertThat(calls).isEqualTo(2)
    }

    @Test
    fun removeChangeListener() {
        var calls = 0
        val listener: () -> Unit = { calls++ }
        manager.addChangeListener(listener)
        manager.removeChangeListener(listener)
        assertThat(calls).isEqualTo(1)

        manager.setColorTheme(ColorTheme.PURPLE)
        assertThat(calls).isEqualTo(1)
    }

    @Test
    fun bindView() {
        var calls = 0
        val spyView = spy(mock(View::class.java))
        val listener: () -> Unit = { calls++ }
        manager.bindView(spyView, listener)
        assertThat(calls).isEqualTo(1)
        verify(spyView, times(1))
                .addOnAttachStateChangeListener(any(View.OnAttachStateChangeListener::class.java))
    }
}
