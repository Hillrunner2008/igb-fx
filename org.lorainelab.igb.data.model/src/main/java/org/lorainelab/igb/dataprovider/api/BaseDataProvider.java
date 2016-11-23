package org.lorainelab.igb.dataprovider.api;

import com.google.common.base.Charsets;
import static com.google.common.base.Preconditions.checkNotNull;
import com.google.common.base.Strings;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import java.util.Objects;
import java.util.Optional;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import static org.lorainelab.igb.dataprovider.api.DataProviderPrefKeys.IS_EDITABLE;
import static org.lorainelab.igb.dataprovider.api.DataProviderPrefKeys.LOAD_PRIORITY;
import static org.lorainelab.igb.dataprovider.api.DataProviderPrefKeys.LOGIN;
import static org.lorainelab.igb.dataprovider.api.DataProviderPrefKeys.MIRROR_URL;
import static org.lorainelab.igb.dataprovider.api.DataProviderPrefKeys.PASSWORD;
import static org.lorainelab.igb.dataprovider.api.DataProviderPrefKeys.PRIMARY_URL;
import static org.lorainelab.igb.dataprovider.api.DataProviderPrefKeys.PROVIDER_NAME;
import static org.lorainelab.igb.dataprovider.api.DataProviderPrefKeys.STATUS;
import static org.lorainelab.igb.dataprovider.api.ResourceStatus.Disabled;
import static org.lorainelab.igb.dataprovider.api.ResourceStatus.Initialized;
import static org.lorainelab.igb.dataprovider.api.ResourceStatus.NotInitialized;
import org.lorainelab.igb.preferences.PreferenceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public abstract class BaseDataProvider implements DataProvider {

    private static final Logger LOG = LoggerFactory.getLogger(BaseDataProvider.class);
    private Preferences preferencesNode;
    protected final StringProperty url;
    protected String mirrorUrl;
    protected StringProperty name;
    protected String login;
    protected String password;
    protected IntegerProperty loadPriority;
    protected ResourceStatus status;
    protected boolean useMirror;
    protected final boolean isEditable;

    public BaseDataProvider(String url, String name, String mirrorUrl, boolean isEditable, int loadPriority) {
        this.url = new SimpleStringProperty(checkNotNull(url));
        this.name = new SimpleStringProperty(checkNotNull(name));
        this.loadPriority = new SimpleIntegerProperty(loadPriority);
        this.mirrorUrl = checkNotNull(mirrorUrl);
        this.isEditable = isEditable;
        status = NotInitialized;
        preferencesNode = getDataProviderNode(url);
        setupPropertyListeners();
        loadPersistedConfiguration();
        initializePreferences();
    }

    public BaseDataProvider(String url, String name, boolean isEditable, int loadPriority) {
        this.url = new SimpleStringProperty(checkNotNull(url));
        this.name = new SimpleStringProperty(checkNotNull(name));
        this.loadPriority = new SimpleIntegerProperty(loadPriority);
        this.isEditable = isEditable;
        status = NotInitialized;
        preferencesNode = getDataProviderNode(url);
        setupPropertyListeners();
        loadPersistedConfiguration();
        initializePreferences();
    }

    private void loadPersistedConfiguration() {
        Optional.ofNullable(preferencesNode.get(PROVIDER_NAME, null)).ifPresent(preferenceValue -> name.set(preferenceValue));
        Optional.ofNullable(preferencesNode.get(LOAD_PRIORITY, null)).ifPresent(preferenceValue -> loadPriority.set(Integer.parseInt(preferenceValue)));
        Optional.ofNullable(preferencesNode.get(MIRROR_URL, null)).ifPresent(preferenceValue -> mirrorUrl = preferenceValue);
        Optional.ofNullable(preferencesNode.get(LOGIN, null)).ifPresent(preferenceValue -> {
            if (!Strings.isNullOrEmpty(preferenceValue)) {
                login = preferenceValue;
            }
        });
        Optional.ofNullable(preferencesNode.get(PASSWORD, null)).ifPresent(preferenceValue -> {
            if (!Strings.isNullOrEmpty(preferenceValue)) {
                password = preferenceValue;
            }
        });
        Optional.ofNullable(preferencesNode.get(STATUS, null)).ifPresent(preferenceValue -> {
            ResourceStatus.fromName(preferenceValue).ifPresent(matchingStatus -> status = matchingStatus);
            if (status == Initialized) {
                status = NotInitialized;
            }
        });
    }

    private void initializePreferences() {
        preferencesNode.put(PRIMARY_URL, url.get());
        preferencesNode.put(PROVIDER_NAME, name.get());
        preferencesNode.putInt(LOAD_PRIORITY, loadPriority.get());
        preferencesNode.putBoolean(IS_EDITABLE, isEditable);
        if (!Strings.isNullOrEmpty(mirrorUrl)) {
            preferencesNode.put(MIRROR_URL, mirrorUrl);
        }
        flushPrefNode();
    }

    protected abstract void disable();

    @Override
    public boolean isEditable() {
        return isEditable;
    }

    @Override
    public StringProperty name() {
        return name;
    }

    @Override
    public IntegerProperty loadPriority() {
        return loadPriority;
    }

    @Override
    public StringProperty url() {
        return url;
    }

    @Override
    public Optional<String> getUsername() {
        return Optional.ofNullable(login);
    }

    @Override
    public void setUsername(String login, boolean saveCredential) {
        this.login = login;
        if (login == null) {
            preferencesNode.remove(LOGIN);
        } else if (saveCredential) {
            preferencesNode.put(LOGIN, login);
        }
        flushPrefNode();
    }

    @Override
    public Optional<String> getPassword() {
        return Optional.ofNullable(password);
    }

    @Override
    public void setPassword(String password, boolean saveCredential) {
        this.password = password;
        if (password == null) {
            preferencesNode.remove(PASSWORD);
        } else if (saveCredential) {
            if (password.isEmpty()) {
                preferencesNode.put(PASSWORD, "");
            } else {
                preferencesNode.put(PASSWORD, password);
            }
        }
        flushPrefNode();
    }

    @Override
    public ResourceStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(ResourceStatus status) {
        this.status = status;
        preferencesNode.put(STATUS, status.toString());
        flushPrefNode();
        if (status == Disabled) {
            useMirror = false;
            disable();
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.url);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BaseDataProvider other = (BaseDataProvider) obj;
        if (!Objects.equals(this.url, other.url)) {
            return false;
        }
        return true;
    }

    public static Optional<Preferences> getDataProviderNodeIfExist(String url) {
        final String convertUrlToHash = convertUrlToHash(url);
        final Preferences rootNode = PreferenceUtils.getClassPrefsNode(BaseDataProvider.class);
        try {
            if (rootNode.nodeExists(convertUrlToHash)) {
                return Optional.ofNullable(rootNode.node(convertUrlToHash));
            }
        } catch (BackingStoreException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return Optional.empty();
    }

    public static Preferences getDataProviderNode(String url) {
        return PreferenceUtils.getClassPrefsNode(BaseDataProvider.class).node(convertUrlToHash(url));
    }

    private static String convertUrlToHash(String url) {
        HashFunction hf = Hashing.md5();
        HashCode hc = hf.newHasher().putString(url, Charsets.UTF_8).hash();
        return hc.toString();
    }

    private void setupPropertyListeners() {
        nameChangeListener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                preferencesNode.put(PROVIDER_NAME, newValue);
                flushPrefNode();
            }
        };
        name.addListener(new WeakChangeListener<>(nameChangeListener));
        urlChangeListener = new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                preferencesNode.put(PRIMARY_URL, newValue);
                flushPrefNode();
            }
        };
        url.addListener(new WeakChangeListener<>(urlChangeListener));
        loadPriorityChangeListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                preferencesNode.putInt(LOAD_PRIORITY, newValue.intValue());
                flushPrefNode();
            }
        };
        loadPriority.addListener(new WeakChangeListener<>(loadPriorityChangeListener));
    }
    private ChangeListener<Number> loadPriorityChangeListener;
    private ChangeListener<String> urlChangeListener;
    private ChangeListener<String> nameChangeListener;

    private void flushPrefNode() {
        try {
            preferencesNode.flush();
        } catch (BackingStoreException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

}
