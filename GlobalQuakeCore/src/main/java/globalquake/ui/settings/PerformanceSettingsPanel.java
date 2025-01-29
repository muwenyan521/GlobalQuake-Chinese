package globalquake.ui.settings;

import globalquake.core.Settings;
import globalquake.core.training.EarthquakeAnalysisTraining;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;

public class PerformanceSettingsPanel extends SettingsPanel {
    private JSlider sliderResolution;
    private JCheckBox chkBoxParalell;
    private JCheckBox chkBoxRecalibrateOnLauch;

    public PerformanceSettingsPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(createSettingAccuracy());
        add(createSettingParalell());
        fill(this, 16);
    }

    @SuppressWarnings("ExtractMethodRecommender")
    private JPanel createSettingParalell() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createRaisedBevelBorder());
        panel.setLayout(new BorderLayout());
        chkBoxParalell = new JCheckBox("使用所有 CPU 核心");
        chkBoxParalell.setSelected(Settings.parallelHypocenterLocations);

        JTextArea textAreaExplanation = new JTextArea(
                """
                使用所有 CPU 核心将大大加快震源定位的速度,
                但会占用 100% 的 CPU,这可能会增加卡顿.
                请确保在上方为您的系统选择了最佳分辨率.""");
        textAreaExplanation.setBorder(new EmptyBorder(5, 5, 5, 5));
        textAreaExplanation.setEditable(false);
        textAreaExplanation.setBackground(panel.getBackground());

        chkBoxParalell.addChangeListener(changeEvent -> Settings.parallelHypocenterLocations = chkBoxParalell.isSelected());

        panel.add(chkBoxParalell, BorderLayout.CENTER);
        panel.add(textAreaExplanation, BorderLayout.SOUTH);
        return panel;
    }

    @Override
    public void save() {
        Settings.hypocenterDetectionResolution = (double) sliderResolution.getValue();
        Settings.parallelHypocenterLocations = chkBoxParalell.isSelected();
        Settings.recalibrateOnLaunch = chkBoxRecalibrateOnLauch.isSelected();
    }

    private Component createSettingAccuracy() {
        sliderResolution = HypocenterAnalysisSettingsPanel.createSettingsSlider(0, 160, 10, 5);

        JLabel label = new JLabel();
        ChangeListener changeListener = changeEvent ->
        {
            label.setText("震源定位分辨率(CPU):%.2f ~ %s".formatted(
                    sliderResolution.getValue() / 100.0,
                    getNameForResolution(sliderResolution.getValue())));
            Settings.hypocenterDetectionResolution = (double) sliderResolution.getValue();
        };
        sliderResolution.addChangeListener(changeListener);

        sliderResolution.setValue(Settings.hypocenterDetectionResolution.intValue());
        changeListener.stateChanged(null);

        JPanel panel = HypocenterAnalysisSettingsPanel.createCoolLayout(sliderResolution, label, "%.2f".formatted(Settings.hypocenterDetectionResolutionDefault / 100.0),
                """
                通过增加震源定位分辨率,您可以提高 GlobalQuake 定位震源的准确度,
                但这会增加 CPU 的负担.如果在地图上发生地震时出现明显的卡顿,
                您应该降低这个值.
                """);

        JPanel panel2 = new JPanel();

        JButton btnRecalibrate = new JButton("重新校准");
        btnRecalibrate.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                btnRecalibrate.setEnabled(false);
                sliderResolution.setEnabled(false);
                new Thread(() -> {
                    EarthquakeAnalysisTraining.calibrateResolution(null, sliderResolution, true);
                    btnRecalibrate.setEnabled(true);
                    sliderResolution.setEnabled(true);
                }).start();
            }
        });

        panel2.add(btnRecalibrate);

        JButton testSpeed = new JButton("测试震源搜索");
        testSpeed.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                testSpeed.setEnabled(false);
                new Thread(() -> {
                    testSpeed.setText("测试耗时 %d 毫秒".formatted(EarthquakeAnalysisTraining.measureTest(System.currentTimeMillis(), 60, true)));
                    testSpeed.setEnabled(true);
                }).start();
            }
        });
        panel2.add(testSpeed);

        chkBoxRecalibrateOnLauch = new JCheckBox("启动时重新校准", Settings.recalibrateOnLaunch);
        panel2.add(chkBoxRecalibrateOnLauch);

        panel.add(panel2, BorderLayout.SOUTH);

        return panel;
    }

    public static final String[] RESOLUTION_NAMES = {"极低", "低", "默认", "较高", "高", "很高", "极高", "疯狂"};

    private String getNameForResolution(int value) {
        return RESOLUTION_NAMES[(int) Math.max(0, Math.min(RESOLUTION_NAMES.length - 1, ((value / 160.0) * (RESOLUTION_NAMES.length))))];
    }

    @Override
    public String getTitle() {
        return "性能";
    }
}