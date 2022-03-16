package com.team9.questgame.Entities.cards;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.team9.questgame.Data.CardData;
import com.team9.questgame.exception.IllegalCardStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property="cardID"
)
public abstract class Cards {
    @JsonIgnore
    protected Logger LOG;
    @JsonIgnore
    protected final Decks assignedDeck;
    protected String activeAbilityDescription;
    protected String cardName;
    protected long cardID;
    protected final CardTypes subType;
    protected String imgSrc;
    protected static long nextId=0;
    protected AllCardCodes cardCode;

    @JsonBackReference
    protected PlayAreas location;

    protected <T extends Enum<T> & AllCardCodes> Cards(Decks assignedDeck,String activeAbilityDescription, String cardName, CardTypes subType, String imgSrc, T cardCode) {
        this.cardID = nextId++;

        LOG= LoggerFactory.getLogger(this.getClass());
        this.assignedDeck = assignedDeck;
        this.activeAbilityDescription = activeAbilityDescription;
        this.cardName = cardName;
        this.subType = subType;
        this.imgSrc = imgSrc;
        this.cardCode = cardCode;
    }

    public String getCardName() {
        return cardName;
    }

    public CardTypes getSubType() {
        return subType;
    }

    public String getImgSrc() {
        return imgSrc;
    }

    public long getCardID() {
        return cardID;
    }

    public AllCardCodes getCardCode() {
        return cardCode;
    }

    public CardArea getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cards)) return false;
        Cards cards = (Cards) o;
        return cardID == cards.cardID && cardCode==cards.cardCode;
    }



    @Override
    public int hashCode() {
        return Objects.hash(cardID);
    }

    public void discardCard() {

        if(location!=null) {
            onLocationChanged();
            location=null;
        }

        //notify assigned deck that card was discarded
        assignedDeck.notifyDiscard(this);

    }

    abstract public CardData generateCardData();


    boolean playCard(CardArea cardArea) {
        if(location==cardArea)
        {
            throw new IllegalCardStateException("Card cannot be played into the same area.");
        }
        else if(cardArea == null) {
            throw new IllegalCardStateException("Card cannot be played to null. Card must be played into another card area or discard.");
        }

        return cardArea.receiveCard(this);
    }

    boolean playCard(PlayAreas playArea) {
        CardArea tmp = playArea;
        boolean rc = playCard(tmp);
        if(rc) {
            if(location!=null) {
                onLocationChanged();
            }
            location = playArea;
        }
        return rc;
    }


    abstract protected void onLocationChanged();

}
