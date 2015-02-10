package de.doubleslash.innovationsmanagement.countodown.client.view.util.multiValues;

import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

public class MultiValuesController implements Initializable {

  private static final String MINUS = "minus";
  private static final String PLUS = "plus";

  private String minus_Sign;
  private String plus_Sign;

  @FXML
  TextField orig;

  @FXML
  VBox root;

  @FXML
  Button button;

  AtomicInteger emptyFields = new AtomicInteger(0);

  LinkedList<Pair<TextField, BooleanBinding>> textList = new LinkedList<>();

  ChangeListener<Boolean> listener = (observerable, oldV, newV) -> {
    if (oldV != newV) {
      final boolean someEmpty = emptyFields.addAndGet(newV ? +1 : -1) > 0; // if empty -> +=1
      if (someEmpty) {
        if (textList.size() == 1) {
          setEmpty();
        } else {
          setMinus();
        }
      } else {
        setPlus();
      }
    }
  };

  @Override
  public void initialize(final URL location, final ResourceBundle resources) {
    minus_Sign = resources.getString(MINUS);
    plus_Sign = resources.getString(PLUS);
    initialize();
  }

  private void initialize() {
    textList.add(new Pair<>(orig, addListenerToTextField(orig)));
    emptyFields.incrementAndGet();
    setEmpty();
  }

  private BooleanBinding addListenerToTextField(final TextField field) {
    final BooleanBinding empty = field.textProperty().isEmpty();
    empty.addListener(listener);
    return empty;
  }

  @FXML
  private void handleButton() {
    if (emptyFields.get() > 0) {
      handleRemove();
    } else {
      handleAdd();
    }
    orig.requestFocus();
    orig.selectEnd();
  }

  private void handleAdd() {
    final TextField added = new TextField();
    added.setText(orig.getText());
    orig.clear();
    textList.addLast(new Pair<>(added, addListenerToTextField(added)));
    root.getChildren().add(root.getChildren().size() - 1, added);
    setMinus();
  }

  private void handleRemove() {
    cleanTextFields();
    setPlus();
    if (orig.getText().isEmpty()) {
      if (textList.size() == 1) {
        setEmpty();
      } else {
        final Pair<TextField, BooleanBinding> pair = textList.removeLast();
        orig.setText(pair.getKey().getText());
        final ObservableList<Node> ch = root.getChildren();
        ch.remove(ch.size() - 2); // don't remove orig
      }
    }

  }

  private void cleanTextFields() {
    int i = 0;
    final Iterator<Pair<TextField, BooleanBinding>> iter = textList.iterator();
    iter.next();// orig
    while (iter.hasNext()) {
      final Pair<TextField, BooleanBinding> pair = iter.next();
      if (pair.getKey().getText().isEmpty()) {
        iter.remove();
        root.getChildren().remove(i);
        emptyFields.decrementAndGet();
      } else {
        i++;
      }
    }
  }

  private void setEmpty() {
    button.setText(plus_Sign);
    button.setDisable(true);
  }

  private void setPlus() {
    button.setText(plus_Sign);
    button.setDisable(false);
  }

  private void setMinus() {
    button.setText(minus_Sign);
    button.setDisable(false);
  }

  public void setString(final String newS) {
    for (final Pair<TextField, BooleanBinding> pair : textList) {
      final TextField tf = pair.getKey();
      tf.clear();
    }
    orig.setText(newS);
    handleRemove();
  }

  public List<String> getAllStrings() {
    final List<String> out = new LinkedList<String>();
    for (final Pair<TextField, BooleanBinding> pair : textList) {
      final TextField tf = pair.getKey();
      final String text = tf.getText().trim();
      if (!text.isEmpty()) {
        out.add(text);
      }
    }
    // // ad orig is always add the end, this is done extra
    // final String text = orig.getText().trim();
    // if (!text.isEmpty()) {
    // out.add(text);
    // }
    return out;
  }
}
