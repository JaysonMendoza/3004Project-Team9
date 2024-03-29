package com.team9.questgame.Entities.cards;

import com.team9.questgame.Data.CardData;
import com.team9.questgame.Entities.Effects.EffectObserver;
import com.team9.questgame.Entities.Effects.Effects;
import com.team9.questgame.Entities.Players;

public class FoeCards extends AdventureCards implements BoostableCard, BattlePointContributor, CardWithEffect<FoeCards> {
    private final int bpValue;
    private final int boostedBpValue;
    private boolean isBoosted;
    private Effects activeEffect;

    public FoeCards(Decks assignedDeck, String activeAbilityDescription, String cardName, CardTypes subType, String fileName, AdventureDeckCards cardCode, int bpValue, int boostedBpValue, Effects activeEffect) {
        super(assignedDeck, activeAbilityDescription, cardName, subType, fileName, cardCode);
        this.bpValue = bpValue;
        this.boostedBpValue = boostedBpValue;
        this.activeEffect = activeEffect;
    }

    @Override
    public String toString() {
        return super.toString()+", FoeCards{" +
                "bpValue=" + bpValue +
                ", boostedBpValue=" + boostedBpValue +
                ", isBoosted=" + isBoosted +
                '}';
    }

    @Override
    public void notifyBoostEnded(CardArea boostTriggerLocation) {
        if(boostTriggerLocation==location) {
            isBoosted=false;
        }
    }

    public void setBoosted(boolean boosted) {
        isBoosted = boosted;
    }

    public int getBattlePoints() {
        if(boostedBpValue > bpValue & isBoosted) {
            return boostedBpValue;
        }
        return bpValue;
    }

    public boolean isBoosted() {
        return isBoosted;
    }

    public Effects getActiveEffect() {
        return activeEffect;
    }

    public FoeCards(Decks assignedDeck, String activeAbilityDescription, String cardName, CardTypes subType, String imageFileName, AdventureDeckCards cardCode, int battlePointValue) {
        this(assignedDeck,activeAbilityDescription, cardName, subType, imageFileName, cardCode,battlePointValue,0);
    }
    public FoeCards(Decks assignedDeck,String activeAbilityDescription, String cardName, CardTypes subType, String imageFileName, AdventureDeckCards cardCode, int battlePointValue, int boostedBattlePointValue) {
        super(assignedDeck,activeAbilityDescription, cardName, subType, imageFileName, cardCode);
        this.bpValue=battlePointValue;
        this.boostedBpValue=boostedBattlePointValue;
        this.isBoosted=false;
        this.activeEffect=null; //TODO: Change when Effects Implemented
    }

    @Override
    public CardData generateCardData() {
        CardData data = new CardData(
                cardID,
                cardCode,
                cardName,
                subType,
                imgSrc,
                0,
                getBattlePoints(),
                activeAbilityDescription,
                activeEffect!=null
        );
        return data;
    }

    @Override
    protected void registerwithNewPlayArea(PlayAreas stage) {
        stage.registerBoostableCard(this);
        stage.registerBattlePointContributor(this);
    }

    @Override
    protected void registerWithHand(Hand hand) {
        if (activeEffect != null) {
            hand.registerCardWithEffect(this);
        }

    }

    @Override
    public FoeCards getCard() {
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
