package com.team9.questgame.Entities.Effects.CardEffects;

import com.team9.questgame.Entities.Effects.EffectResolverService;
import com.team9.questgame.Entities.Effects.Effects;
import com.team9.questgame.Entities.Effects.TargetSelector;
import com.team9.questgame.Entities.Players;

import java.util.ArrayList;
import java.util.HashMap;

public class PlagueEffect extends Effects {
    @Override
    protected ArrayList<TargetSelector> initTargetSelectors() {
        return null; //Trivial case, not needed
    }

    @Override
    protected void onActivated() {
        nextState();
    }

    @Override
    protected void onEffectResolution() {
        HashMap<Players,Integer> targetList = new HashMap<>();
        targetList.put(activatedBy,2);
        EffectResolverService.getService().playerLoosesShieldsHashMap(targetList);
        nextState();
    }
}