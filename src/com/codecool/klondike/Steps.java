package com.codecool.klondike;

import java.util.*;

public interface Steps {

    //ListIterator<Card> cardStepIt = new LinkedList<Card>().listIterator();
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

        if (cardStepIt.hasNext()) {
            if (pileStepIt.next().getTopCard()!=null) {
                pileStepIt.previous();
                pileStepIt.next().getTopCard().flip();
            }
            pileStepIt.previous();
            //System.out.println("alma");
            //System.out.println(numOfSteps.next());
            Integer next = numOfSteps.next();
            for (Integer i = 0; i<next; i++)
                cardStepIt.next().moveToPile(pileStepIt.next());
        }

    }


}
