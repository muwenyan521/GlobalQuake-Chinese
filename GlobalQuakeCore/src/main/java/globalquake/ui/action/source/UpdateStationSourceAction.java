package globalquake.ui.action.source;


import globalquake.core.database.StationDatabaseManager;
import globalquake.core.database.StationSource;
import globalquake.core.exception.RuntimeApplicationException;
import globalquake.ui.table.FilterableTableModel;

import javax.swing.*;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UpdateStationSourceAction extends AbstractAction {

    private final StationDatabaseManager databaseManager;
    private FilterableTableModel<StationSource> tableModel;

    private JTable table;

    public UpdateStationSourceAction(StationDatabaseManager databaseManager) {
        super("更新");
        this.databaseManager = databaseManager;

        putValue(SHORT_DESCRIPTION, "更新台站数据源");

        ImageIcon updateIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/image_icons/update.png")));
        Image image = updateIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(image);
        putValue(Action.SMALL_ICON, scaledIcon);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length < 1) {
            throw new RuntimeApplicationException("无效的选中行数(必须大于0): " + selectedRows.length);
        }
        if (table.isEditing()) {
            table.getCellEditor().cancelCellEditing();
        }

        List<StationSource> toBeUpdated = new ArrayList<>();
        for (int i : selectedRows) {
            StationSource stationSource = tableModel.getEntity(table.getRowSorter().convertRowIndexToModel(i));
            toBeUpdated.add(stationSource);
        }

        this.setEnabled(false);
        databaseManager.runUpdate(toBeUpdated, () -> UpdateStationSourceAction.this.setEnabled(true));
    }

    public void setTableModel(FilterableTableModel<StationSource> tableModel) {
        this.tableModel = tableModel;
    }

    public void setTable(JTable table) {
        this.table = table;
    }
}
