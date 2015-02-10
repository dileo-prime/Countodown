package de.doubleslash.innovationsmanagement.countodown.client.view.root.dialogStage;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import de.doubleslash.innovationsmanagement.countodown.client.view.MVCLoader;

public abstract class DialogStageLoader extends MVCLoader {
  final protected Stage stage;

  public DialogStageLoader(final Stage primaryStage) {
    this.stage = new Stage();//(StageStyle.UNDECORATED);
    stage.addEventFilter(KeyEvent.KEY_PRESSED, (event)->{
    	if(event.getCode().equals(KeyCode.ESCAPE)){
    		stage.fireEvent(new WindowEvent(stage,  WindowEvent.WINDOW_CLOSE_REQUEST));
    	}
    });
    stage.initModality(Modality.WINDOW_MODAL);
    stage.initOwner(primaryStage);

  }

  public Object showDialogStage() {
	  
    stage.setScene(new Scene(super.getView()));
    stage.setMinHeight(super.getView().minHeight(0));
    stage.setMinWidth(super.getView().minWidth(0));
    stage.setMaxHeight(super.getView().maxHeight(0));
    stage.setMaxWidth(super.getView().maxWidth(0));
    stage.showAndWait();
    return null;
  }

  @Override
  public final <T extends Node> T getView() {
    return null;
  }

}
