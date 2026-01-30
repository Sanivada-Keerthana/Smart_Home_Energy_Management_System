package com.srems.srems.model;


import java.util.Set;

public class RolePermissions {

    public static Set<Permission> get(Role role) {

        return switch (role) {

            case FLAT_OWNER -> Set.of(
                    Permission.VIEW_DEVICE,
                    Permission.ADD_DEVICE,
                    Permission.UPDATE_DEVICE,
                    Permission.DELETE_DEVICE,
                    Permission.TOGGLE_DEVICE,
                    Permission.APPROVE_USER
            );

            case SECRETARY -> Set.of(
                    Permission.VIEW_DEVICE,
                    Permission.ADD_DEVICE,
                    Permission.UPDATE_DEVICE,
                    Permission.DELETE_DEVICE,
                    Permission.TOGGLE_DEVICE,
                    Permission.APPROVE_USER
            );

            case SECURITY -> Set.of(
                    Permission.VIEW_DEVICE,
                    Permission.TOGGLE_DEVICE
            );

            case FLAT_MEMBER -> Set.of(
                    Permission.VIEW_DEVICE,
                    Permission.TOGGLE_DEVICE
            );

            case FLAT_GUEST -> Set.of(
                    Permission.VIEW_DEVICE
            );

            case STAFF -> Set.of(
                    Permission.VIEW_DEVICE
            );

            default -> Set.of();
        };
    }
}