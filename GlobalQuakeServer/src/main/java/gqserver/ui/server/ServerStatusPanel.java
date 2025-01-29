package gqserver.ui.server;

import globalquake.core.Settings;
import globalquake.core.exception.RuntimeApplicationException;
import gqserver.events.GlobalQuakeServerEventListener;
import gqserver.events.specific.ServerStatusChangedEvent;
import gqserver.main.Main;
import gqserver.server.GlobalQuakeServer;
import gqserver.server.SocketStatus;
import gqserver.ui.server.tabs.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class ServerStatusPanel extends JPanel {
    private JButton controlButton;
    private JLabel statusLabel;
    private JTextField addressField;
    private JTextField portField;

    public ServerStatusPanel() {
        setLayout(new BorderLayout());

        add(createTopPanel(), BorderLayout.NORTH);
        add(createMiddlePanel(), BorderLayout.CENTER);
    }

    private Component createMiddlePanel() {
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("状态", new StatusTab());
        tabbedPane.addTab("节点", new SeedlinksTab());
        tabbedPane.addTab("客户端", new ClientsTab());
        tabbedPane.addTab("地震", new EarthquakesTab());
        tabbedPane.addTab("序列", new ClustersTab());

        return tabbedPane;
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));

        JPanel addressPanel = new JPanel(new GridLayout(2,1));
        addressPanel.setBorder(BorderFactory.createTitledBorder("服务器地震"));

        JPanel ipPanel = new JPanel();
        ipPanel.setLayout(new BoxLayout(ipPanel, BoxLayout.X_AXIS));
        ipPanel.add(new JLabel("IP地址: "));
        ipPanel.add(addressField = new JTextField(Settings.lastServerIP,20));

        addressPanel.add(ipPanel);

        JPanel portPanel = new JPanel();
        portPanel.setLayout(new BoxLayout(portPanel, BoxLayout.X_AXIS));
        portPanel.add(new JLabel("端口: "));
        portPanel.add(portField = new JTextField(String.valueOf(Settings.lastServerPORT),20));

        addressPanel.add(portPanel);

        topPanel.add(addressPanel);

        JPanel controlPanel = new JPanel(new GridLayout(2,1));
        controlPanel.setBorder(BorderFactory.createTitledBorder("控制面板"));

        controlPanel.add(statusLabel = new JLabel("状态:空闲"));
        controlPanel.add(controlButton = new JButton("启动服务器"));

        GlobalQuakeServer.instance.getServerEventHandler().registerEventListener(new GlobalQuakeServerEventListener(){
            @Override
            public void onServerStatusChanged(ServerStatusChangedEvent event) {
                switch (event.status()){
                    case IDLE -> {
                        addressField.setEnabled(true);
                        portField.setEnabled(true);
                        controlButton.setEnabled(true);
                        controlButton.setText("启动服务器");
                    }
                    case OPENING -> {
                        addressField.setEnabled(false);
                        portField.setEnabled(false);
                        controlButton.setEnabled(false);
                        controlButton.setText("启动服务器");
                    }
                    case RUNNING -> {
                        addressField.setEnabled(false);
                        portField.setEnabled(false);
                        controlButton.setEnabled(true);
                        controlButton.setText("停止服务器");
                    }
                }
                statusLabel.setText("状态: %s".formatted(event.status()));
            }
        });

        controlButton.addActionListener(actionEvent -> {
            SocketStatus status = GlobalQuakeServer.instance.getServerSocket().getStatus();
            if(status == SocketStatus.IDLE){
                try {
                    String ip = addressField.getText();
                    int port = Integer.parseInt(portField.getText());

                    Settings.lastServerIP = ip;
                    Settings.lastServerPORT = port;
                    Settings.save();

                    GlobalQuakeServer.instance.initStations();
                    GlobalQuakeServer.instance.getServerSocket().run(ip, port);
                    GlobalQuakeServer.instance.startRuntime();
                } catch(Exception e){
                    Main.getErrorHandler().handleException(new RuntimeApplicationException("服务器启动失败", e));
                }
            } else if(status == SocketStatus.RUNNING) {
                if(confirm("你确定要关闭服务器吗?")) {
                    try {
                        GlobalQuakeServer.instance.getServerSocket().stop();
                        GlobalQuakeServer.instance.stopRuntime();
                        GlobalQuakeServer.instance.reset();
                    } catch (IOException e) {
                        Main.getErrorHandler().handleException(new RuntimeApplicationException("服务器停止失败", e));
                    }
                }
            }
        });

        topPanel.add(controlPanel);
        return topPanel;
    }

    @SuppressWarnings("SameParameterValue")
    private boolean confirm(String s) {
        return JOptionPane.showConfirmDialog(this, s, "确定", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

}
