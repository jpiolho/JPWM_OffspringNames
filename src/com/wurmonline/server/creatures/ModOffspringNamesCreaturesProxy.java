/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wurmonline.server.creatures;

import java.io.IOException;

/**
 *
 * @author JPiolho
 */
public class ModOffspringNamesCreaturesProxy {
    public static String GetMaleName() {
        return Offspring.generateMaleName();
    }
    
    public static String GetFemaleName() {
        return Offspring.generateFemaleName();
    }
    
    public static String GetGenericName() {
        return Offspring.generateGenericName();
    }
    
    public static void SaveCreatureName(CreatureStatus status,String name) throws IOException {
        status.saveCreatureName(name);
    }
}
