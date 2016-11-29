package org.lorainelab.igb.quickload;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.Optional;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.lorainelab.igb.dataprovider.api.DataProvider;
import static org.lorainelab.igb.utils.FXUtilities.runAndWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true, provide = QuickloadBasicAuthModal.class)
public class QuickloadBasicAuthModal extends Authenticator {

    private static final Logger LOG = LoggerFactory.getLogger(QuickloadBasicAuthModal.class);
    private Stage stage;

    @FXML
    private Label hostNameLabel;
    @FXML
    private TextField usernameTextField;
    @FXML
    private PasswordField passwordTextField;
    @FXML
    private Button cancelBtn;
    @FXML
    private Button okBtn;
    @FXML
    private CheckBox savePasswordCheckBox;
    private QuickloadSiteManager quickloadSiteManager;

    public QuickloadBasicAuthModal() {
        final URL resource = AddEditeQuickoadModal.class.getClassLoader().getResource("basicAuthModal.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setClassLoader(this.getClass().getClassLoader());
        fxmlLoader.setController(this);
        runAndWait(() -> {
            try {
                VBox root = fxmlLoader.load();
                stage = new Stage();
                stage.setResizable(false);
                stage.setAlwaysOnTop(true);
                Scene scene = new Scene(root);
                stage.setScene(scene);
                initializeBtnActions();
            } catch (IOException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        });
    }

    @Activate
    public void activate() {
        Authenticator.setDefault(this);
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        final String[] username = new String[]{null};
        final String[] password = new String[]{null};
        try {
            Optional<DataProvider> dataProvider = quickloadSiteManager.getServerFromRequestingUrl(this.getRequestingURL().toString());
            runAndWait(() -> {
                if (dataProvider.isPresent()) {
                    hostNameLabel.setText("\"" + dataProvider.get().name().get() + "\" asks for authentication:");
                } else {
                    hostNameLabel.setText(this.getRequestingURL().toExternalForm() + "asks for authentication:");
                }
                if (storedPasswordIsAvailable(dataProvider)) {
                    username[0] = dataProvider.get().getUsername().get();
                    password[0] = dataProvider.get().getPassword().get();
                } else {
                    stage.showAndWait();
                    username[0] = usernameTextField.getText().trim();
                    password[0] = passwordTextField.getText().trim();
                    //todo validate credentials here and show popup again if the credentials are not valid along with message indicating the credentials were invalid
                }
            });
            char[] chars = getPasswordCharArray(password[0]);
            if (savePasswordCheckBox.isSelected()) {
                if (dataProvider.isPresent()) {
                    dataProvider.get().setUsername(username[0], true);
                    dataProvider.get().setPassword(password[0], true);
                }
            } else {
                if (dataProvider.isPresent()) {
                    dataProvider.get().setUsername(username[0], false);
                    dataProvider.get().setPassword(password[0], false);
                }
            }
            return new PasswordAuthentication(username[0], chars);
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
            return super.getPasswordAuthentication();
        }

    }

    private char[] getPasswordCharArray(String password) throws UnsupportedEncodingException {
        final byte[] bytes = password.getBytes("UTF-8");
        final char[] chars = new char[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            chars[i] = (char) (bytes[i] & 0xff);
        }
        return chars;
    }

    private void initializeBtnActions() {
        cancelBtn.setOnAction(action -> {
            Platform.runLater(() -> {
                stage.hide();
            });
        });
        okBtn.setOnAction(action -> {
            Platform.runLater(() -> {
                stage.hide();
            });
        });
    }

    private boolean storedPasswordIsAvailable(Optional<DataProvider> dataProvider) {
        if (dataProvider.isPresent()) {
            Optional<String> login = dataProvider.get().getUsername();
            Optional<String> password = dataProvider.get().getPassword();
            return login.isPresent() && password.isPresent();
        }
        return false;
    }

    @Reference
    public void setQuickloadSiteManager(QuickloadSiteManager quickloadSiteManager) {
        this.quickloadSiteManager = quickloadSiteManager;
    }

}
