/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpiolho.wurmmod.offspringnames;

import com.wurmonline.server.questions.VillageFoundationQuestion;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javassist.CtClass;
import javassist.CtPrimitiveType;
import javassist.bytecode.Descriptor;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;
import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.WurmMod;

/**
 *
 * @author JPiolho
 */
public class ModOffspringNames implements WurmMod, Configurable, Initable {

    
    private boolean replaceBuiltInNames = false;
    private boolean checkDuplicate = true;
    
    private Logger logger = Logger.getLogger(this.getClass().getName());
    
    @Override
    public void configure(Properties properties) {
        replaceBuiltInNames = Boolean.parseBoolean(properties.getProperty("replaceBuiltInNames",Boolean.toString(replaceBuiltInNames)));
        checkDuplicate = Boolean.parseBoolean(properties.getProperty("checkDuplicate",Boolean.toString(checkDuplicate)));
        
        logger.log(Level.INFO,"replaceBuiltInNames: " + replaceBuiltInNames);
    }

    
    
    private ArrayList<String> namesMale,namesFemale,namesGeneric;
    
    
    private void readNameFile()
    {
        namesMale = new ArrayList<>();
        namesFemale = new ArrayList<>();
        namesGeneric = new ArrayList<>();
        
        
        int mode = 0;
        
        try {
            BufferedReader br = new BufferedReader(new FileReader("mods" + File.separator + "JPWM_OffspringNames" + File.separator + "names.txt"));
        
            String line;
            while((line = br.readLine()) != null)
            {
                line = line.trim();
                
                
                if(line.length() == 0)
                    continue;
                
                if(line.startsWith("#"))
                    continue;
                
                
                switch (line) {
                    case "GENERIC:":
                        mode = 0;
                        break;
                    case "MALE:":
                        mode = 1;
                        break;
                    case "FEMALE:":
                        mode = 2;
                        break;
                    default:
                        ArrayList<String> targetList;
                        
                        switch(mode) {
                            default:
                            case 0: targetList = namesGeneric; break;
                            case 1: targetList = namesMale; break;
                            case 2: targetList = namesFemale; break;
                        }
                    
                        if(checkDuplicate) {
                            if(targetList.contains(line))
                                break;
                        }
                        
                        targetList.add(line);
                        break;
                }
                
                
            }
            
            br.close();
            
            logger.log(Level.INFO,"Loaded " + namesGeneric.size() + " generic names, " + namesMale.size() + " male names and " + namesFemale.size() + " female names.");
        } 
        catch(FileNotFoundException ex)
        {
            logger.log(Level.SEVERE,"Could not find names file. " + ex.getMessage());
        }
        catch(IOException ex)
        {
            logger.log(Level.SEVERE,"Failed to read names file");
        }
    }
    
    private String[] getNames(ArrayList<String> loadedNames,String[] builtin)
    {
        String[] arr;
        
        if(replaceBuiltInNames) {
            arr = new String[loadedNames.size()];
            loadedNames.toArray(arr);
            return arr;
        }

        ArrayList<String> newList = new ArrayList<String>();
        
        for(int i=0;i<builtin.length;i++)
            newList.add(builtin[i]);
        
        if(!checkDuplicate) {
            newList.addAll(loadedNames);
        }
        else {
            for(int i=0;i<loadedNames.size();i++) {
                if(!newList.contains(loadedNames.get(i)))
                    newList.add(loadedNames.get(i));
            }
        }

        
        arr = new String[newList.size()];
        newList.toArray(arr);
        return arr;
    }
    
    @Override
    public void init() {
        
        readNameFile();
        
        
        HookManager.getInstance().registerHook("com.wurmonline.server.creatures.Offspring", "createMaleNames", "()[Ljava/lang/String;", new InvocationHandlerFactory() {

            @Override
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {

                    @Override
                    public Object invoke(Object o, Method method, Object[] os) throws Throwable {
                        return getNames(namesMale,(String[])method.invoke(o, os));
                    }
                };
            }
            
        });
        
        
        HookManager.getInstance().registerHook("com.wurmonline.server.creatures.Offspring", "createFemaleNames", "()[Ljava/lang/String;", new InvocationHandlerFactory() {

            @Override
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {

                    @Override
                    public Object invoke(Object o, Method method, Object[] os) throws Throwable {
                        return getNames(namesFemale,(String[])method.invoke(o, os));
                    }
                };
            }
            
        });
        
        
        HookManager.getInstance().registerHook("com.wurmonline.server.creatures.Offspring", "createGenericNames", "()[Ljava/lang/String;", new InvocationHandlerFactory() {

            @Override
            public InvocationHandler createInvocationHandler() {
                return new InvocationHandler() {

                    @Override
                    public Object invoke(Object o, Method method, Object[] os) throws Throwable {
                        return getNames(namesGeneric,(String[])method.invoke(o, os));
                    }
                };
            }
            
        });
    }
    
    
    
    
    
}
