package dev.kensa.state;

import dev.kensa.util.KensaMap;

public class CapturedInteractions extends KensaMap<CapturedInteractions> {

    private boolean isUnderTestEnabled = true;
    private boolean isUnderTest;

    public void capture(CapturedInteractionBuilder builder) {
        if (isUnderTestEnabled) {
            builder.underTest(isUnderTest);
        }

        builder.applyTo(this);
    }

    public void setUnderTest(boolean isUnderTest) {
        this.isUnderTest = isUnderTest;
    }

    public void disableUnderTest() {
        this.isUnderTestEnabled = false;
    }
}
