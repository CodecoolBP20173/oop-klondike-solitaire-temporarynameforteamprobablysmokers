package com.codecool.klondike;

import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

import java.util.*;

public class Card extends ImageView {

    private cardSuits suit;
    private cardRanks rank;
    private cardColor color;
    private boolean faceDown;

    private Image backFace;
    private Image frontFace;
    private Pile containingPile;
    private DropShadow dropShadow;

    static Image cardBackImage;
    private static final Map<String, Image> cardFaceImages = new HashMap<>();
    public static final int WIDTH = 150;
    public static final int HEIGHT = 215;

    public Card(cardSuits suit, cardRanks rank, boolean faceDown) {
        this.suit = suit;
        this.rank = rank;
        if (suit == cardSuits.HEARTS || suit == cardSuits.DIAMONDS) {
            this.color = cardColor.RED;
        } else {
            this.color = cardColor.BLACK;
        }
        this.faceDown = faceDown;
        this.dropShadow = new DropShadow(2, Color.gray(0, 0.75));
        backFace = cardBackImage;
        frontFace = cardFaceImages.get(getShortName());
        setImage(faceDown ? backFace : frontFace);
        setEffect(dropShadow);
    }

    public cardSuits getSuit() {
        return suit;
    }

    public cardRanks getRank() {
        return rank;
    }

    public cardColor getColor() {
        return color;
    }

    public boolean isFaceDown() {
        return faceDown;
    }

    public String getShortName() {
        return "S" + Integer.toString(suit.ordinal()+1) + "R" + Integer.toString(rank.ordinal() + 1);
    }

    public DropShadow getDropShadow() {
        return dropShadow;
    }

    public Pile getContainingPile() {
        return containingPile;
    }

    public void setContainingPile(Pile containingPile) {
        this.containingPile = containingPile;
    }

    public void moveToPile(Pile destPile) {
        this.getContainingPile().getCards().remove(this);
        destPile.addCard(this);
    }

    public void flip() {
        faceDown = !faceDown;
        setImage(faceDown ? backFace : frontFace);
    }

    @Override
    public String toString() {
        return "The " + "Rank" + rank + 1 + " of " + "Suit" + suit.ordinal();
    }

    public static boolean isOppositeColor(Card card1, Card card2) {
        return card1.color != card2.color;
    }

    public static boolean isSameSuit(Card card1, Card card2) {
        return card1.getSuit() == card2.getSuit();
    }

    public static List<Card> createNewDeck() {
        List<Card> result = new ArrayList<>();
        for (cardSuits suit: cardSuits.values()) {
            for (cardRanks rank: cardRanks.values()) {
                result.add(new Card(suit, rank, true));
            }
        }
        System.out.println(result.size());
        Collections.shuffle(result);
        return result;
    }

    public static void loadCardImages() {
        cardBackImage = new Image("card_images/card_back.png");
        for (cardSuits suit: cardSuits.values()) {
            for (cardRanks rank: cardRanks.values()) {
                String cardName = suit.toString() + Integer.toString(rank.ordinal()+1);
                System.out.println(cardName.toLowerCase());
                String cardId = "S" + Integer.toString(suit.ordinal()+1) + "R" + Integer.toString(rank.ordinal()+1);
                System.out.println(cardId);
                String imageFileName = "card_images/" + cardName.toLowerCase() + ".png";
                cardFaceImages.put(cardId, new Image(imageFileName));

            }
        }
    }

    public enum cardSuits{
        HEARTS,
        DIAMONDS,
        SPADES,
        CLUBS
    }

    public enum cardRanks{
        ACE,
        TWO,
        THREE,
        FOUR,
        FIVE,
        SIX,
        SEVEN,
        EIGHT,
        NINE,
        TEN,
        JOHNNY,
        QUEEN,
        KING
    }

    public enum cardColor{
        BLACK,
        RED
    }

}
