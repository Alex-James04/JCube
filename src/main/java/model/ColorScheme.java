package model;

// Per-role custom color scheme for the whole app, persisted as a single row via ColorSchemeDB.
// Not a record since the Settings screen needs individual setters to wire up per-role ColorPickers.
public class ColorScheme {

    private String background;
    private String surface;
    private String textPrimary;
    private String textSecondary;
    private String accent;
    private String button;
    private String buttonHover;
    private String danger;
    private String border;

    public ColorScheme(String background, String surface, String textPrimary, String textSecondary,
                        String accent, String button, String buttonHover, String danger, String border) {
        this.background = background;
        this.surface = surface;
        this.textPrimary = textPrimary;
        this.textSecondary = textSecondary;
        this.accent = accent;
        this.button = button;
        this.buttonHover = buttonHover;
        this.danger = danger;
        this.border = border;
    }

    public static ColorScheme darkPreset() {
        return new ColorScheme(
                "#1e1e1e", "#2a2a2a", "#e0e0e0", "#c2c2c2",
                "#4a9eff", "#333333", "#444444", "#c0392b", "#3a3a3a");
    }

    public static ColorScheme lightPreset() {
        return new ColorScheme(
                "#f5f5f5", "#ffffff", "#1e1e1e", "#5a5a5a",
                "#1a73e8", "#e0e0e0", "#d0d0d0", "#d32f2f", "#d0d0d0");
    }

    public String getBackground() { return background; }
    public String getSurface() { return surface; }
    public String getTextPrimary() { return textPrimary; }
    public String getTextSecondary() { return textSecondary; }
    public String getAccent() { return accent; }
    public String getButton() { return button; }
    public String getButtonHover() { return buttonHover; }
    public String getDanger() { return danger; }
    public String getBorder() { return border; }

    public void setBackground(String background) { this.background = background; }
    public void setSurface(String surface) { this.surface = surface; }
    public void setTextPrimary(String textPrimary) { this.textPrimary = textPrimary; }
    public void setTextSecondary(String textSecondary) { this.textSecondary = textSecondary; }
    public void setAccent(String accent) { this.accent = accent; }
    public void setButton(String button) { this.button = button; }
    public void setButtonHover(String buttonHover) { this.buttonHover = buttonHover; }
    public void setDanger(String danger) { this.danger = danger; }
    public void setBorder(String border) { this.border = border; }

    @Override
    public String toString() {
        return "ColorScheme{background='" + background + "', surface='" + surface + "', textPrimary='" + textPrimary
                + "', textSecondary='" + textSecondary + "', accent='" + accent + "', button='" + button
                + "', buttonHover='" + buttonHover + "', danger='" + danger + "', border='" + border + "'}";
    }
}
