package model;

public class UserSettings {

    private String theme;
    private boolean showScramble;

    public UserSettings(String theme, boolean showScramble) {
        this.theme = theme;
        this.showScramble = showScramble;
    }

    public String getTheme() { return theme; }
    public boolean isShowScramble() { return showScramble; }

    public void setTheme(String theme) { this.theme = theme; }
    public void setShowScramble(boolean showScramble) { this.showScramble = showScramble; }

    @Override
    public String toString() {
        return "UserSettings{theme='" + theme + "', showScramble=" + showScramble + "}";
    }
}