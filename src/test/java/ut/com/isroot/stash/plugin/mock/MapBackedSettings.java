package ut.com.isroot.stash.plugin.mock;

import com.atlassian.bitbucket.setting.Settings;
import com.google.common.base.MoreObjects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Sean Ford
 * @since 2015-09-14
 */
public class MapBackedSettings implements Settings {
    private final Map<String, Object> values;

    public MapBackedSettings() {
        this.values = new HashMap<>();
    }

    public MapBackedSettings(Map<String, Object> values) {
        this.values = new HashMap<>(values);
    }

    public void set(String key, Object value) {
        values.put(key, value);
    }

    @SuppressWarnings("unchecked")
    private <T> T get(String key) {
        checkNotNull(key, "key");
        return (T) values.get(key);
    }

    @Nullable
    @Override
    public String getString(String key) {
        return get(key);
    }

    @Nonnull
    @Override
    public String getString(String key, String defaultValue) {
        return MoreObjects.firstNonNull(getString(key), defaultValue);
    }

    @Nullable
    @Override
    public Boolean getBoolean(String s) {
        return (Boolean)values.get(s);
    }

    @Override
    public boolean getBoolean(String s, boolean defaultValue) {
        Boolean value = getBoolean(s);
        return MoreObjects.firstNonNull(value, defaultValue);
    }

    @Nullable
    @Override
    public Integer getInt(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getInt(String key, int defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Long getLong(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getLong(String key, long defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public Double getDouble(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> asMap() {
        return values;
    }
}
