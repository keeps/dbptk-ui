package com.databasepreservation.common.client.common.lists.cells;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;

/**
 * Edit and delete cells, in a way that they can be added to a CompositeCell.
 * based on this code http://stackoverflow.com/a/9119496/1483200
 */
public class ActionsCell<T> implements HasCell<T, T> {
	private FontAwesomeActionCell<T> cell;

	public ActionsCell(String tooltip, String icon, String extraButtonClasses,
										 FontAwesomeActionCell.Delegate<T> delegate) {
		cell = new FontAwesomeActionCell<>(tooltip, icon, extraButtonClasses, delegate);
	}

	public ActionsCell(String tooltip, String icon, FontAwesomeActionCell.Delegate<T> delegate) {
		cell = new FontAwesomeActionCell<>(tooltip, icon, delegate);
	}

	@Override
	public Cell<T> getCell() {
		return cell;
	}

	@Override
	public FieldUpdater<T, T> getFieldUpdater() {
		return null;
	}

	@Override
	public T getValue(T object) {
		return object;
	}
}
