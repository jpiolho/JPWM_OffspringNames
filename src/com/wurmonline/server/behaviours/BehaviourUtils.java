/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wurmonline.server.behaviours;

import java.lang.reflect.Field;
import java.util.Map;

/**
 *
 * @author JPiolho
 */
public class BehaviourUtils {
    
    private static Field fieldBehaviours = null;
    public static short GetNextBehaviourType() {
        
        if(fieldBehaviours == null) {
            try {
                fieldBehaviours = Behaviours.class.getDeclaredField("behaviours");
                fieldBehaviours.setAccessible(true);
            }
            catch(Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        
        
        try {
            Behaviours instance = Behaviours.getInstance();
            Map<Short,Behaviour> behaviours = (Map<Short,Behaviour>)fieldBehaviours.get(instance);
            
            short maximumKey = 0;
            for(Short key : behaviours.keySet()) {
                maximumKey = (short)Math.max((int)maximumKey,(int)key);
            }
            
            return (short)(maximumKey+1);
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
        
    }
}
