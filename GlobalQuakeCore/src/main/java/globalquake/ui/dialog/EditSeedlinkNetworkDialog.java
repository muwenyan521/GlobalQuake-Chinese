package globalquake.ui.dialog;

import globalquake.core.database.SeedlinkNetwork;
import globalquake.core.database.StationDatabaseManager;
import globalquake.core.exception.RuntimeApplicationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class EditSeedlinkNetworkDialog extends JDialog {
    private final StationDatabaseManager databaseManager;
    private final JTextField portField;
    private final JTextField nameField;
    private final JTextField hostField;
    private final SeedlinkNetwork seedlinkNetwork;
    private final JTextField timeoutField;

    public EditSeedlinkNetworkDialog(Window parent, StationDatabaseManager databaseManager, SeedlinkNetwork seedlinkNetwork) {
        super(parent);
        this.seedlinkNetwork = seedlinkNetwork;
        setModal(true);

        this.databaseManager = databaseManager;
        setLayout(new BorderLayout());

        setTitle("编辑Seedlink节点");
        setSize(320, 180);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parent);

        JPanel fieldsPanel = new JPanel();
        LayoutManager gridLayout = new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS);
        fieldsPanel.setLayout(gridLayout);
        fieldsPanel.setBorder(new EmptyBorder(10,10,10,10));

        nameField = new JTextField(seedlinkNetwork==null ? "" : seedlinkNetwork.getName(), 40);
        hostField = new JTextField(seedlinkNetwork==null ? "" : seedlinkNetwork.getHost(), 40);
        portField = new JTextField(seedlinkNetwork==null ? "18000" : String.valueOf(seedlinkNetwork.getPort()), 40);
        timeoutField = new JTextField(seedlinkNetwork==null ? String.valueOf(SeedlinkNetwork.DEFAULT_TIMEOUT) : String.valueOf(seedlinkNetwork.getTimeout()), 40);
        JButton saveButton = new JButton("保存");
        saveButton.addActionListener(e -> saveChanges());
        JButton cancelButton = new JButton("取消");
        cancelButton.addActionListener(actionEvent -> EditSeedlinkNetworkDialog.this.dispose());

        JPanel buttonsPanel = new JPanel();

        fieldsPanel.add(new JLabel("名称:"));
        fieldsPanel.add(nameField);
        fieldsPanel.add(new JLabel("地址:"));
        fieldsPanel.add(hostField);
        fieldsPanel.add(new JLabel("端口:"));
        fieldsPanel.add(portField);
        fieldsPanel.add(new JLabel("超时时间(秒):"));
        fieldsPanel.add(timeoutField);
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(saveButton);

        add(fieldsPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        setResizable(false);

        pack();
        setVisible(true);
    }

    private void saveChanges() {
        SeedlinkNetwork newSeedlinkNetwork = getSeedlinkNetwork();
        databaseManager.getStationDatabase().getDatabaseWriteLock().lock();
        try{
            databaseManager.getStationDatabase().getSeedlinkNetworks().remove(seedlinkNetwork);
            databaseManager.getStationDatabase().getSeedlinkNetworks().add(newSeedlinkNetwork);
        }finally {
            databaseManager.getStationDatabase().getDatabaseWriteLock().unlock();
        }

        databaseManager.fireUpdateEvent();

        this.dispose();
    }

    private SeedlinkNetwork getSeedlinkNetwork() {
        int port;
        try {
            port = Integer.parseInt(portField.getText());
        }catch(NumberFormatException e){
            throw new RuntimeApplicationException("无效的端口!", e);
        }

        int timeout;

        try {
            timeout = Integer.parseInt(timeoutField.getText());
        }catch(NumberFormatException e){
            throw new RuntimeApplicationException("无效的超时时间!", e);
        }

        if(timeout < 5 || timeout > 300){
            throw new RuntimeApplicationException("超时时间必须在5秒到300秒之间!");
        }

        return new SeedlinkNetwork(nameField.getText(), hostField.getText(), port, timeout);
    }
}
