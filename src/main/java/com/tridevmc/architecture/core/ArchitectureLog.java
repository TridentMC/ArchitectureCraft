package com.tridevmc.architecture.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ArchitectureLog {

    public static final Logger LOG = LogManager.getLogger("ArchitectureCraft");
    public static void debug(Object message) { LOG.debug(message); }

    public static void debug(Object message, Throwable cause) { LOG.debug(message, cause); }

    public static void debug(String message) { LOG.debug(message); }

    public static void debug(String message, Object... params) { LOG.debug(message, params); }

    public static void debug(String message, Throwable cause) { LOG.debug(message, cause); }

    public static void error(Object message) {
        LOG.error(message);
    }

    public static void error(Object message, Throwable t) {
        LOG.error(message, t);
    }

    public static void error(String message) {
        LOG.error(message);
    }

    public static void error(String message, Object... params) {
        LOG.error(message, params);
    }


    public static void error(String message, Throwable t) {
        LOG.error(message, t);
    }

    public static void fatal(Object message) {
        LOG.fatal(message);
    }

    public static void fatal(Object message, Throwable t) {
        LOG.fatal(message, t);
    }

    public static void fatal(String message) {
        LOG.fatal(message);
    }

    public static void fatal(String message, Object... params) {
        LOG.fatal(message, params);
    }

    public static void fatal(String message, Throwable t) {
        LOG.fatal(message, t);
    }

    public static void info(Object message) {
        LOG.info(message);
    }

    public static void info(Object message, Throwable t) {
        LOG.info(message, t);
    }

    public static void info(String message) {
        LOG.info(message);
    }

    public static void info(String message, Object... params) {
        LOG.info(message, params);
    }

    public static void info(String message, Throwable t) {
        LOG.info(message, t);
    }

    public static void warn(Object message) {
        LOG.warn(message);
    }

    public static void warn(Object message, Throwable t) {
        LOG.warn(message, t);
    }

    public static void warn(String message) {
        LOG.warn(message);
    }

    public static void warn(String message, Object... params) {
        LOG.warn(message, params);
    }

}