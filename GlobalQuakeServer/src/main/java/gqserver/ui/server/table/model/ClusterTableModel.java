package gqserver.ui.server.table.model;

import globalquake.core.earthquake.data.Cluster;
import globalquake.ui.table.Column;
import globalquake.ui.table.FilterableTableModel;
import globalquake.ui.table.TableCellRendererAdapter;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ClusterTableModel extends FilterableTableModel<Cluster> {
    private final List<Column<Cluster, ?>> columns = List.of(
            Column.readonly("ID", UUID.class, Cluster::getUuid, new TableCellRendererAdapter<>()),
            Column.readonly("已分配事件", Integer.class, cluster -> cluster.getAssignedEvents().size(), new TableCellRendererAdapter<>()),
            Column.readonly("等级", Integer.class, Cluster::getLevel, new TableCellRendererAdapter<>()),
            Column.readonly("所在纬度", Double.class, Cluster::getRootLat, new TableCellRendererAdapter<>()),
            Column.readonly("所在经度", Double.class, Cluster::getRootLon, new TableCellRendererAdapter<>()));


    public ClusterTableModel(Collection<Cluster> data) {
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
        Cluster event = getEntity(rowIndex);
        return columns.get(columnIndex).getValue(event);
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        Cluster event = getEntity(rowIndex);
        columns.get(columnIndex).setValue(value, event);
    }
}
