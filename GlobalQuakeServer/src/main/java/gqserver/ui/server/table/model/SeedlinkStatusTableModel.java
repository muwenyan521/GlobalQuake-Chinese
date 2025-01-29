package gqserver.ui.server.table.model;

import globalquake.core.database.SeedlinkNetwork;
import globalquake.ui.table.Column;
import globalquake.ui.table.FilterableTableModel;
import globalquake.ui.table.TableCellRendererAdapter;

import java.util.List;

public class SeedlinkStatusTableModel extends FilterableTableModel<SeedlinkNetwork> {
    private final List<Column<SeedlinkNetwork, ?>> columns = List.of(
            Column.readonly("名称", String.class, SeedlinkNetwork::getName, new TableCellRendererAdapter<>()),
            Column.readonly("地址", String.class, SeedlinkNetwork::getHost, new TableCellRendererAdapter<>()),
            Column.readonly("端口", Integer.class, SeedlinkNetwork::getPort, new TableCellRendererAdapter<>()),
            Column.readonly("可用台站数", Integer.class, SeedlinkNetwork::getAvailableStations, new TableCellRendererAdapter<>()),
            Column.readonly("选择台站数", Integer.class, SeedlinkNetwork::getSelectedStations, new TableCellRendererAdapter<>()),
            Column.readonly("已连接台站数", Integer.class, SeedlinkNetwork::getConnectedStations, new TableCellRendererAdapter<>()));

    public SeedlinkStatusTableModel(List<SeedlinkNetwork> data) {
        super(data);
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columns.get(columnIndex).getName();
    }

    @Override
    public TableCellRendererAdapter<?, ?> getColumnRenderer(int columnIndex) {
        return columns.get(columnIndex).getRenderer();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columns.get(columnIndex).getColumnType();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columns.get(columnIndex).isEditable();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        SeedlinkNetwork event = getEntity(rowIndex);
        return columns.get(columnIndex).getValue(event);
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        SeedlinkNetwork event = getEntity(rowIndex);
        columns.get(columnIndex).setValue(value, event);
    }
}
