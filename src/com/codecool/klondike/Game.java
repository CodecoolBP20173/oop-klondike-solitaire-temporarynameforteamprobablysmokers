package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

import java.util.*;

public class Game extends Pane {

    private List<Card> deck = new ArrayList<>();

    private Pile stockPile;
    private Pile discardPile;
    private List<Pile> foundationPiles = FXCollections.observableArrayList();
    private List<Pile> tableauPiles = FXCollections.observableArrayList();

    private double dragStartX, dragStartY;
    private List<Card> draggedCards = FXCollections.observableArrayList();

    private static double STOCK_GAP = 1;
    private static double FOUNDATION_GAP = 0;
    private static double TABLEAU_GAP = 30;

    private Pile source;
    private Card currentCard;

    private EventHandler<MouseEvent> onMouseClickedHandler = e -> {
        Card card = (Card) e.getSource();
        source=((Card) e.getSource()).getContainingPile();
        if (card.getContainingPile().getPileType() == Pile.PileType.STOCK) {
            card.moveToPile(discardPile);
            card.flip();
            card.setMouseTransparent(false);
            System.out.println("Placed " + card + " to the waste.");
        }
    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        dragStartX = e.getSceneX();
        dragStartY = e.getSceneY();
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        Card card = (Card) e.getSource();
        source = card.getContainingPile();
        currentCard= card;

        Pile activePile = card.getContainingPile();
        if (activePile.getPileType() == Pile.PileType.STOCK)
            return;
        double offsetX = e.getSceneX() - dragStartX;
        double offsetY = e.getSceneY() - dragStartY;

        draggedCards.clear();
        draggedCards.add(card);

        card.getDropShadow().setRadius(20);
        card.getDropShadow().setOffsetX(10);
        card.getDropShadow().setOffsetY(10);

        card.toFront();
        card.setTranslateX(offsetX);
        card.setTranslateY(offsetY);
    };

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (draggedCards.isEmpty())
            return;

        Card card = (Card) e.getSource();
        List<Pile> union = new ArrayList<>();
        union.addAll( tableauPiles );
        union.addAll( foundationPiles );
        Pile pile = getValidIntersectingPile(card, union);
        Pile fromPile = card.getContainingPile();
        //TODO Complete
        if (pile != null) {
            card.moveToPile(pile);
            //handles undo
            Steps.getCardStepIt().add(currentCard);
            Steps.getCardStepIt().previous();
            Steps.getPileStepIt().add(source);
            Steps.getPileStepIt().previous();

            handleValidMove(card, pile);
            if (isGameWon()) {
                gameWon();
            }
            if (!(fromPile.getPileType() == Pile.PileType.DISCARD) && (!fromPile.isEmpty()) && fromPile.getTopCard().isFaceDown()) {
                fromPile.getTopCard().flip();
            }
        } else {
            draggedCards.forEach(MouseUtil::slideBack);
            draggedCards.clear();
        }
    };

    public boolean isGameWon() {
        //TODO
        boolean answer = true;
        for (Pile pile: foundationPiles) {
            if (pile.getCards().size()!=13) {
                answer=false;
                break;
            }
        }
        return answer;
    }

    public Game() {

        deck = Card.createNewDeck();
        initPiles();
        dealCards();
        initButtons();
    }

    public void gameWon() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("You Won!");
        alert.setContentText("Exit or Restart?");
        ButtonType exit = new ButtonType("Exit");
        ButtonType restart = new ButtonType("Restart");


        alert.getButtonTypes().setAll(exit, restart);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == exit){
            System.exit(0);
        }  else {
            reset();
        }


    }


    public void addMouseEventHandlers(Card card) {
        card.setOnMousePressed(onMousePressedHandler);
        card.setOnMouseDragged(onMouseDraggedHandler);
        card.setOnMouseReleased(onMouseReleasedHandler);
        card.setOnMouseClicked(onMouseClickedHandler);
    }

    public void refillStockFromDiscard() {
        //TODO
        int discardPileLength = discardPile.numOfCards();
        if (stockPile.isEmpty()) {
            for (int i = 0; i < discardPileLength; i++) {
                discardPile.getTopCard().flip();
                discardPile.getTopCard().moveToPile(stockPile);
            }
        }
        System.out.println("Stock refilled from discard pile.");
    }

    public boolean isMoveValid(Card card, Pile destPile) {
        //TODO Complete
        if (destPile.getPileType() == Pile.PileType.TABLEAU && !(destPile.isEmpty())) {
            return Card.isOppositeColor(card, destPile.getTopCard()) && card.getRank() == destPile.getTopCard().getRank() - 1;
        } else if (destPile.getPileType() == Pile.PileType.TABLEAU && destPile.isEmpty()) {
            return card.getRank() == 13;
        } else if (destPile.getPileType() == Pile.PileType.FOUNDATION && destPile.isEmpty()) {
            return card.getRank() == 1;
        } else if (destPile.getPileType() == Pile.PileType.FOUNDATION && (!destPile.isEmpty())) {
            return (card.getRank() == destPile.getTopCard().getRank() + 1) && (card.getSuit() == destPile.getTopCard().getSuit());
        } else {
            return false;
        }
    }
    private Pile getValidIntersectingPile(Card card, List<Pile> piles) {
        Pile result = null;
        for (Pile pile : piles) {
            if (!pile.equals(card.getContainingPile()) &&
                    isOverPile(card, pile) &&
                    isMoveValid(card, pile))
                result = pile;
        }
        return result;
    }

    private boolean isOverPile(Card card, Pile pile) {
        if (pile.isEmpty())
            return card.getBoundsInParent().intersects(pile.getBoundsInParent());
        else
            return card.getBoundsInParent().intersects(pile.getTopCard().getBoundsInParent());
    }

    private void handleValidMove(Card card, Pile destPile) {
        String msg = null;
        if (destPile.isEmpty()) {
            if (destPile.getPileType().equals(Pile.PileType.FOUNDATION))
                msg = String.format("Placed %s to the foundation.", card);
            if (destPile.getPileType().equals(Pile.PileType.TABLEAU))
                msg = String.format("Placed %s to %s.", card, destPile);
        } else {
            msg = String.format("Placed %s to %s from %s", card, destPile.getTopCard(), source);
        }
        System.out.println(msg);
        MouseUtil.slideToDest(draggedCards, destPile);
        draggedCards.clear();
    }




    private void initButtons() {
        Button undo = new Button("Undo Move");
        Button restart = new Button("Restart");
        undo.setLayoutY(600);
        undo.setLayoutX(600);
        undo.setOnAction(e -> {
            Steps.undo();
        });

        getChildren().add(undo);
        restart.setLayoutY(700);
        restart.setLayoutX(600);
        restart.setOnAction(e -> {

            reset();

            deck=Card.createNewDeck();
            initPiles();
            dealCards();

        });

        getChildren().add(restart);

    }

    public void reset() {
        for (Pile pile: tableauPiles) {

            getChildren().removeAll(pile.getCards());
            pile.clear();
            getChildren().remove(pile);

        }
        tableauPiles.clear();
        for (Pile pile: foundationPiles) {
            getChildren().removeAll(pile.getCards());
            pile.clear();
            getChildren().remove(pile);

        }
        foundationPiles.clear();
        getChildren().removeAll(stockPile.getCards());
        getChildren().remove(stockPile);
        stockPile.clear();
        getChildren().removeAll(discardPile.getCards());
        getChildren().remove(discardPile);
        discardPile.clear();
        deck.clear();

    }

    private void initPiles() {
        stockPile = new Pile(Pile.PileType.STOCK, "Stock", STOCK_GAP);
        stockPile.setBlurredBackground();
        stockPile.setLayoutX(95);
        stockPile.setLayoutY(20);
        stockPile.setOnMouseClicked(stockReverseCardsHandler);
        getChildren().add(stockPile);

        discardPile = new Pile(Pile.PileType.DISCARD, "Discard", STOCK_GAP);
        discardPile.setBlurredBackground();
        discardPile.setLayoutX(285);
        discardPile.setLayoutY(20);
        getChildren().add(discardPile);

        for (int i = 0; i < 4; i++) {
            Pile foundationPile = new Pile(Pile.PileType.FOUNDATION, "Foundation " + i, FOUNDATION_GAP);
            foundationPile.setBlurredBackground();
            foundationPile.setLayoutX(610 + i * 180);
            foundationPile.setLayoutY(20);
            foundationPiles.add(foundationPile);
            getChildren().add(foundationPile);
        }
        for (int i = 0; i < 7; i++) {
            Pile tableauPile = new Pile(Pile.PileType.TABLEAU, "Tableau " + i, TABLEAU_GAP);
            tableauPile.setBlurredBackground();
            tableauPile.setLayoutX(95 + i * 180);
            tableauPile.setLayoutY(275);
            tableauPiles.add(tableauPile);
            getChildren().add(tableauPile);
        }
    }

    public void dealCards() {
        Iterator<Card> deckIterator = deck.iterator();
        //TODO Complete
        int i = 0;
        while (i < 7) {
            for (int j=-1; j < i; j++) {
                Card card = deckIterator.next();
                tableauPiles.get(i).addCard(card);
                addMouseEventHandlers(card);
                getChildren().add(card);
                if (j == i - 1) {
                    card.flip();
                }
            }
            i++;
        }
        deckIterator.forEachRemaining(card -> {
            stockPile.addCard(card);
            addMouseEventHandlers(card);
            getChildren().add(card);
        });

    }

    public void setTableBackground(Image tableBackground) {
        setBackground(new Background(new BackgroundImage(tableBackground,
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT,
                BackgroundPosition.CENTER, BackgroundSize.DEFAULT)));
    }

}
