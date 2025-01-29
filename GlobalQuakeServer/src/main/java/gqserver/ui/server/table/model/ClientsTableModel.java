package gqserver.ui.server.table.model;

import globalquake.ui.table.Column;
import globalquake.ui.table.FilterableTableModel;
import globalquake.ui.table.LastUpdateRenderer;
import globalquake.ui.table.TableCellRendererAdapter;
import gqserver.api.ServerClient;

import java.time.LocalDateTime;
import java.util.List;

public class ClientsTableModel extends FilterableTableModel<ServerClient> {
    private final List<Column<ServerClient, ?>> columns = List.of(
            Column.readonly("ID", Integer.class, ServerClient::getID, new TableCellRendererAdapter<>()),
            Column.readonly("加入时间", LocalDateTime.class, ServerClient::getJoinDate, new LastUpdateRenderer<>()),
            Column.readonly("延迟 (ms)", Long.class, ServerClient::getDelay, new TableCellRendererAdapter<>()),
            Column.readonly("数据包发送数", Long.class, ServerClient::getSentPackets, new TableCellRendererAdapter<>()),
            Column.readonly("数据包接受数", Long.class, ServerClient::getReceivedPackets, new TableCellRendererAdapter<>()));


    public ClientsTableModel(List<ServerClient> data) {
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
        ServerClient event = getEntity(rowIndex);
        return columns.get(columnIndex).getValue(event);
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        ServerClient event = getEntity(rowIndex);
        columns.get(columnIndex).setValue(value, event);
    }
}
