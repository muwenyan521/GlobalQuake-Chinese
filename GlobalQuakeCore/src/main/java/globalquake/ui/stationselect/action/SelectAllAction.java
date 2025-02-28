package globalquake.ui.stationselect.action;

import globalquake.core.database.Network;
import globalquake.core.database.Station;
import globalquake.core.database.StationDatabaseManager;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;

public class SelectAllAction extends AbstractAction {

    private final StationDatabaseManager stationDatabaseManager;
    private final Window parent;

    public SelectAllAction(StationDatabaseManager stationDatabaseManager, Window parent) {
        super("全选");
        this.stationDatabaseManager=stationDatabaseManager;
        this.parent = parent;

        putValue(SHORT_DESCRIPTION, "选择所有可用台站");

        ImageIcon selectAllIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/image_icons/selectAll.png")));
        Image image = selectAllIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(image);
        putValue(Action.SMALL_ICON, scaledIcon);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        int option = JOptionPane.showConfirmDialog(parent,
                "你确定要选择所有台站吗?",
                "确定",
                JOptionPane.YES_NO_OPTION);

        if (option != JOptionPane.YES_OPTION) {
            return;
        }

        stationDatabaseManager.getStationDatabase().getDatabaseWriteLock().lock();
        try{
            for(Network network : stationDatabaseManager.getStationDatabase().getNetworks()){
                network.getStations().forEach(Station::selectBestAvailableChannel);
            }
            stationDatabaseManager.fireUpdateEvent();
        }finally {
            stationDatabaseManager.getStationDatabase().getDatabaseWriteLock().unlock();
        }
    }
}
