package com.axreng.backend.utils;

import java.util.UUID;

public class IdGenerator {
    public static String generateRandomId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
