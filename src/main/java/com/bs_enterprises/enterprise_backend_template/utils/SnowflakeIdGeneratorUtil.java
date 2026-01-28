package com.bs_enterprises.enterprise_backend_template.utils;

import cn.ipokerface.snowflake.SnowflakeIdGenerator;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Snowflake ID Generator utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SnowflakeIdGeneratorUtil {

    // Initialize the SnowflakeIdGenerator with worker ID and datacenter ID
    private static final SnowflakeIdGenerator snowflakeIdGenerator;

    static {
        // Create the SnowflakeIdGenerator instance (worker ID, datacenter ID)
        // Typically workerId and datacenterId should be unique per server/machine
        snowflakeIdGenerator = new SnowflakeIdGenerator(1, 1); // Example values, adjust as needed
    }

    /**
     * Generate a new unique Snowflake ID.
     * 
     * @return The generated Snowflake ID.
     */
    public static long generateId() {
        return snowflakeIdGenerator.nextId();
    }

    public static long generateId(String previousId) {
        long newId = snowflakeIdGenerator.nextId();

        // If previousId is null or empty, just return the new ID
        if (previousId == null || previousId.isEmpty()) {
            return newId;
        }

        // Compare with previous ID
        if (String.valueOf(newId).equals(previousId)) {
            // Recursively generate until a different ID is produced
            return generateId(previousId);
        }

        return newId;
    }

}
