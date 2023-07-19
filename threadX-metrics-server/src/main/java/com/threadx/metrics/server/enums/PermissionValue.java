package com.threadx.metrics.server.enums;

/**
 * 权限信息
 *
 * @author huangfukexing
 * @date 2023/6/7 14:00
 */
public enum PermissionValue {
    /**
     * 添加用户
     */
    USER_ADD("add:user","添加用户"),
    /**
     * 修改用户信息
     */
    USER_UPDATE("update:user","修改用户信息"),
    /**
     * 查询所有用户信息
     */
    FIND_ALL_USER("findAll:user","查询所有用户信息"),

    /**
     * 冻结用户
     */
    USER_DISABLE("disable:user","冻结用户"),

    /**
     * 启用用户
     */
    USER_ENABLE("enable:user","启用用户"),
    ;
    /**
     * 权限主键
     */
    private final String permissionKey;
    /**
     * 权限介绍
     */
    private final String permissionDesc;


    PermissionValue(String permissionKey, String permissionDesc) {
        this.permissionKey = permissionKey;
        this.permissionDesc = permissionDesc;
    }

    public String getPermissionKey() {
        return permissionKey;
    }

    public String getPermissionDesc() {
        return permissionDesc;
    }
}
