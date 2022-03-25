import Card from "../cards/Card";
import CardImages from "../../assets/images/index";
import { useHandOversize } from "../../stores/generalStore"

function PlayerHand(props){
    const handOversize = useHandOversize();
    console.log(`handOversize=${handOversize}`);

    const Rendercards = props.cardsInHand?.map((card) => (
        props.isMyHand ? (
           <Card playerID={props.playerID} card={card} key={card.cardID} cardImage={card.imgSrc} selectedAllowed={(props.isMyHand) || (props.isMyHand && handOversize)} canGrow={props.isMyHand} cardOwner={props.playerName} isActive={false}></Card>
        ) : (
            <Card playerID={props.playerID} card={card} key={card.cardID} cardImage={CardImages.Back_Adventure} selectedAllowed={props.isMyHand} canGrow={props.isMyHand} cardOwner={props.playerName} isActive={false}></Card>
        )
    ));

    const RenderActiveCards = props.activeCards?.map((card) => (
        <>
            <Card card={card} key={card.cardID} cardImage={card.imgSrc} selectedAllowed={false} canGrow={false} cardOwner={props.playerName} isActive={true}></Card>
        </>
    ));

    let newTop = 70;
    if(props.activeCards.length > 0){
        newTop += 70;
    }

    return(
        <div>
            <div>
                <Card cardImage={props.rank} selectedAllowed={false} canGrow={false} ></Card>
                <img
                    src={props.shield}
                    style={{
                        width:33,
                        height:30,
                        top: 3,
                        left: 58,
                        position: "absolute"
                    }}
                />     
                <p style={{position:"absolute",top:10,left:85,fontSize:13}}>{"x" + props.numShields}</p> 
                <p style={{position:"absolute",top:43,left:65,fontSize:13}}>{props.playerName}</p>           
            </div>

            <div
            style={{
                position: "relative",
                top: newTop,
                width:900,
                left:-60,
            }}>
                {typeof props.cardsInHand !== 'undefined' && props.cardsInHand.length > 0 &&
                    Rendercards
                }
                <div style={{top: -70, left:60, position:"absolute"}}>
                    {typeof props.cardsInHand !== 'undefined' && props.cardsInHand.length > 0 &&
                        RenderActiveCards
                    }
                </div>
            </div>
        </div>
    );
}
export default PlayerHand;