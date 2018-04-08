/*
 * Enjun
 *
 * @version     1.0 Beta 1
 * @author      Rocking Stars
 * @copyright   2018, Enjun
 *
 * Copyright 2018 RockingStars

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rockingstar.modules.Reversi.controllers;

import com.rockingstar.engine.ServerConnection;
import com.rockingstar.engine.command.client.CommandExecutor;
import com.rockingstar.engine.command.client.MoveCommand;
import com.rockingstar.engine.game.AbstractGame;
import com.rockingstar.engine.game.Player;
import com.rockingstar.engine.game.State;
import com.rockingstar.modules.Reversi.models.ReversiModel;
import com.rockingstar.modules.Reversi.views.ReversiView;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class ReversiController extends AbstractGame {

    private ReversiModel _model;
    private ReversiView _view;

    public ReversiController(Player player1, Player player2) {
        super(player1, player2);

        _view = new ReversiView(this);
        _model = new ReversiModel(_view);

        _model.addEventHandlers();
        _model.createCells();

        _view.setBoard(_model.getBoard());
        _view.generateBoardVisual();
    }

    @Override
    public Node getView() {
        return _view.getNode();
    }

    @Override
    public void doPlayerMove(int x, int y) {
        if (!gameFinished()) {
            if (_model.isValidMove(x, y, currentPlayer)) {
                if (yourTurn) {
                    CommandExecutor.execute(new MoveCommand(ServerConnection.getInstance(), y * 8 + x));

                    _model.setPlayerAtPosition(currentPlayer, x, y);
                    _view.setCellImage(x, y);

                    yourTurn = false;
                    setCurrentPlayer(1);
                }
                else
                    _view.setErrorStatus("It's not your turn");

            }
            else
                _view.setErrorStatus("Invalid move");
        }
        else
            gameEnded();
    }

    @Override
    public void doPlayerMove(int position) {
        if (!gameFinished()) {
            if (yourTurn)
                return;

            int x = position % 8;
            int y = position / 8;

            _model.setPlayerAtPosition(currentPlayer, x, y);
            _view.setCellImage(x, y);

            setCurrentPlayer(0);
        }
    }

    @Override
    public void setCurrentPlayer(int id) {
        if (currentState == State.GAME_FINISHED)
            return;

        currentPlayer = id == 0 ? player1 : player2;
        _view.setStatus(_model.getTurnMessage(currentPlayer));
    }

    private boolean gameFinished() {
        if (_model.hasWon(player1) || _model.hasWon(player2)) {
            _view.setStatus("Player " + (yourTurn ? player1 : player2).getUsername() + " has won! Congratulations.");

            setGameState(State.GAME_FINISHED);
            _view.setIsFinished(true);

            return true;
        }

        return _model.isFull();
    }

    public void gameEnded() {
        super.gameEnded();
        _view.setIsFinished(true);

        String currentPlayerName = (yourTurn ? player1 : player2).getUsername();
        _view.setStatus(_model.isFull() ? "It's a draw! N00bs!" : "Player " + currentPlayerName + " has won! Congratulations!");

        setGameState(State.GAME_FINISHED);

        Platform.runLater(() -> {
            Alert returnToLobby = new Alert(Alert.AlertType.CONFIRMATION);

            returnToLobby.setTitle("Game ended!");
            returnToLobby.setHeaderText(null);
            returnToLobby.setContentText("Do you want to return to the lobby?");
            returnToLobby.showAndWait();

            if (returnToLobby.getResult() == ButtonType.OK)
                toLobby();
        });
    }

    public void setStartingPlayer(Player player) {
        player1.setCharacter(player.getUsername().equals(player1.getUsername()) ? 'b' : 'w');
        player2.setCharacter(player1.getCharacter() == 'b' ? 'w' : 'b');

        _model.setStartingPositions(player1, player2);
    }
}