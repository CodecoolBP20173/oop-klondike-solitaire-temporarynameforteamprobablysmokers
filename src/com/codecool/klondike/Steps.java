package com.codecool.klondike;

import java.util.*;

public interface Steps {

    ListIterator<Pile> pileStepIt = new LinkedList<Pile>().listIterator();
    ListIterator<Card> cardStepIt = new LinkedList<Card>().listIterator();
    ListIterator<Integer> numOfSteps = new LinkedList<Integer>().listIterator();

    public static ListIterator<Card> getCardStepIt() {
        return cardStepIt;
    }

    public static ListIterator<Pile> getPileStepIt() {
        return pileStepIt;
    }

    public static void undo () {

        if (cardStepIt.hasNext() && cardStepIt.next() != null) {
            if (pileStepIt.next().getTopCard()!=null) {
                pileStepIt.previous();
                if (pileStepIt.next().getPileType() != Pile.PileType.DISCARD) {
                    pileStepIt.next().getTopCard().flip();
                }
            }
            pileStepIt.previous();
            Integer next = numOfSteps.next();
            Pile sourceP = pileStepIt.next();
            for (Integer i = 0; i<next; i++)
                cardStepIt.next().moveToPile(sourceP);
        }
    }
}
