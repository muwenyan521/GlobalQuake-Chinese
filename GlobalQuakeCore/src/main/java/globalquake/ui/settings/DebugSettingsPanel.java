package globalquake.ui.settings;

import globalquake.core.Settings;
import globalquake.core.report.EarthquakeReporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class DebugSettingsPanel extends SettingsPanel {

    private final JCheckBox chkBoxReports;
    private final JCheckBox chkBoxCoreWaves;
    private final JCheckBox chkBoxConfidencePolygons;
    private final JCheckBox chkBoxRevisions;

    public DebugSettingsPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(5,5,5,5));

        add(chkBoxReports = new JCheckBox("启用地震报告", Settings.reportsEnabled));
        add(new JLabel("     报告将存储在 %s".formatted(EarthquakeReporter.ANALYSIS_FOLDER.getPath())));
        add(chkBoxCoreWaves = new JCheckBox("显示 PKP 和 PKIKP 波", Settings.displayCoreWaves));
        add(chkBoxConfidencePolygons = new JCheckBox("显示震中置信多边形", Settings.confidencePolygons));
        add(chkBoxRevisions = new JCheckBox("减少修正报次数", Settings.reduceRevisions));
    }

    @Override
    public void save() {
        Settings.reportsEnabled = chkBoxReports.isSelected();
        Settings.displayCoreWaves = chkBoxCoreWaves.isSelected();
        Settings.confidencePolygons = chkBoxConfidencePolygons.isSelected();
        Settings.reduceRevisions = chkBoxRevisions.isSelected();
    }

    @Override
    public String getTitle() {
        return "调试";
    }
}