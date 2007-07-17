package com.bc.ceres.binding;

import java.util.*;
import java.util.regex.Pattern;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class ValueDefinition {
    private final String name;
    private final Class<?> type;
    private Map<String, Object> properties;
    private PropertyChangeSupport propertyChangeSupport;

    public ValueDefinition(String name, Class<?> type) {
        this(name, type, new HashMap<String, Object>(8));
    }

    public ValueDefinition(String name, Class<?> type, Map<String, Object> properties) {
        this.name = name;
        this.type = type;
        this.properties = properties;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public String getDisplayName() {
        return (String) getProperty("displayName");
    }

    public void setDisplayName(String displayName) {
        setProperty("displayName", displayName);
    }

    public String getUnit() {
        return (String) getProperty("unit");
    }

    public void setUnit(String unit) {
        setProperty("unit", unit);
    }

    public String getDescription() {
        return (String) getProperty("description");
    }

    public void setDescription(String description) {
        setProperty("description", description);
    }

    public boolean isNotNull() {
        return getBooleanProperty("notNull");
    }

    public void setNotNull(boolean notNull) {
        setProperty("notNull", notNull);
    }

    public boolean isNotEmpty() {
        return getBooleanProperty("notEmpty");
    }

    public void setNotEmpty(boolean notEmpty) {
        setProperty("notEmpty", notEmpty);
    }

    public String getFormat() {
        return (String) getProperty("format");
    }

    public void setFormat(String format) {
        setProperty("format", format);
    }

    public Interval getInterval() {
        return (Interval) getProperty("interval");
    }

    public void setInterval(Interval interval) {
        setProperty("interval", interval);
    }

    public Pattern getPattern() {
        return (Pattern) getProperty("pattern");
    }

    public Object getDefaultValue() {
        return getProperty("defaultValue");
    }

    public void setDefaultValue(Object defaultValue) {
        setProperty("defaultValue", defaultValue);
    }

    public void setPattern(Pattern pattern) {
        setProperty("pattern", pattern);
    }

    public ValueSet getValueSet() {
        return (ValueSet) getProperty("valueSet");
    }

    public void setValueSet(ValueSet valueSet) {
        setProperty("valueSet", valueSet);
    }

    public Converter getConverter() {
        return getConverter(false);
    }

    public Converter getConverter(boolean notNull) {
        final Converter converter = (Converter) getProperty("converter");
        if (converter == null && notNull) {
            throw new IllegalStateException("no converter defined for value '" + getName() + "'");
        }
        return converter;
    }

    public void setConverter(Converter converter) {
        setProperty("converter", converter);
    }

    public Validator getValidator() {
        return(Validator) getProperty("validator");
    }

    public void setValidator(Validator validator) {
        setProperty("validator", validator);
    }

    //////////////////////////////////////////////////////////////////////////////

    public Object getProperty(String name) {
        return properties.get(name);
    }

    public void setProperty(String name, Object value) {
        Object oldValue = getProperty(name);
        if (value != null) {
            properties.put(name, value);
        } else {
            properties.remove(name);
        }
        if (!equals(oldValue, value)) {
            firePropertyChange(name, oldValue, value);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (propertyChangeSupport != null) {
            propertyChangeSupport.removePropertyChangeListener(listener);
        }
    }

    public PropertyChangeListener[] getPropertyChangeListeners() {
        if (propertyChangeSupport == null) {
            return new PropertyChangeListener[0];
        }
        return this.propertyChangeSupport.getPropertyChangeListeners();
    }

    private void firePropertyChange(String propertyName, Object newValue, Object oldValue) {
        if (propertyChangeSupport == null) {
            return;
        }
        PropertyChangeListener[] propertyChangeListeners = getPropertyChangeListeners();
        PropertyChangeEvent evt = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
        for (PropertyChangeListener propertyChangeListener : propertyChangeListeners) {
            propertyChangeListener.propertyChange(evt);
        }
    }

    private static boolean equals(Object a, Object b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.equals(b);
    }

    private boolean getBooleanProperty(String name) {
        Object v = getProperty(name);
        return v != null && (Boolean) v;
    }
}