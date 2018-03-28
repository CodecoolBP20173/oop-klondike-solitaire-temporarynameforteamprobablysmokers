package com.codecool.klondike;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

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
        if ((!(card.isFaceDown()) && (card.getContainingPile().getTopCard() == card)) || card.getContainingPile().getPileType() == Pile.PileType.STOCK) {
            int clickCount = e.getClickCount();
            Pile sourcePile=((Card) e.getSource()).getContainingPile();
            if (clickCount == 2 && (sourcePile.getPileType() == Pile.PileType.TABLEAU) || sourcePile.getPileType() == Pile.PileType.DISCARD) {
                List<Pile> union = new ArrayList<>();
                union.addAll( tableauPiles );
                union.addAll( foundationPiles );
                Pile pile = getValidIntersectingPile(card, union, clickCount);
                if (pile != null) {
                    Steps.numOfSteps.add(1);
                    Steps.numOfSteps.previous();

                    Steps.getCardStepIt().add(card);
                    Steps.getCardStepIt().previous();
                    Steps.getPileStepIt().add(card.getContainingPile());
                    Steps.getPileStepIt().previous();
                    handleValidMove(card, pile);
                    if (!(sourcePile.getPileType() == Pile.PileType.DISCARD) && (!sourcePile.isEmpty()) && sourcePile.getTopCard().isFaceDown()) {
                        sourcePile.getTopCard().flip();
                    }
                } else {

                }
            }
            if (card.getContainingPile().getPileType() == Pile.PileType.STOCK) {
                card.moveToPile(discardPile);
                card.flip();
                card.setMouseTransparent(false);
                System.out.println("Placed " + card + " to the waste.");
            }
        }
    };

    private EventHandler<MouseEvent> stockReverseCardsHandler = e -> {
        refillStockFromDiscard();
    };

    private EventHandler<MouseEvent> onMousePressedHandler = e -> {
        Card card = (Card) e.getSource();
        if (!(card.isFaceDown())) {
            dragStartX = e.getSceneX();
            dragStartY = e.getSceneY();
        }
    };

    private EventHandler<MouseEvent> onMouseDraggedHandler = e -> {
        Card card = (Card) e.getSource();
      if (!(card.isFaceDown())) {
            source = card.getContainingPile();
            currentCard= card;

            Pile activePile = card.getContainingPile();
            if (activePile.getPileType() == Pile.PileType.STOCK)
                return;

            double offsetX = e.getSceneX() - dragStartX;
            System.out.println(offsetX);
            double offsetY = e.getSceneY() - dragStartY;
            draggedCards.clear();
            int offSetCounter = 1;
            for (Card pileCard: source.getCards()) {
                if (source.getCards().indexOf(pileCard) >= source.getCards().indexOf(currentCard)) {
                    draggedCards.add(pileCard);

                    pileCard.toFront();
                    pileCard.getDropShadow().setRadius(20);
                    pileCard.getDropShadow().setOffsetX(10);
                    pileCard.getDropShadow().setOffsetY(10);

                    pileCard.setTranslateX(offsetX);
                    pileCard.setTranslateY(offsetY);
                    offSetCounter++;
            }
          }
        }
    };

    private EventHandler<MouseEvent> onMouseReleasedHandler = e -> {
        if (draggedCards.isEmpty()) {
            return;
        }
        int clickCount = e.getClickCount();
        Card card = (Card) e.getSource();
        List<Pile> union = new ArrayList<>();
        union.addAll( tableauPiles );
        union.addAll( foundationPiles );
        Pile pile = getValidIntersectingPile(card, union, clickCount);
        Pile fromPile = card.getContainingPile();
        //TODO Complete
        if (pile != null) {

            if (pile.getPileType() == Pile.PileType.FOUNDATION && draggedCards.size() > 1 ) {
                draggedCards.forEach(MouseUtil::slideBack);
                draggedCards.clear();
            } else {
                Steps.numOfSteps.add(draggedCards.size());
                Steps.numOfSteps.previous();
                for (int i=0; i<draggedCards.size(); i++) {
                    Steps.getCardStepIt().add(draggedCards.get(i));
                    
                }

                for (int i=0; i<draggedCards.size(); i++) {

                    Steps.getCardStepIt().previous();
                }

                Steps.getPileStepIt().add(card.getContainingPile());
                Steps.getPileStepIt().previous();
                handleValidMove(card, pile);
              if (isGameWon()) {
                gameWon();
            }
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
        alert.setTitle("Game Complete");
        alert.setHeaderText("You Won!");
        alert.setContentText("Exit or Restart?");
        ButtonType exit = new ButtonType("Exit");
        ButtonType restart = new ButtonType("Restart");


        alert.getButtonTypes().setAll(exit, restart);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == exit){
            System.exit(0);
        }  else {
            reset();
            deck=Card.createNewDeck();
            initPiles();
            dealCards();
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
            return Card.isOppositeColor(card, destPile.getTopCard()) && card.getRank().ordinal() + 1 == destPile.getTopCard().getRank().ordinal();
        } else if (destPile.getPileType() == Pile.PileType.TABLEAU && destPile.isEmpty()) {
            return card.getRank().ordinal() + 1 == 13;
        } else if (destPile.getPileType() == Pile.PileType.FOUNDATION && destPile.isEmpty()) {
            return card.getRank().ordinal() + 1 == 1;
        } else if (destPile.getPileType() == Pile.PileType.FOUNDATION && (!destPile.isEmpty())) {
            return (card.getRank().ordinal() == destPile.getTopCard().getRank().ordinal() + 1) && (Card.isSameSuit(card, destPile.getTopCard()));
        } else {
            return false;
        }
    }
    private Pile getValidIntersectingPile(Card card, List<Pile> piles, int clickCount) {
        Pile result = null;
        if (clickCount == 2) {
            for (Pile pile : piles) {
                if (!pile.equals(card.getContainingPile()) &&
                        isMoveValid(card, pile))
                    result = pile;
            }
        } else {
            for (Pile pile : piles) {
                if (!pile.equals(card.getContainingPile()) &&
                        isOverPile(card, pile) &&
                        isMoveValid(card, pile))
                    result = pile;
            }
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
        Font myFont = new Font("Serif",  36);
        Button undo = new Button("\u21ba");
        undo.setFont(myFont);
        Button restart = new Button("Restart");
        undo.setLayoutY(130);
        undo.setLayoutX(475);
        undo.setMaxSize(100,80);
        undo.setMinSize(100,80);
        undo.setOnAction(e -> {
            Steps.undo();
        });
        getChildren().add(undo);

        restart.setLayoutY(40);
        restart.setLayoutX(475);
        restart.setMaxSize(100,80);
        restart.setMinSize(100,80);
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
