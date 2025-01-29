package globalquake.ui.table;

import globalquake.core.database.StationSource;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.List;

public class StationSourcesTableModel extends FilterableTableModel<StationSource>{
    private final List<Column<StationSource, ?>> columns = List.of(
            Column.readonly("名称", String.class, StationSource::getName, new TableCellRendererAdapter<>()),
            Column.readonly("网址", String.class, StationSource::getUrl, new TableCellRendererAdapter<>()),
            Column.readonly("最后更新时间", LocalDateTime.class, StationSource::getLastUpdate, new LastUpdateRenderer<>()),
            Column.readonly("状态", JProgressBar.class, StationSource::getStatus, new ProgressBarRenderer<>()));

    public StationSourcesTableModel(List<StationSource> data) {
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
        StationSource event = getEntity(rowIndex);
        return columns.get(columnIndex).getValue(event);
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        StationSource event = getEntity(rowIndex);
        columns.get(columnIndex).setValue(value, event);
    }
}
