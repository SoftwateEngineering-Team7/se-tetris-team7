// package org.tetris.menu.controller;

// import javafx.fxml.FXML;
// import javafx.scene.control.Label;
// import javafx.scene.input.KeyEvent;

// abstract class MenuController
// {
//     MenuController()
//     {
//     }
//     @FXML
//     public void handleKey(KeyEvent event)
//     {
//         switch(event.getCode())
//         {
//             case LEFT -> this.executeMenu(Command.LEFT);
//             case RIGHT -> this.executeMenu(Command.RIGHT);
//             case DOWN -> this.executeMenu(Command.DOWN);
//             case UP -> this.executeMenu(Command.UP);
//             case ENTER -> this.executeMenu(Command.SELECT);
//         }
//     }
//     abstract void executeMenu(Command command) {
//         // Todo: 실제 동작 (게임 시작, 옵션창 열기, 종료 등)
//     }
// }
