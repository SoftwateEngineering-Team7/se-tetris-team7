package org.tetris.shared;

import javafx.scene.Parent;
import javafx.scene.Scene;


public class ViewWrap {
    private final Parent root;
    private final Scene scene;
    public ViewWrap(Parent root) { 
        this.root = root; 
        this.scene = new Scene(root); 
    }
    public Scene getScene() { return scene; }
    public Parent getRoot() { return root; }
}
