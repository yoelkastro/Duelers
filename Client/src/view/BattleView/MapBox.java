package view.BattleView;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import models.card.CardType;
import models.comperessedData.CompressedCard;
import models.comperessedData.CompressedGameMap;
import models.comperessedData.CompressedTroop;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;

public class MapBox implements PropertyChangeListener {
    private final BattleScene battleScene;
    private final CompressedGameMap gameMap;
    private final Group mapGroup;
    private final Polygon[][] cells = new Polygon[5][9];
    private final double[][] cellsX = new double[5][9];
    private final double[][] cellsY = new double[5][9];

    private final HashMap<CompressedTroop, TroopAnimation> troopAnimationHashMap = new HashMap<>();
    private CompressedTroop selectedTroop = null;
    private ArrayList<CompressedTroop> comboTroops = new ArrayList<>();
    private boolean spellSelected = false;
    private boolean comboSelected = false;

    public MapBox(BattleScene battleScene, CompressedGameMap gameMap, double x, double y) throws Exception {
        this.battleScene = battleScene;
        this.gameMap = gameMap;
        mapGroup = new Group();
        mapGroup.setLayoutY(y);
        mapGroup.setLayoutX(x);
        makePolygons();
        resetSelection();
        //addCircles();
        for (CompressedTroop troop : gameMap.getTroops()) {
            updateTroop(null, troop);
        }
        gameMap.addPropertyChangeListener(this);
    }

    private void makePolygons() {
        for (int j = 0; j < 5; j++) {
            double upperWidth = (j * Constants.MAP_DOWNER_WIDTH + (6 - j) * Constants.MAP_UPPER_WIDTH) / 6;
            double downerWidth = ((j + 1) * Constants.MAP_DOWNER_WIDTH + (6 - (j + 1)) * Constants.MAP_UPPER_WIDTH) / 6;
            double upperY = Constants.MAP_HEIGHT * (upperWidth - Constants.MAP_UPPER_WIDTH) /
                    (Constants.MAP_DOWNER_WIDTH - Constants.MAP_UPPER_WIDTH);
            double downerY = Constants.MAP_HEIGHT * (downerWidth - Constants.MAP_UPPER_WIDTH) /
                    (Constants.MAP_DOWNER_WIDTH - Constants.MAP_UPPER_WIDTH);
            for (int i = 0; i < 9; i++) {
                double x1 = (Constants.MAP_DOWNER_WIDTH - upperWidth) / 2 + i * upperWidth / 9;
                double x2 = (Constants.MAP_DOWNER_WIDTH - upperWidth) / 2 + (i + 1) * upperWidth / 9;
                double x3 = (Constants.MAP_DOWNER_WIDTH - downerWidth) / 2 + (i + 1) * downerWidth / 9;
                double x4 = (Constants.MAP_DOWNER_WIDTH - downerWidth) / 2 + i * downerWidth / 9;
                cells[j][i] = new Polygon(x1 + Constants.SPACE_BETWEEN_CELLS / 2,
                        upperY + Constants.SPACE_BETWEEN_CELLS / 2, x2 - Constants.SPACE_BETWEEN_CELLS / 2,
                        upperY + Constants.SPACE_BETWEEN_CELLS / 2, x3 - Constants.SPACE_BETWEEN_CELLS / 2,
                        downerY - Constants.SPACE_BETWEEN_CELLS / 2, x4 + Constants.SPACE_BETWEEN_CELLS / 2,
                        downerY - Constants.SPACE_BETWEEN_CELLS / 2);
                cells[j][i].setFill(Color.DARKBLUE);
                cells[j][i].setOpacity(Constants.CELLS_OPACITY);
                final int I = i, J = j;
                cells[j][i].setOnMouseEntered(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        hoverCell(J, I);
                    }
                });
                cells[j][i].setOnMouseExited(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        exitCell(J, I);
                    }
                });
                cells[j][i].setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        clickCell(J, I);
                    }
                });
                mapGroup.getChildren().add(cells[j][i]);
                cellsX[j][i] = (x1 + x2 + x3 + x4) / 4;
                cellsY[j][i] = (upperY + downerY) / 2;
            }
        }
    }

    private void addCircles() {
        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 9; i++) {
                mapGroup.getChildren().add(new Circle(cellsX[j][i], cellsY[j][i], 2));
            }
        }
    }

    private void updateTroop(CompressedTroop oldTroop, CompressedTroop newTroop) {
        final TroopAnimation animation;
        if (newTroop == null) {
            animation = troopAnimationHashMap.get(oldTroop);
            if (animation != null) {
                animation.kill();
                troopAnimationHashMap.remove(oldTroop);
            }
        } else if (oldTroop != null && troopAnimationHashMap.containsKey(oldTroop)) {
            animation = troopAnimationHashMap.get(oldTroop);
            troopAnimationHashMap.remove(oldTroop);
            troopAnimationHashMap.put(newTroop, animation);
            animation.moveTo(newTroop.getPosition().getRow(), newTroop.getPosition().getColumn());
        } else {
            try {
                animation = new TroopAnimation(mapGroup, cellsX, cellsY, newTroop.getCard().getSpriteName(),
                        newTroop.getPosition().getRow(), newTroop.getPosition().getColumn(), newTroop.getPlayerNumber() == 1);
                animation.getImageView().setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        clickCell(animation.getRow(), animation.getColumn());
                    }
                });
                animation.getImageView().setOnMouseEntered(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        hoverCell(animation.getRow(), animation.getColumn());
                    }
                });
                animation.getImageView().setOnMouseExited(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        exitCell(animation.getRow(), animation.getColumn());
                    }
                });
                troopAnimationHashMap.put(newTroop, animation);
            } catch (Exception e) {
                System.out.println("Error making animation " + newTroop.getCard().getCardId());
            }
        }
        resetSelection();
    }

    public Group getMapGroup() {
        return mapGroup;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("troop")) {
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    updateTroop((CompressedTroop) evt.getOldValue(), (CompressedTroop) evt.getNewValue());
                }
            });
        }
    }

    public void resetSelection() {
        selectedTroop = null;
        comboTroops.clear();
        spellSelected = false;
        comboSelected = false;
        for (int j = 0; j < 5; j++) {
            for (int i = 0; i < 9; i++) {
                cells[j][i].setFill(Color.DARKBLUE);
            }
        }
        battleScene.getPlayerBox().refreshComboAndSpell();
    }

    private void exitCell(int j, int i) {
        CompressedTroop troop = getTroop(j, i);
        if (troop == null) {
            cells[j][i].setFill(Color.DARKBLUE);
            return;
        }
        if (selectedTroop == troop || comboTroops.contains(troop)) {
            cells[j][i].setFill(Color.DARKGREEN);
            return;
        }
        cells[j][i].setFill(Color.DARKBLUE);
    }

    private void hoverCell(int j, int i) {
        CompressedTroop troop = getTroop(j, i);
        CompressedCard card = battleScene.getHandBox().getSelectedCard();
        if (card == null) {
            if (troop == null) {
                if (selectedTroop != null)
                    cells[j][i].setFill(Color.DARKGREEN);
            } else {
                if (selectedTroop == null) {
                    if (troop.getPlayerNumber() == battleScene.getMyPlayerNumber())
                        cells[j][i].setFill(Color.DARKGREEN);
                } else {
                    if (troop == selectedTroop) {
                        selectedTroop = null;
                        System.out.println("diSelect Troop");
                    } else {
                        if (spellSelected) {
                            if (troop.getPlayerNumber() != battleScene.getMyPlayerNumber())
                                cells[j][i].setFill(Color.DARKRED);
                        } else {
                            if (comboSelected) {
                                if (troop.getPlayerNumber() == battleScene.getMyPlayerNumber() && troop.getCard().isHasCombo())
                                    cells[j][i].setFill(Color.DARKGREEN);
                                else if (troop.getPlayerNumber() != battleScene.getMyPlayerNumber()) {
                                    cells[j][i].setFill(Color.DARKRED);
                                } else {
                                    cells[j][i].setFill(Color.DARKBLUE);
                                }
                            } else {
                                if (troop.getPlayerNumber() != battleScene.getMyPlayerNumber()) {
                                    cells[j][i].setFill(Color.DARKRED);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (card.getType() == CardType.SPELL || card.getType() == CardType.USABLE_ITEM) {
                cells[j][i].setFill(Color.HOTPINK);
            } else {
                if (troop == null)
                    cells[j][i].setFill(Color.DARKGREEN);
            }
        }
    }

    private void clickCell(int j, int i) {
        CompressedTroop troop = getTroop(j, i);
        CompressedCard card = battleScene.getHandBox().getSelectedCard();
        if (card == null) {
            if (troop == null) {
                if (selectedTroop != null) {
                    System.out.println("Move");
                    gameMap.updateTroop(new CompressedTroop(selectedTroop, j, i));
                    resetSelection();
                }
            } else {
                if (selectedTroop == null) {
                    if (troop.getPlayerNumber() == battleScene.getMyPlayerNumber()) {
                        selectedTroop = troop;
                        System.out.println("Select Troop");
                    }
                } else {
                    if (troop == selectedTroop) {
                        selectedTroop = null;
                        System.out.println("diSelect Troop");
                    } else {
                        if (spellSelected) {
                            if (troop.getPlayerNumber() != battleScene.getMyPlayerNumber()) {
                                System.out.println("Spell");
                                resetSelection();
                            }
                        } else {
                            if (comboSelected) {
                                if (troop.getPlayerNumber() == battleScene.getMyPlayerNumber() && troop.getCard().isHasCombo()) {
                                    comboTroops.add(troop);
                                    System.out.println("Add Combo");
                                } else if (troop.getPlayerNumber() != battleScene.getMyPlayerNumber()) {
                                    troopAnimationHashMap.get(selectedTroop).attack(troop.getPosition().getColumn());
                                    for (CompressedTroop comboAttacker : comboTroops)
                                        troopAnimationHashMap.get(comboAttacker).attack(troop.getPosition().getColumn());
                                    System.out.println("Attack Combo");
                                    resetSelection();
                                }
                            } else {
                                if (troop.getPlayerNumber() != battleScene.getMyPlayerNumber()) {
                                    System.out.println("Attack");
                                    troopAnimationHashMap.get(selectedTroop).attack(troop.getPosition().getColumn());
                                    resetSelection();
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (card.getType() == CardType.SPELL || card.getType() == CardType.USABLE_ITEM) {
                System.out.println("Insert Spell");
                resetSelection();
            } else {
                if (troop == null) {
                    System.out.println("Insert Troop");
                    resetSelection();
                }
            }
            battleScene.getHandBox().resetSelection();
        }
    }

    private CompressedTroop getTroop(int j, int i) {
        for (CompressedTroop troop : troopAnimationHashMap.keySet()) {
            if (troop.getPosition().getRow() == j && troop.getPosition().getColumn() == i)
                return troop;
        }
        return null;
    }

    public CompressedTroop getSelectedTroop() {
        return selectedTroop;
    }

    public void setSpellSelected(boolean spellSelected) {
        this.spellSelected = spellSelected;
    }

    public void setComboSelected(boolean comboSelected) {
        this.comboSelected = comboSelected;
    }

    public boolean isSpellSelected() {
        return spellSelected;
    }

    public boolean isComboSelected() {
        return comboSelected;
    }
}
