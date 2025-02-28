package globalquake.ui.table;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class Column<E, T> {

    private final String name;
    private final Function<E, T> valueGetter;
    private final Class<T> columnType;
	private final TableCellRendererAdapter<E, ?> renderer;

    private Column(String name, Class<T> columnClass, Function<E, T> valueGetter, TableCellRendererAdapter<E, ?> renderer) {
        this.name = Objects.requireNonNull(name, "名称不能为空");
        this.columnType = Objects.requireNonNull(columnClass, "列类不能为空");
        this.valueGetter = Objects.requireNonNull(valueGetter, "值获取器不能为空");
        this.renderer = Objects.requireNonNull(renderer, "渲染器不能为空");
    }	

    @SuppressWarnings("unused")
    public static <E, T> Column<E, T> editable(String name, Class<T> columnClass, Function<E, T> valueGetter,
                                               BiConsumer<E, T> valueSetter, TableCellRendererAdapter<E, ?> renderer) {
        return new Editable<>(name, columnClass, valueGetter, valueSetter, renderer);
    }

    public static <E, T> Column<E, T> readonly(String name, Class<T> columnClass, Function<E, T> valueGetter, TableCellRendererAdapter<E, T> renderer) {
        return new ReadOnly<>(name, columnClass, valueGetter, renderer);
    }

    public abstract boolean isEditable();

    public abstract void setValue(Object value, E entity);

    public T getValue(E entity) {
        return valueGetter.apply(entity);
    }

    public String getName() {
        return name;
    }

    public Class<T> getColumnType() {
        return columnType;
    }
    
    public TableCellRendererAdapter<E, ?> getRenderer() {
		return renderer;
	}

    private static class ReadOnly<E, T> extends Column<E, T> {

        private ReadOnly(String name, Class<T> columnClass, Function<E, T> valueGetter, TableCellRendererAdapter<E, ?> renderer) {
            super(name, columnClass, valueGetter, renderer);
        }

        @Override
        public boolean isEditable() {
            return false;
        }

        @Override
        public void setValue(Object value, E entity) {
            throw new UnsupportedOperationException("列'" + getName() + "'不可编辑");
        }
    }

    @SuppressWarnings("unused")
    private static class Editable<E, T> extends Column<E, T> {

        private final BiConsumer<E, T> valueSetter;

        private Editable(String name, Class<T> columnClass, Function<E, T> valueGetter, BiConsumer<E, T> valueSetter, TableCellRendererAdapter<E, ?> renderer) {
            super(name, columnClass, valueGetter, renderer);
            this.valueSetter = Objects.requireNonNull(valueSetter, "值设置器不能为null");
        }

        @Override
        public boolean isEditable() {
            return true;
        }

        @Override
        public void setValue(Object value, E entity) {
            valueSetter.accept(entity, getColumnType().cast(value));
        }
    }
}

