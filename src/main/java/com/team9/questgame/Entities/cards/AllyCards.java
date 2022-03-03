package com.team9.questgame.Entities.cards;

import com.team9.questgame.Data.CardData;
import com.team9.questgame.Entities.Effects.Effects;

/**
 * Entity representing an Ally card within the game.
 *
 * Allies may have conditional effects based on a target card being present in a particular area.
 * An Allies boost effect can either be triggered by a particular Quest card being in play, or by a specific
 * ally being in the owing players play area.
 * @param <T> This is an enumeration type representing the card that triggers the boost.
 */
public class AllyCards <T extends Enum<T> & AllCardCodes> extends AdventureCards {
    private final int bonusBp; //Battle Points
    private final int bids;
    private final int boostBonusBp;
    private final int boostBids;
    boolean isBoosted;
    private final T boostCardCode;
    private Effects activeEffect; //TODO: Modify for Effect implementation

    /**
     *
     * @param activeAbilityDescription A description text to appear at the bottom of a card with an active or conditional effect.
     * @param cardName The title of the card, appearing at the top.
     * @param subType The subtype of card within it's deck type. This is generally Foe,Ally,etc..
     * @param fileName The uri path where the image representing the card can be found from the client.
     * @param cardCode An enumeration cardCode that helps identify which unique card this instance represents. Many cards will have multiple copies in a deck.
     * @param bonusBp The battlepoints this ally contributes
     * @param bids The bids this ally contributes
     */
    public AllyCards(Decks assignedDeck,String activeAbilityDescription, String cardName, CardTypes subType, String fileName, AdventureDeckCards cardCode, int bonusBp, int bids) {
        this(assignedDeck,activeAbilityDescription, cardName, subType, fileName, cardCode,bonusBp,bids,0,0,null);
    }

    @Override
    public String toString() {
        return super.toString() + ", AllyCards{" +
                "bonusBp=" + bonusBp +
                ", bids=" + bids +
                ", boostBonusBp=" + boostBonusBp +
                ", boostBids=" + boostBids +
                ", isBoosted=" + isBoosted +
                ", boostCardCode=" + boostCardCode +
                '}';
    }

    /**
     *
     * @param activeAbilityDescription A description text to appear at the bottom of a card with an active or conditional effect.
     * @param cardName The title of the card, appearing at the top.
     * @param subType The subtype of card within it's deck type. This is generally Foe,Ally,etc..
     * @param fileName The uri path where the image representing the card can be found from the client.
     * @param cardCode An enumeration cardCode that helps identify which unique card this instance represents. Many cards will have multiple copies in a deck.
     * @param bonusBp The battlepoints this ally contributes
     * @param bids The bids this ally contributes
     * @param boostBonusBp The total number of battlepoints this ally contributes if the boost condition was met.
     * @param boostBids The total number of bids this ally contributes if the boost condition was met.
     * @param boostCardCode The target card that will trigger this ally's boost effect.
     */
    public AllyCards(Decks assignedDeck,String activeAbilityDescription, String cardName, CardTypes subType, String fileName, AdventureDeckCards cardCode, int bonusBp, int bids, int boostBonusBp, int boostBids,T boostCardCode) {
        super(assignedDeck,activeAbilityDescription, cardName, subType, fileName, cardCode);
        this.bonusBp = bonusBp;
        this.bids = bids;
        this.boostBonusBp = boostBonusBp;
        this.boostBids=boostBids;
        this.isBoosted=false;
        this.boostCardCode = boostCardCode;
        this.activeEffect=null; //TODO: Change when Effects implemented

    }

    @Override
    public void playCard() {

    }

    @Override
    public CardData generateCardData() {
        CardData data = new CardData(
                cardID,
                cardCode,
                cardName,
                subType,
                imgSrc,
                isBoosted ? bids : boostBids,
                isBoosted ? bonusBp : boostBonusBp,
                activeAbilityDescription,
                activeEffect!=null
        );
        return data;
    }
}
