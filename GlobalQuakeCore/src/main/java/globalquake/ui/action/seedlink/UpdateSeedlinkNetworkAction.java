package globalquake.ui.action.seedlink;


import globalquake.core.database.SeedlinkNetwork;
import globalquake.core.database.StationDatabaseManager;
import globalquake.core.exception.RuntimeApplicationException;
import globalquake.ui.table.FilterableTableModel;

import javax.swing.*;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UpdateSeedlinkNetworkAction extends AbstractAction {

    private final StationDatabaseManager databaseManager;
    private FilterableTableModel<SeedlinkNetwork> tableModel;

    private JTable table;

    public UpdateSeedlinkNetworkAction(StationDatabaseManager databaseManager) {
        super("更新");
        this.databaseManager = databaseManager;

        putValue(SHORT_DESCRIPTION, "更新Seedlink节点");

        ImageIcon updateIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/image_icons/update.png")));
        Image image = updateIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(image);
        putValue(Action.SMALL_ICON, scaledIcon);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        this.setEnabled(false);
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length < 1) {
            throw new RuntimeApplicationException("无效的选中行数(必须大于0): " + selectedRows.length);
        }
        if (table.isEditing()) {
            table.getCellEditor().cancelCellEditing();
        }

        List<SeedlinkNetwork> toBeUpdated = new ArrayList<>();
        for (int i : selectedRows) {
            SeedlinkNetwork seedlinkNetwork = tableModel.getEntity(table.getRowSorter().convertRowIndexToModel(i));
            toBeUpdated.add(seedlinkNetwork);
        }

        databaseManager.runAvailabilityCheck(toBeUpdated, () -> UpdateSeedlinkNetworkAction.this.setEnabled(true));
    }

    public void setTableModel(FilterableTableModel<SeedlinkNetwork> tableModel) {
        this.tableModel = tableModel;
    }

    public void setTable(JTable table) {
        this.table = table;
    }
}
