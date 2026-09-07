package model;

import java.util.List;

public class Settings {

    private String theme;
    private boolean showScramble;
    private SpacebarMode spacebarMode;
    private InspectionMode inspectionMode;
    private List<StatSpec> statSpecs;
    private boolean confirmDeletes;

    public Settings(String theme, boolean showScramble, SpacebarMode spacebarMode,
                     InspectionMode inspectionMode, List<StatSpec> statSpecs, boolean confirmDeletes) {
        this.theme = theme;
        this.showScramble = showScramble;
        this.spacebarMode = spacebarMode;
        this.inspectionMode = inspectionMode;
        this.statSpecs = statSpecs;
        this.confirmDeletes = confirmDeletes;
    }

    public String getTheme() { return theme; }
    public boolean isShowScramble() { return showScramble; }
    public SpacebarMode getSpacebarMode() { return spacebarMode; }
    public InspectionMode getInspectionMode() { return inspectionMode; }
    public List<StatSpec> getStatSpecs() { return statSpecs; }
    public boolean isConfirmDeletes() { return confirmDeletes; }

    public void setTheme(String theme) { this.theme = theme; }
    public void setShowScramble(boolean showScramble) { this.showScramble = showScramble; }
    public void setSpacebarMode(SpacebarMode spacebarMode) { this.spacebarMode = spacebarMode; }
    public void setInspectionMode(InspectionMode inspectionMode) { this.inspectionMode = inspectionMode; }
    public void setStatSpecs(List<StatSpec> statSpecs) { this.statSpecs = statSpecs; }
    public void setConfirmDeletes(boolean confirmDeletes) { this.confirmDeletes = confirmDeletes; }

    @Override
    public String toString() {
        return "UserSettings{theme='" + theme + "', showScramble=" + showScramble
                + ", spacebarMode=" + spacebarMode + ", inspectionMode=" + inspectionMode
                + ", statSpecs=" + statSpecs + ", confirmDeletes=" + confirmDeletes + "}";
    }
}
