package com.codecool.klondike;

import org.omg.CORBA.Object;

import java.util.*;

public interface Steps {

    ListIterator<Card> cardStepIt = new LinkedList<Card>().listIterator();
    ListIterator<Pile> pileStepIt = new LinkedList<Pile>().listIterator();

    
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
            cardStepIt.next().moveToPile(pileStepIt.next());


            //pileStepIt.previous();
        }

    }


}
