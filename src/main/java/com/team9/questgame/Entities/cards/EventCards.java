package com.team9.questgame.Entities.cards;

import com.team9.questgame.Entities.Effects.Effects;
import com.team9.questgame.Entities.Players;
import com.team9.questgame.game_phases.GamePhases;
import com.team9.questgame.game_phases.GeneralGameController;

import java.util.ArrayList;

/**
 * Entity representing Event Cards
 *
 * Each event card has an effect that is activated when the card is drawn.
 * Some effects are delayed and this is handled by the Effects class and it's
 * subclasses.
 */
public class EventCards extends StoryCards {
    final Effects activeEffect;

    @Override
    public String toString() {
        return super.toString()+", EventCards{" +
                "activeEffect=" + activeEffect +
                '}';
    }

    @Override
    public GamePhases generateGamePhase(ArrayList<Players> players, GeneralGameController gameInstance) {
        return null;
    }

    /**
     *
     * @param activeAbilityDescription A description text to appear at the bottom of a card with an active or conditional effect.
     * @param cardName The title of the card, appearing at the top.
     * @param subType The subtype of card within it's deck type. This is generally Foe,Ally,etc..
     * @param fileName The uri path where the image representing the card can be found from the client.
     * @param cardCode An enumeration cardCode that helps identify which unique card this instance represents. Many cards will have multiple copies in a deck.
     * @param activeEffect An effect object that represents the algorithm required to resolve this card's active ability
     */
    public EventCards(Decks assignedDeck,String activeAbilityDescription, String cardName, CardTypes subType, String fileName, StoryDeckCards cardCode, Effects activeEffect) {
        super(assignedDeck,activeAbilityDescription, cardName, subType, fileName, cardCode);
        this.activeEffect = activeEffect;
    }
}
