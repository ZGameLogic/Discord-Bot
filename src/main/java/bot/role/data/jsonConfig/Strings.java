package bot.role.data.jsonConfig;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import data.serializing.SavableData;
import lombok.Getter;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Strings extends SavableData {

    private List<String[]> activeGold, activeAgility, activeKnowledge, activeMagic,
            activeStamina, activeStrength;

    private List<String[]> baneBandit, baneBlob, baneGoul, baneGiant,
            baneSkeleton, baneTroll, baneWizard, baneWolf;

    private List<String[]> staticActivity, staticAgility, staticKnowledge, staticMagic,
            staticStamina, staticStrength;

    private List<String> dayMessageStart, dayMessageEnd;

    private List<String> agilityVendorNames, knowledgeVendorNames, magicVendorNames,
            staminaVendorNames, strengthVendorNames, goldVendorJobs;

    public Strings() {
        super("strings");
        activeAgility = new LinkedList<>();
        activeKnowledge = new LinkedList<>();
        activeMagic = new LinkedList<>();
        activeStamina = new LinkedList<>();
        activeStrength = new LinkedList<>();
        activeGold = new LinkedList<>();

        baneBandit = new LinkedList<>();
        baneBlob = new LinkedList<>();
        baneGoul  = new LinkedList<>();
        baneGiant = new LinkedList<>();
        baneSkeleton = new LinkedList<>();
        baneTroll = new LinkedList<>();
        baneWizard = new LinkedList<>();
        baneWolf = new LinkedList<>();

        staticAgility = new LinkedList<>();
        staticKnowledge = new LinkedList<>();
        staticMagic = new LinkedList<>();
        staticStamina = new LinkedList<>();
        staticStrength = new LinkedList<>();
        staticActivity = new LinkedList<>();

        dayMessageStart = new LinkedList<>();
        dayMessageEnd = new LinkedList<>();

        agilityVendorNames = new LinkedList<>();
        knowledgeVendorNames = new LinkedList<>();
        magicVendorNames = new LinkedList<>();
        staminaVendorNames = new LinkedList<>();
        strengthVendorNames = new LinkedList<>();
        goldVendorJobs = new LinkedList<>();
    }

    /**
     * @return a random agility vendor name
     */
    @JsonIgnore
    public String getAgilityVendorName(){
        Random random = new Random();
        return agilityVendorNames.get(random.nextInt(agilityVendorNames.size()));
    }

    /**
     * @return a random knowledge vendor name
     */
    @JsonIgnore
    public String getKnowledgeVendorName(){
        Random random = new Random();
        return knowledgeVendorNames.get(random.nextInt(knowledgeVendorNames.size()));
    }

    /**
     * @return a random magic vendor name
     */
    @JsonIgnore
    public String getMagicVendorName(){
        Random random = new Random();
        return magicVendorNames.get(random.nextInt(magicVendorNames.size()));
    }

    /**
     * @return a random stamina vendor name
     */
    @JsonIgnore
    public String getStaminaVendorName(){
        Random random = new Random();
        return staminaVendorNames.get(random.nextInt(staminaVendorNames.size()));
    }

    /**
     * @return a random strength vendor name
     */
    @JsonIgnore
    public String getStrengthVendorName(){
        Random random = new Random();
        return strengthVendorNames.get(random.nextInt(strengthVendorNames.size()));
    }

    /**
     * @return a random gold job
     */
    @JsonIgnore
    public String getGoldJobName(){
        Random random = new Random();
        return goldVendorJobs.get(random.nextInt(goldVendorJobs.size()));
    }

    /**
     * @return a random string to start a new day message
     */
    @JsonIgnore
    public String getDayMessageStart(){
        Random random = new Random();
        return dayMessageStart.get(random.nextInt(dayMessageStart.size()));
    }

    /**
     * @return a random string to end a new day message
     */
    @JsonIgnore
    public String getDayMessageEnd(){
        Random random = new Random();
        return dayMessageEnd.get(random.nextInt(dayMessageEnd.size()));
    }

    /**
     * @return a random item name and description pair for static agility
     */
    @JsonIgnore
    public String[] getStaticAgilityItem(){
        Random random = new Random();
        return staticAgility.get(random.nextInt(staticAgility.size()));
    }

    /**
     * @return a random item name and description pair for static knowledge
     */
    @JsonIgnore
    public String[] getStaticKnowledgeItem(){
        Random random = new Random();
        return staticKnowledge.get(random.nextInt(staticKnowledge.size()));
    }

    /**
     * @return a random item name and description pair for static magic
     */
    @JsonIgnore
    public String[] getStaticMagicItem(){
        Random random = new Random();
        return staticMagic.get(random.nextInt(staticMagic.size()));
    }

    /**
     * @return a random item name and description pair for static stamina
     */
    @JsonIgnore
    public String[] getStaticStaminaItem(){
        Random random = new Random();
        return staticStamina.get(random.nextInt(staticStamina.size()));
    }

    /**
     * @return a random item name and description pair for static strength
     */
    @JsonIgnore
    public String[] getStaticStrengthItem(){
        Random random = new Random();
        return staticStrength.get(random.nextInt(staticStrength.size()));
    }

    /**
     * @return a random item name and description pair for static activity
     */
    @JsonIgnore
    public String[] getStaticActivityItem(){
        Random random = new Random();
        return staticActivity.get(random.nextInt(staticActivity.size()));
    }

    /**
     * @return a random item name and description pair for active agility
     */
    @JsonIgnore
    public String[] getActiveAgilityItem(){
        Random random = new Random();
        return activeAgility.get(random.nextInt(activeAgility.size()));
    }

    /**
     * @return a random item name and description pair for active knowledge
     */
    @JsonIgnore
    public String[] getActiveKnowledgeItem(){
        Random random = new Random();
        return activeKnowledge.get(random.nextInt(activeKnowledge.size()));
    }

    /**
     * @return a random item name and description pair for active magic
     */
    @JsonIgnore
    public String[] getActiveMagicItem(){
        Random random = new Random();
        return activeMagic.get(random.nextInt(activeMagic.size()));
    }

    /**
     * @return a random item name and description pair for active stamina
     */
    @JsonIgnore
    public String[] getActiveStaminaItem(){
        Random random = new Random();
        return activeStamina.get(random.nextInt(activeStamina.size()));
    }

    /**
     * @return a random item name and description pair for active strength
     */
    @JsonIgnore
    public String[] getActiveStrengthItem(){
        Random random = new Random();
        return activeStrength.get(random.nextInt(activeStrength.size()));
    }

    /**
     * @return a random item name and description pair for active gold
     */
    @JsonIgnore
    public String[] getActiveGoldItem(){
        Random random = new Random();
        return activeGold.get(random.nextInt(activeGold.size()));
    }

    /**
     * @return a random item name and description pair for bane bandit
     */
    @JsonIgnore
    public String[] getBaneBanditItem(){
        Random random = new Random();
        return baneBandit.get(random.nextInt(baneBandit.size()));
    }

    /**
     * @return a random item name and description pair for bane blob
     */
    @JsonIgnore
    public String[] getBaneBlobItem(){
        Random random = new Random();
        return baneBlob.get(random.nextInt(baneBlob.size()));
    }

    /**
     * @return a random item name and description pair for bane goul
     */
    @JsonIgnore
    public String[] getBaneGoulItem(){
        Random random = new Random();
        return baneGoul.get(random.nextInt(baneGoul.size()));
    }

    /**
     * @return a random item name and description pair for bane giant
     */
    @JsonIgnore
    public String[] getBaneGiantItem(){
        Random random = new Random();
        return baneGiant.get(random.nextInt(baneGiant.size()));
    }

    /**
     * @return a random item name and description pair for bane skeleton
     */
    @JsonIgnore
    public String[] getBaneSkeletonItem(){
        Random random = new Random();
        return baneSkeleton.get(random.nextInt(baneSkeleton.size()));
    }

    /**
     * @return a random item name and description pair for bane Troll
     */
    @JsonIgnore
    public String[] getBaneTrollItem(){
        Random random = new Random();
        return baneTroll.get(random.nextInt(baneTroll.size()));
    }

    /**
     * @return a random item name and description pair for bane wizard
     */
    @JsonIgnore
    public String[] getBaneWizardItem(){
        Random random = new Random();
        return baneWizard.get(random.nextInt(baneWizard.size()));
    }

    /**
     * @return a random item name and description pair for bane wolf
     */
    @JsonIgnore
    public String[] getBaneWolfItem(){
        Random random = new Random();
        return baneWolf.get(random.nextInt(baneWolf.size()));
    }
}