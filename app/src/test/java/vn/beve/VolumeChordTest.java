package vn.beve;
import org.junit.Test;
import static org.junit.Assert.*;
public class VolumeChordTest {
    @Test public void requiresBothKeysAndResets() {
        VolumeChord chord = new VolumeChord();
        assertFalse(chord.press(true)); assertFalse(chord.press(true));
        chord.release(true); assertFalse(chord.press(false));
        assertTrue(chord.press(true)); chord.reset();
        assertFalse(chord.press(true)); chord.release(true);
        assertFalse(chord.press(false)); assertTrue(chord.press(true));
    }
}
