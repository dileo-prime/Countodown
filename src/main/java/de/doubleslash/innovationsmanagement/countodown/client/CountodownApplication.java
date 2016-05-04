package de.doubleslash.innovationsmanagement.countodown.client;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.doubleslash.innovationsmanagement.countodown.backend.EntryPointBackend;
import de.doubleslash.innovationsmanagement.countodown.client.view.root.rootLayout.RootLayoutController;
import de.doubleslash.innovationsmanagement.countodown.client.view.root.rootLayout.RootLayoutLoader;

public class CountodownApplication extends Application {

  private static final String IMAGE_URL = CountodownApplication.class.getResource(
      "logo_doubleSlash.png").toExternalForm();
  private final static Logger logger = LoggerFactory.getLogger(CountodownApplication.class);

  private volatile static EntryPointBackend backend = null;

  @Override
  public void start(final Stage primaryStage) throws Exception {
    primaryStage.setTitle("Countodown");
    // sometimes it doesn't work, seems to be an eclipse problem
    primaryStage.getIcons().add(new Image(IMAGE_URL));

    final RootLayoutLoader rootViewLoader = new RootLayoutLoader(primaryStage, backend);
    final RootLayoutController rootController = rootViewLoader.getController();
    primaryStage.setOnCloseRequest(rootController);

    rootViewLoader.showIn(primaryStage);
    logger.debug("show PrimaryStage");

  }

  @Override
  public void init() {}

  /**
   * launches the Application with given Parameters, will block until application has finished
   *
   * @param backend
   *          the parameters of this Singleton
   */
  public static void initialize(final EntryPointBackend backend) {
    CountodownApplication.backend = backend;
    launch(CountodownApplication.class, new String[0]);
  }

}
