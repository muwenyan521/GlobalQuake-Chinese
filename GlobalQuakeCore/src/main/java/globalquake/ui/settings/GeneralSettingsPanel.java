package globalquake.ui.settings;

import globalquake.core.Settings;
import globalquake.core.geo.DistanceUnit;
import globalquake.core.intensity.IntensityScale;
import globalquake.core.intensity.IntensityScales;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Objects;

public class GeneralSettingsPanel extends SettingsPanel {
	private JComboBox<IntensityScale> comboBoxScale;
	private JCheckBox chkBoxHomeLoc;

	private JTextField textFieldLat;
	private JTextField textFieldLon;
	private JComboBox<DistanceUnit> distanceUnitJComboBox;
	private JComboBox<ZoneId> timezoneCombobox;

	private JSlider sliderStoreTime;

	public GeneralSettingsPanel(SettingsFrame settingsFrame) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		createHomeLocationSettings();
		//createAlertsDialogSettings();
		add(createIntensitySettingsPanel());
		createOtherSettings(settingsFrame);
		add(createSettingStoreTime());
	}

	private void createOtherSettings(SettingsFrame settingsFrame) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createTitledBorder("其他"));

		JPanel row1 = new JPanel();

		row1.add(new JLabel("距离单位: "));

		distanceUnitJComboBox = new JComboBox<>(DistanceUnit.values());
		distanceUnitJComboBox.setSelectedIndex(Math.max(0, Math.min(distanceUnitJComboBox.getItemCount() - 1, Settings.distanceUnitsIndex)));

		distanceUnitJComboBox.addItemListener(itemEvent -> {
			Settings.distanceUnitsIndex = distanceUnitJComboBox.getSelectedIndex();
			settingsFrame.refreshUI();
		});

		row1.add(distanceUnitJComboBox);

		JPanel row2 = new JPanel();

		row2.add(new JLabel("时区: "));
		Comparator<ZoneId> zoneIdComparator = Comparator.comparingInt(zone -> zone.getRules().getOffset(Instant.now()).getTotalSeconds());

		DefaultComboBoxModel<ZoneId> timezoneModel = new DefaultComboBoxModel<>();

		ZoneId.getAvailableZoneIds().stream()
				.map(ZoneId::of)
				.sorted(zoneIdComparator)
				.forEach(timezoneModel::addElement);

		timezoneCombobox = new JComboBox<>(timezoneModel);

		timezoneCombobox.setSelectedItem(ZoneId.systemDefault());

		timezoneCombobox.setSelectedItem(ZoneId.of(Settings.timezoneStr));

		row2.add(timezoneCombobox);

		timezoneCombobox.setRenderer(new DefaultListCellRenderer(){
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index,
														  boolean isSelected, boolean cellHasFocus) {
				JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

				if (value instanceof ZoneId zoneId) {
					String offset = zoneId.getRules().getOffset(Instant.now()).toString();
					label.setText(String.format("%s (%s)", zoneId, offset));
				}

				return label;
			}
		});

		timezoneCombobox.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				Settings.timezoneStr = ((ZoneId) Objects.requireNonNull(timezoneCombobox.getSelectedItem())).getId();
				Settings.initTimezoneSettings();
			}
		});

		row2.add(timezoneCombobox);

		panel.add(row1);
		panel.add(row2);

		add(panel);
	}

	private void createHomeLocationSettings() {
		JPanel outsidePanel = new JPanel(new BorderLayout());
		outsidePanel.setBorder(BorderFactory.createTitledBorder("用户位置设置"));

		JPanel homeLocationPanel = new JPanel();
		homeLocationPanel.setLayout(new GridLayout(2,1));

		JLabel lblLat = new JLabel("用户纬度: ");
		JLabel lblLon = new JLabel("用户经度: ");

		textFieldLat = new JTextField(20);
		textFieldLat.setText(String.format("%s", Settings.homeLat));
		textFieldLat.setColumns(10);

		textFieldLon = new JTextField(20);
		textFieldLon.setText(String.format("%s", Settings.homeLon));
		textFieldLon.setColumns(10);

		JPanel latPanel = new JPanel();

		latPanel.add(lblLat);
		latPanel.add(textFieldLat);

		JPanel lonPanel = new JPanel();

		lonPanel.add(lblLon);
		lonPanel.add(textFieldLon);

		homeLocationPanel.add(latPanel);
		homeLocationPanel.add(lonPanel);

		JTextArea infoLocation = new JTextArea("如果附近发生地震,用户位置将用于播放额外的警报声音");
		infoLocation.setBorder(new EmptyBorder(5,5,5,5));
		infoLocation.setLineWrap(true);
		infoLocation.setEditable(false);
		infoLocation.setBackground(homeLocationPanel.getBackground());

		chkBoxHomeLoc = new JCheckBox("显示用户位置");
		chkBoxHomeLoc.setSelected(Settings.displayHomeLocation);
		outsidePanel.add(chkBoxHomeLoc);

		outsidePanel.add(homeLocationPanel, BorderLayout.NORTH);
		outsidePanel.add(infoLocation, BorderLayout.CENTER);
		outsidePanel.add(chkBoxHomeLoc, BorderLayout.SOUTH);

		add(outsidePanel);
	}

	private Component createSettingStoreTime() {
		sliderStoreTime = HypocenterAnalysisSettingsPanel.createSettingsSlider(2, 20, 2, 1);

		JLabel label = new JLabel();
		ChangeListener changeListener = changeEvent -> label.setText("波形数据存储时间(分钟): %d".formatted(
				sliderStoreTime.getValue()));

		sliderStoreTime.addChangeListener(changeListener);

		sliderStoreTime.setValue(Settings.logsStoreTimeMinutes);
		changeListener.stateChanged(null);

		return HypocenterAnalysisSettingsPanel.createCoolLayout(sliderStoreTime, label, "5",
				"""
                在GlobalQuake中,波形数据对系统内存的需求最高.
                如果遇到内存限制,您有两个选择:
                减少选定的台站数量或降低此特定值.
                """);
	}

	private JPanel createIntensitySettingsPanel() {
		JPanel panel = new JPanel(new GridLayout(2,1));
		panel.setBorder(BorderFactory.createTitledBorder("烈度等级"));

		comboBoxScale = new JComboBox<>(IntensityScales.INTENSITY_SCALES);
		comboBoxScale.setSelectedIndex(Settings.intensityScaleIndex);

		JPanel div = new JPanel();
		div.add(comboBoxScale);
		panel.add(div, BorderLayout.CENTER);

		JLabel lbl = new JLabel();
		lbl.setFont(new Font("MiSans Normal", Font.PLAIN, 13));
		lbl.setText("请注意,显示的烈度是估算值,而非实测值");

		panel.add(lbl, BorderLayout.SOUTH);

		return panel;
	}

	@Override
	public void save() {
		Settings.homeLat = parseDouble(textFieldLat.getText(), "用户纬度", -90, 90);
		Settings.homeLon = parseDouble(textFieldLon.getText(), "用户经度", -180, 180);
		Settings.intensityScaleIndex = comboBoxScale.getSelectedIndex();
		Settings.displayHomeLocation = chkBoxHomeLoc.isSelected();
		Settings.distanceUnitsIndex = distanceUnitJComboBox.getSelectedIndex();
		Settings.timezoneStr = ((ZoneId) Objects.requireNonNull(timezoneCombobox.getSelectedItem())).getId();
		Settings.logsStoreTimeMinutes = sliderStoreTime.getValue();
	}

	@Override
	public String getTitle() {
		return "通用";
	}
}