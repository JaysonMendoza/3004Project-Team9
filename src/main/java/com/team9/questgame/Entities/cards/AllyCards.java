package com.team9.questgame.Entities.cards;

import com.team9.questgame.Data.CardData;
import com.team9.questgame.Entities.Effects.EffectObserver;
import com.team9.questgame.Entities.Effects.Effects;
import com.team9.questgame.Entities.Players;

/**
 * Entity representing an Ally card within the game.
 *
 * Allies may have conditional effects based on a target card being present in a particular area.
 * An Allies boost effect can either be triggered by a particular Quest card being in play, or by a specific
 * ally being in the owing players play area.
 * @param <T> This is an enumeration type representing the card that triggers the boost.
 */
public class AllyCards <T extends Enum<T> & AllCardCodes> extends AdventureCards implements BoostableCard, BattlePointContributor,BidContributor,CardWithEffect<AllyCards> {
    private final int bonusBp; //Battle Points
    private final int bids;
    private final int boostBonusBp;
    private final int boostBids;
    boolean isBoosted;
    private final T boostConditionCardCode;
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
        this(assignedDeck,activeAbilityDescription, cardName, subType, fileName, cardCode,bonusBp,bids,0,0,null,null);
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
     * @param boostConditionCardCode The target card that will trigger this ally's boost effect.
     */
    public AllyCards(Decks assignedDeck,String activeAbilityDescription, String cardName, CardTypes subType, String fileName, AdventureDeckCards cardCode, int bonusBp, int bids, int boostBonusBp, int boostBids,T boostConditionCardCode, Effects effect) {
        super(assignedDeck,activeAbilityDescription, cardName, subType, fileName, cardCode);
        this.bonusBp = bonusBp;
        this.bids = bids;
        this.boostBonusBp = boostBonusBp;
        this.boostBids=boostBids;
        this.isBoosted=false;
        this.boostConditionCardCode = boostConditionCardCode;
        this.activeEffect=effect;
        if(effect!=null) {
            effect.setSource(this);
        }

    }

    @Override
    public String toString() {
        return super.toString() + ", AllyCards{" +
                "bonusBp=" + bonusBp +
                ", bids=" + bids +
                ", boostBonusBp=" + boostBonusBp +
                ", boostBids=" + boostBids +
                ", isBoosted=" + isBoosted +
                ", boostCardCode=" + boostConditionCardCode +
                '}';
    }

    @Override
    public void notifyBoostEnded(CardArea boostTriggerLocation) {
        if(boostTriggerLocation==location) {
            isBoosted=false;
        }
    }

    @Override
    public CardData generateCardData() {
        CardData data = new CardData(
                cardID,
                cardCode,
                cardName,
                subType,
                imgSrc,
                getBids(),
                getBattlePoints(),
                activeAbilityDescription,
                activeEffect!=null
        );
        return data;
    }

    public int getBids() {
        if(boostBids > bids && isBoosted) {
            return boostBids;
        }
        return bids;
    }

    public int getBattlePoints() {
        if(boostBonusBp > bonusBp && isBoosted) {
            return boostBonusBp;
        }
        return bonusBp;
    }

    public boolean isBoosted() {
        return isBoosted;
    }

    public void setBoosted(boolean boosted) {
        isBoosted = boosted;
    }

    public T getBoostConditionCardCode() {
        return boostConditionCardCode;
    }

    public Effects getActiveEffect() {
        return activeEffect;
    }

    @Override
    protected void registerWithNewPlayerPlayArea(PlayerPlayAreas playArea) {

        if(activeEffect!=null) {
            playArea.registerActiveEffect(this);
        }

        if(bids>0 || boostBids>0) {
            playArea.registerBidContributor(this);
        }

        if(bonusBp>0 || boostBonusBp>0) {
            playArea.registerBattlePointContributor(this);
        }

        if(boostConditionCardCode!=null) {
            playArea.registerCardBoostDependency(boostConditionCardCode,this);
        }
    }

    @Override
    protected void registerwithNewPlayArea(PlayAreas playArea) {
        playArea.registerBoostableCard(this);
    }

    @Override
    protected void registerWithHand(Hand hand) {
        if(activeEffect!=null) {
            hand.registerCardWithEffect(this);
        }
    }

    @Override
    public AllyCards getCard() {
        return this;
    }

    @Override
    public void discardCard() {
        isBoosted=false;
        super.discardCard();
    }


    @Override
    public void activate(EffectObserver observer, Players activatingPlayer) {
        if(activeEffect==null) {
            LOG.error("Player "+ activatingPlayer.getName()+ " activated "+this.cardCode+", but it has no Effect to activate!");
            return;
        }
        activeEffect.activate(observer,activatingPlayer);
    }

    @Override
    protected void onLocationChanged() {
        super.onLocationChanged();
        setBoosted(false);
    }
}
