package vn.beve;

final class VolumeChord {
    private boolean up, down;
    boolean press(boolean isUp) {
        if (isUp) up = true; else down = true;
        return up && down;
    }
    void release(boolean isUp) { if (isUp) up = false; else down = false; }
    void reset() { up = false; down = false; }
}
