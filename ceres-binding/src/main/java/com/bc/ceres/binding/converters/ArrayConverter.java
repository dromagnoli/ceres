package com.bc.ceres.binding.converters;

import com.bc.ceres.binding.ConversionException;
import com.bc.ceres.binding.Converter;

import java.lang.reflect.Array;
import java.util.StringTokenizer;

public class ArrayConverter implements Converter {

    private static final String SEP = ",";
    private static final String SEP_ESC = "\\u002C"; // Unicode escape repr. of ','
    private Class arrayType;
    private Converter componentConverter;

    public ArrayConverter(Class arrayType, Converter componentConverter) {
        this.arrayType = arrayType;
        this.componentConverter = componentConverter;
    }

    public Class<?> getValueType() {
        return arrayType;
    }

    public Object parse(String text) throws ConversionException {
        if (text.isEmpty()) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(text, SEP);
        int length = st.countTokens();
        Object array = Array.newInstance(arrayType.getComponentType(), length);
        for (int i = 0; i < length; i++) {
            Object component = componentConverter.parse(st.nextToken().replace(SEP_ESC, SEP));
            Array.set(array, i, component);
        }
        return array;
    }

    public String format(Object array) {
        if (array == null) {
            return "";
        }
        int length = Array.getLength(array);
        StringBuilder sb = new StringBuilder(length * 4);
        for (int i = 0; i < length; i++) {
            Object component = Array.get(array, i);
            if (i > 0) {
                sb.append(',');
            }
            sb.append(componentConverter.format(component).replace(SEP, SEP_ESC));
        }
        return sb.toString();
    }
}