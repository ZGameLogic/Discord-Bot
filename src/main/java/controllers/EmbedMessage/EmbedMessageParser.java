package controllers.EmbedMessage;


import bot.role.data.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public abstract class EmbedMessageParser {
    /**
     * This is a guide for creating the patterns for an embedded message:
     *      =============================================================================
     *                                      Specific stats
     *      =============================================================================
     *      Upper : Stat with item modifier added.                          Ex: 46
     *      Lower: stat with item modifier not added                        Ex: 34
     *      UpperLower : stat with item modifier separated but included.    Ex: 34 (+12)
     *      SR : strength
     *      KN : knowledge
     *      MA : magic
     *      AG : agility
     *      SA : stamina
     *
     *      Example: "%SR", "%sr", "%SRsr"
     *      =============================================================================
     *                                          Stats
     *      =============================================================================
     *      Wi : wins
     *      Df : defeats
     *      Go : gold
     *      Tv : tournament victories
     *      Al : activities left
     *      Df : defends left
     *      Nm : name of player
     *      Rn : rank of player
     *      In : item name
     *      Id : item description
     */

    public static MessageEmbed parse(String parser, Player player){
        EmbedBuilder eb = new EmbedBuilder();

        return eb.build();
    }

    private static String getValue(String value, Player player){
//        switch(value.replace("%", "")){
//            case "SR":
//                return player.getStrength() + "";
//            case "sr":
//                return player.getRawStrengthStat() + "";
//            case "Sr":
//                if(player.getItem().getItemType() == Item.StatType.STATIC_STRENGTH){
//                    return player.getRawStrength() + " (+" + player.getItem().getStatIncrease() + ")";
//                } else {
//                    return player.getRawStrength() + "";
//                }
//            case "KN":
//                return player.getKnowledge() + "";
//            case "kn":
//                return player.getRawKnowledge() + "";
//            case "Kn":
//                if(player.getItem().getItemType() == Item.StatType.STATIC_KNOWLEDGE){
//                    return player.getRawKnowledge() + " (+" + player.getItem().getStatIncrease() + ")";
//                } else {
//                    return player.getRawKnowledge() + "";
//                }
//            case "MA":
//                return player.getMagic() + "";
//            case "ma":
//                return player.getRawMagic() + "";
//            case "Ma":
//                if(player.getItem().getItemType() == Item.StatType.STATIC_MAGIC){
//                    return player.getRawMagic() + " (+" + player.getItem().getStatIncrease() + ")";
//                } else {
//                    return player.getRawMagic() + "";
//                }
//            case "AG":
//                return player.getAgility() + "";
//            case "ag":
//                return player.getRawAgility() + "";
//            case "Ag":
//                if(player.getItem().getItemType() == Item.StatType.STATIC_AGILITY){
//                    return player.getRawAgility() + " (+" + player.getItem().getStatIncrease() + ")";
//                } else {
//                    return player.getRawAgility() + "";
//                }
//            case "SA":
//                return player.getStamina() + "";
//            case "sa":
//                return player.getRawStamina() + "";
//            case "Sa":
//                if(player.getItem().getItemType() == Item.StatType.STATIC_AGILITY){
//                    return player.getRawStamina() + " (+" + player.getItem().getStatIncrease() + ")";
//                } else {
//                    return player.getRawStamina() + "";
//                }
//            case "Wi":
//                return player.getWins() + "";
//            case "Df":
//                return player.getLosses() + "";
//            case "Go":
//                return player.getGold() + "";
//            case "Tv":
//                return player.getTournamentWins() + "";
//            case "Al":
//                return player.getActivitiesLeft() + "";
//            case "Df":
//                return "";
//
//
//            default:
//                return "";
//        }
        /**
         *      *      Df : defeats
         *      *      Go : gold
         *      *      Tv : tournament victories
         *      *      Al : activities left
         *      *      Df : defends left
         *      *      Nm : name of player
         *      *      Rn : rank of player
         *      *      In : item name
         *      *      Id : item description
         */
        return "";
    }

}
