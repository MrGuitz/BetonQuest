package org.betonquest.betonquest.modules.config.transformer;

import org.betonquest.betonquest.modules.config.PatchException;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renames a value for a given key if the given regex matches.
 */
public class ValueRenameTransformation implements PatchTransformation {

    /**
     * Default constructor
     */
    public ValueRenameTransformation() {
    }

    @Override
    public void transform(final Map<String, String> options, final ConfigurationSection config) throws PatchException {
        final String key = options.get("key");

        final Object value = config.get(key);
        if (value == null) {
            throw new PatchException("The key '" + key + "' did not exist, skipping transformation.");
        }

        final String regex = options.get("oldValueRegex");
        final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        final String newEntry = options.get("newValue");

        final Matcher matcher = pattern.matcher(value.toString());
        if (matcher.matches()) {
            config.set(key, newEntry);
        } else {
            throw new PatchException("Value does not match the given regex, skipping transformation.");
        }
    }
}
