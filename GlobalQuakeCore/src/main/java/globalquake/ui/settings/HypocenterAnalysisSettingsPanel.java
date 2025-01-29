package globalquake.ui.settings;

import globalquake.core.Settings;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;

public class HypocenterAnalysisSettingsPanel extends SettingsPanel {

    private JSlider sliderPWaveInaccuracy;
    private JSlider sliderCorrectness;
    private JSlider sliderMinStations;
    private JSlider sliderMaxStations;

    public HypocenterAnalysisSettingsPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(createMinStationsSetting());
        add(createMaxStationsSetting());
        add(createSettingPWave());
        add(createSettingCorrectness());
    }

    private Component createMaxStationsSetting() {
        sliderMaxStations = createSettingsSlider(20, 300, 20, 5);

        JLabel label = new JLabel();

        ChangeListener upd = changeEvent -> label.setText("关联测站的最大数量:%d".formatted(sliderMaxStations.getValue()));

        sliderMaxStations.addChangeListener(upd);
        sliderMaxStations.setValue(Settings.maxEvents);

        upd.stateChanged(null);

        return createCoolLayout(sliderMaxStations, label, "%s".formatted(Settings.maxEventsDefault),
                """
                在这里,您可以设置震源定位算法将使用的最大测站数量.
                增加这个值可以提高地震检测的准确性,
                但请注意,这样做也会显著增加计算需求,
                可能导致处理时间变长.
                """);
    }

    private Component createMinStationsSetting() {
        sliderMinStations = createSettingsSlider(4, 16, 1, 1);

        JLabel label = new JLabel();

        ChangeListener upd = changeEvent -> label.setText("最小测站数量:%d".formatted(sliderMinStations.getValue()));

        sliderMinStations.addChangeListener(upd);
        sliderMinStations.setValue(Settings.minimumStationsForEEW);

        upd.stateChanged(null);

        return createCoolLayout(sliderMinStations, label, "%s".formatted(Settings.minimumStationsForEEWDefault),
                """
                在这里,您可以设置发布地震预警(EEW)所需的最小测站数量.
                增加这个数量可以大大减少误报的数量,
                但也可能导致在测站较少的地区无法出现地震预警.
                """);
    }

    public static JPanel createCoolLayout(JSlider slider, JLabel label, String defaultValue, String explanation){
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createRaisedBevelBorder());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(5,5,5,5));

        topPanel.add(label, BorderLayout.NORTH);
        topPanel.add(slider, BorderLayout.CENTER);

        if(defaultValue != null) {
            JLabel labelDefault = new JLabel("默认值:" + defaultValue);
            labelDefault.setBorder(new EmptyBorder(8, 2, 0, 0));
            topPanel.add(labelDefault, BorderLayout.SOUTH);
        }

        if(explanation != null) {
            JTextArea textAreaExplanation = new JTextArea(explanation);
            textAreaExplanation.setBorder(new EmptyBorder(5, 5, 5, 5));
            textAreaExplanation.setEditable(false);
            textAreaExplanation.setBackground(panel.getBackground());
            panel.add(textAreaExplanation, BorderLayout.CENTER);
        }

        panel.add(topPanel, BorderLayout.NORTH);

        return panel;
    }

    public static JSlider createSettingsSlider(int min, int max, int major, int minor){
        JSlider slider = new JSlider();
        slider.setMinimum(min);
        slider.setMaximum(max);
        slider.setMajorTickSpacing(major);
        slider.setMinorTickSpacing(minor);

        slider.setPaintLabels(true);
        slider.setPaintTicks(true);
        return slider;
    }

    private Component createSettingCorrectness() {
        sliderCorrectness = createSettingsSlider(20, 90, 10, 2);

        JLabel label = new JLabel();

        ChangeListener upd = changeEvent -> label.setText("震源正确性阈值:%d %%".formatted(sliderCorrectness.getValue()));

        sliderCorrectness.addChangeListener(upd);
        sliderCorrectness.setValue(Settings.hypocenterCorrectThreshold.intValue());

        upd.stateChanged(null);

        return createCoolLayout(sliderCorrectness, label, "%s %%".formatted(Settings.hypocenterCorrectThresholdDefault),
                """
                这个值决定了震源被认为是正确还是不正确的阈值.
                正确性是通过计算到达时间在不准确性阈值内的测站数量
                与震源定位算法使用的总测站数量之比来计算的.
                如果震源被标记为不正确,地震将不会显示在地图上.
                较高的值会导致更多的误报,
                较低的值会导致更多的漏报.
                """);
    }

    private Component createSettingPWave() {
        sliderPWaveInaccuracy = createSettingsSlider(400, 5200, 400, 200);

        JLabel label = new JLabel();
        ChangeListener changeListener = changeEvent -> label.setText("P波到达时间不准确性阈值:%d 毫秒".formatted(sliderPWaveInaccuracy.getValue()));
        sliderPWaveInaccuracy.addChangeListener(changeListener);

        sliderPWaveInaccuracy.setValue(Settings.pWaveInaccuracyThreshold.intValue());
        changeListener.stateChanged(null);

        return createCoolLayout(sliderPWaveInaccuracy, label, "%s 毫秒".formatted(Settings.pWaveInaccuracyThresholdDefault),
                """
                这个值决定了震源定位算法在考虑从当前点到测站的到达时间
                是否正确时使用的阈值.
                较高的值限制性较小,会导致更多的误报.
                较低的值会强制算法找到更准确的震源,
                但会导致更多的漏报.
                """);
    }

    @Override
    public void save() {
        Settings.pWaveInaccuracyThreshold = (double) sliderPWaveInaccuracy.getValue();
        Settings.hypocenterCorrectThreshold = (double) sliderCorrectness.getValue();
        Settings.minimumStationsForEEW = sliderMinStations.getValue();
        Settings.maxEvents = sliderMaxStations.getValue();
    }

    @Override
    public String getTitle() {
        return "高级";
    }
}