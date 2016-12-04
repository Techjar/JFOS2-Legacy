
package com.techjar.jfos2.network.watcher;

import com.techjar.jfos2.util.logging.LogHelper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

/**
 * @author Techjar
 */
public class FieldWatcher implements Watcher {
	protected final Object watchedObject;
	protected final int id;
	protected final Map<Integer, WatchedField> fields = new HashMap<>();

	public FieldWatcher(Object watchedObject, int id) {
		this.watchedObject = watchedObject;
		this.id = id;
	}

	/**
	 * Add field to be watched by this FieldWatcher.
	 *
	 * @param id        an index for the field, max 255
	 * @param fieldName name of the field
	 * @param synced    whether this field will be updated regularly, or only sent once
	 */
	@SneakyThrows(Exception.class)
	public void watchField(Class clazz, int id, String fieldName, boolean synced) {
		if (id < 0 || id > 255) throw new IllegalArgumentException("Invalid field ID: " + id);
		if (fields.containsKey(id)) throw new IllegalArgumentException("Field ID already in use: " + id);
		if (isWatchingField(fieldName))
			throw new IllegalArgumentException("Field \"" + fieldName + "\" is already watched!");
		Field field = clazz.getDeclaredField(fieldName);
		field.setAccessible(true);
		WatchedField watchedField = new WatchedField(id, fieldName, synced, field);
		watchedField.setLastValue(field.get(watchedObject));
		fields.put(id, watchedField);
	}

	public void unwatchField(int id) {
		fields.remove(id);
	}

	public void unwatchField(String fieldName) {
		for (Map.Entry<Integer, WatchedField> entry : fields.entrySet()) {
			if (fieldName.equals(entry.getValue().getName())) {
				fields.remove(entry.getKey());
				return;
			}
		}
	}

	public boolean isWatchingField(String fieldName) {
		for (Map.Entry<Integer, WatchedField> entry : fields.entrySet()) {
			if (fieldName.equals(entry.getValue().getName())) {
				return true;
			}
		}
		return false;
	}

	@SneakyThrows(IllegalAccessException.class)
	public Object getField(int id) {
		WatchedField field = fields.get(id);
		if (field == null) throw new IllegalArgumentException("No such field ID: " + id);
		return fields.get(id).getField().get(watchedObject);
	}

	@SneakyThrows(IllegalAccessException.class)
	public Object getField(String fieldName) {
		WatchedField field = findField(fieldName);
		if (field == null) throw new IllegalArgumentException("No such field: " + fieldName);
		return field.getField().get(watchedObject);
	}

	@SneakyThrows(IllegalAccessException.class)
	public void setField(int id, Object value) {
		WatchedField field = fields.get(id);
		if (field == null) throw new IllegalArgumentException("No such field ID: " + id);
		field.getField().set(watchedObject, value);
		field.setLastValue(value);
	}

	@SneakyThrows(IllegalAccessException.class)
	public void setField(String fieldName, Object value) {
		WatchedField field = findField(fieldName);
		if (field == null) throw new IllegalArgumentException("No such field: " + fieldName);
		field.getField().set(watchedObject, value);
		field.setLastValue(value);
	}

	public Class getFieldType(String fieldName) {
		WatchedField field = findField(fieldName);
		if (field == null) throw new IllegalArgumentException("No such field: " + fieldName);
		return field.getField().getType();
	}

	public Class getFieldType(int id) {
		WatchedField field = fields.get(id);
		if (field == null) throw new IllegalArgumentException("No such field ID: " + id);
		return field.getField().getType();
	}

	public void forceSyncField(int id) {
		WatchedField field = fields.get(id);
		if (field == null) throw new IllegalArgumentException("No such field ID: " + id);
		field.setForceSynced(true);
	}

	public void forceSyncField(String fieldName) {
		WatchedField field = findField(fieldName);
		if (field == null) throw new IllegalArgumentException("No such field: " + fieldName);
		field.setForceSynced(true);
	}

	protected WatchedField findField(String fieldName) {
		for (Map.Entry<Integer, WatchedField> entry : fields.entrySet()) {
			if (fieldName.equals(entry.getValue().getName())) {
				return entry.getValue();
			}
		}
		return null;
	}

	@Override
	@SneakyThrows(IllegalAccessException.class)
	public List<WatcherValue> getAllValues() {
		List<WatcherValue> list = new ArrayList<>(fields.size());
		for (Map.Entry<Integer, WatchedField> entry : fields.entrySet()) {
			list.add(new WatcherValue(entry.getKey(), entry.getValue().getField().get(watchedObject)));
		}
		return list;
	}

	@Override
	@SneakyThrows(IllegalAccessException.class)
	public List<WatcherValue> getChangedValues(boolean markUpdated) {
		List<WatcherValue> list = new ArrayList<>();
		for (Map.Entry<Integer, WatchedField> entry : fields.entrySet()) {
			WatchedField field = entry.getValue();
			if (field.isSynced() || field.isForceSynced()) {
				Object value = field.getField().get(watchedObject);
				if (!value.equals(field.getLastValue())) {
					list.add(new WatcherValue(entry.getKey(), value));
					if (markUpdated) {
						field.setLastValue(value);
						field.setForceSynced(false);
					}
				}
			}
		}
		return list;
	}

	@Override
	public Class getValueType(int id) {
		return getFieldType(id);
	}

	@Override
	public void setValue(WatcherValue value) {
		setField(value.getId(), value.getValue());
	}

	@Getter
	@Setter
	@RequiredArgsConstructor
	protected class WatchedField {
		private final int id;
		private final String name;
		private final boolean synced;
		private final Field field;
		private Object lastValue;
		private boolean forceSynced;
	}
}
