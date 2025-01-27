package com.youzi.tunnel.common;


import com.youzi.tunnel.common.utils.LoggerFactory;

public class LangUtil {

    private static LoggerFactory logger = LoggerFactory.getLogger();

    public static Boolean parseBoolean(Object value) {
        if (value != null) {
            if (value instanceof Boolean) {
                return (Boolean) value;
            } else if (value instanceof String) {
                return Boolean.valueOf((String) value);
            }
        }
        return null;
    }

    public static boolean parseBoolean(Object value, boolean defaultValue) {
        if (value != null) {
            if (value instanceof Boolean) {
                return (Boolean) value;
            } else if (value instanceof String) {
                try {
                    return Boolean.valueOf((String) value);
                } catch (Exception e) {
                    logger.info("parse boolean value({}) failed.", value);
                }
            }
        }
        return defaultValue;
    }

    /**
     * @param value Integer或String值
     * @return Integer 返回类型
     * @Title: parseInt
     * @Description: Int解析方法，可传入Integer或String值
     */
    public static Integer parseInt(Object value) {
        if (value != null) {
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof String) {
                return Integer.valueOf((String) value);
            }
        }
        return null;
    }

    public static Integer parseInt(Object value, Integer defaultValue) {
        if (value != null) {
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof String) {
                try {
                    return Integer.valueOf((String) value);
                } catch (Exception e) {
                    logger.info("parse Integer value({}) failed.", value);
                }
            }
        }
        return defaultValue;
    }

    /***
     *
     * @Title: parseLong
     * @Description: long解析方法，可传入Long或String值
     * @param value Integer或String值
     * @param @return
     * @return Long 返回类型
     */
    public static Long parseLong(Object value) {
        if (value != null) {
            if (value instanceof Long) {
                return (Long) value;
            } else if (value instanceof String) {
                return Long.valueOf((String) value);
            }
        }
        return null;
    }

    public static Long parseLong(Object value, Long defaultValue) {
        if (value != null) {
            if (value instanceof Long) {
                return (Long) value;
            } else if (value instanceof String) {
                try {
                    return Long.valueOf((String) value);
                } catch (NumberFormatException e) {
                    logger.info("parse Long value({}) failed.", value);
                }
            }
        }
        return defaultValue;
    }

    /**
     * @param value Double或String值
     * @return Double 返回类型
     * @Title: parseDouble
     * @Description: Double解析方法，可传入Double或String值
     */
    public static Double parseDouble(Object value) {
        if (value != null) {
            if (value instanceof Double) {
                return (Double) value;
            } else if (value instanceof String) {
                return Double.valueOf((String) value);
            }
        }
        return null;
    }

    /**
     * @param value Double或String值
     * @return Double 返回类型
     * @Title: parseDouble
     * @Description: Double解析方法，可传入Double或String值
     */
    public static Double parseDouble(Object value, Double defaultValue) {
        if (value != null) {
            if (value instanceof Double) {
                return (Double) value;
            } else if (value instanceof String) {
                try {
                    return Double.valueOf((String) value);
                } catch (NumberFormatException e) {
                    logger.info("parse Double value({}) failed.", value);
                }
            }
        }
        return defaultValue;
    }
}
