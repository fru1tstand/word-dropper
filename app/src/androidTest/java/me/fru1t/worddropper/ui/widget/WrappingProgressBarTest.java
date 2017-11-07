package me.fru1t.worddropper.ui.widget;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class WrappingProgressBarTest {
    private static final int TEST_MAX = 10;

    private Context testContext;
    private WrappingProgressBar testProgressBar;

    @Before
    public void setUp() {
        testContext = InstrumentationRegistry.getTargetContext();
        testProgressBar = new WrappingProgressBar(testContext);
        testProgressBar.setMax(TEST_MAX);
    }

    @Test
    public void maxShouldStayConstantWhenNoFunctionIsPresent() {

    }
}
