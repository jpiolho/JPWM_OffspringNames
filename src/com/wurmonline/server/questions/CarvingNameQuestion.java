/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wurmonline.server.questions;

import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.behaviours.NamingTagBehaviour;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.creatures.ModOffspringNamesCreaturesProxy;
import com.wurmonline.server.items.Item;
import java.util.Properties;

/**
 *
 * @author JPiolho
 */
public class CarvingNameQuestion extends Question {

    private int maxChars = 0;
    
    private String randomizedName = "";
    
    public CarvingNameQuestion(Creature aResponder,long target,int maxChars) {
        super(aResponder, "Carving on the naming tag", "What name would you like to carve?", 2, target);
        
        this.maxChars = maxChars;
    }

    
    public void setRandomizedName(String name) {
        this.randomizedName = name;
    }
    
    
    @Override
    public void answer(Properties properties) {
        this.setAnswer(properties);
        
        boolean rndmale = properties.containsKey("rndmale");
        boolean rndfemale = properties.containsKey("rndfemale");
        
        if(rndmale || rndfemale) {
            String rndname = "";
            
            if(rndmale) rndname = ModOffspringNamesCreaturesProxy.GetMaleName();
            if(rndfemale) rndname = ModOffspringNamesCreaturesProxy.GetFemaleName();
            
            
            rndname = Character.toUpperCase(rndname.charAt(0)) + rndname.substring(1);
            
            CarvingNameQuestion question = new CarvingNameQuestion(getResponder(), this.target, this.maxChars);
            question.setRandomizedName(rndname);
            question.sendQuestion();
        } else {
            Item item;
            try {
                item = Items.getItem(this.target);
            } catch(NoSuchItemException ex) {
                getResponder().getCommunicator().sendNormalServerMessage("The item you're trying to carve no longer exists.");
                return;
            }
            
            
            String name = properties.getProperty("answer").trim();
            if(name.length() == 0) {
                getResponder().getCommunicator().sendNormalServerMessage("You decide not to carve right now.");
                return;
            }
            
            if(name.length() > NamingTagBehaviour.GetMaximumCharacters(item) && (randomizedName.length() > 0 && !name.equals(randomizedName))) {
                getResponder().getCommunicator().sendNormalServerMessage("That name is too long for the quality of this tag.");
                return;
            }
            
            if(containsIllegalCharacters(name)) {
                getResponder().getCommunicator().sendNormalServerMessage("There are certain illegal characters that you don't know how to carve."); 
                return;
            }
                        
            item.setDescription(name);
            getResponder().getCommunicator().sendNormalServerMessage("You carve '" + name + "' on the naming tag.");
        }
    }
    
    private boolean containsIllegalCharacters(String text) {
        char[] chars = text.toCharArray();
        
        for(int x=0;x<chars.length;x++) {
            if("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".indexOf(chars[x]) < 0)
                return true;
        }
        
        return false;
    }

    @Override
    public void sendQuestion() {
        StringBuilder buf = new StringBuilder(this.getBmlHeader());
        
        buf.append(
            "input{id='answer';text='" + randomizedName + "';maxchars='" + this.maxChars + "'}" +
            "harray{" + 
                "button{text='Random Male name';id='rndmale'}" +
                "label{text=' ';id='spacedlxg'}" +
                "button{text='Random Female name';id='rndfemale'}" +
            "}"
        );
        
        buf.append(this.createAnswerButton2());
        this.getResponder().getCommunicator().sendBml(300, 300, true, true, buf.toString(), 200, 200, 200, this.title);
    }
    
}
