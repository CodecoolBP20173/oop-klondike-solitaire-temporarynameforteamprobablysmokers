package com.codecool.klondike;

import org.omg.CORBA.Object;

import java.util.*;

public class Steps {

    private static ListIterator<Card> cardStepIt = new LinkedList<Card>().listIterator();
    private static ListIterator<Pile> pileStepIt = new LinkedList<Pile>().listIterator();


    public static ListIterator<Card> getCardStepIt() {
        return cardStepIt;
    }

    public static ListIterator<Pile> getPileStepIt() {
        return pileStepIt;
    }

    public static void undo () {

        if (cardStepIt.hasNext()) {
            cardStepIt.next().moveToPile(pileStepIt.next());
        }

    }


}
