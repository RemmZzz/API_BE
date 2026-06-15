package com.apibe.API_BE.module.database.util;

public class DatabaseTypeParser {

    public static ColumnTypeInfo parse(String type) {
        if (type == null || type.isBlank()) {
            return new ColumnTypeInfo("VARCHAR", 255, null, null);
        }
        
        type = type.trim();
        
        if (type.contains("(")) {
            int openParen = type.indexOf("(");
            int closeParen = type.indexOf(")");
            if (closeParen > openParen) {
                String baseType = type.substring(0, openParen).trim().toUpperCase();
                String paramsStr = type.substring(openParen + 1, closeParen).trim();
                
                if (paramsStr.contains(",")) {
                    String[] params = paramsStr.split(",");
                    Integer precision = tryParseInt(params[0].trim());
                    Integer scale = tryParseInt(params[1].trim());
                    return new ColumnTypeInfo(baseType, null, precision, scale);
                } else {
                    Integer length = tryParseInt(paramsStr);
                    return new ColumnTypeInfo(baseType, length, null, null);
                }
            }
        }
        
        return new ColumnTypeInfo(type.toUpperCase(), null, null, null);
    }

    public static String format(String dataType, Integer length, Integer precision, Integer scale) {
        if (dataType == null || dataType.isBlank()) {
            return "VARCHAR(255)";
        }
        
        String upper = dataType.toUpperCase();
        if (length != null) {
            return upper + "(" + length + ")";
        }
        if (precision != null && scale != null) {
            return upper + "(" + precision + "," + scale + ")";
        }
        return upper;
    }

    private static Integer tryParseInt(String val) {
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
