/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jpiolho.wurmmod.offspringnames;

import com.jpiolho.wurmmod.base.ItemActionListener;
import com.jpiolho.wurmmod.base.ListenerResult;
import com.jpiolho.wurmmod.base.ModBase;
import com.wurmonline.server.MiscConstants;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.behaviours.NamingTagBehaviour;
import com.wurmonline.server.behaviours.NoSuchBehaviourException;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.items.ItemTypes;
import com.wurmonline.server.questions.Question;
import com.wurmonline.server.questions.VillageFoundationQuestion;
import com.wurmonline.shared.constants.ItemMaterials;
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
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.CtPrimitiveType;
import javassist.bytecode.Descriptor;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.classhooks.InvocationHandlerFactory;
import org.gotti.wurmunlimited.modloader.interfaces.Configurable;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.ItemTemplatesCreatedListener;
import org.gotti.wurmunlimited.modloader.interfaces.PreInitable;
import org.gotti.wurmunlimited.modloader.interfaces.WurmMod;
import org.gotti.wurmunlimited.modsupport.ItemTemplateBuilder;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;

/**
 *
 * @author JPiolho
 */
public class ModOffspringNames implements WurmMod, Configurable, Initable, PreInitable,ItemTemplatesCreatedListener {

    
    private boolean replaceBuiltInNames = false;
    private boolean checkDuplicate = true;
    
    private static Logger logger = Logger.getLogger(ModOffspringNames.class.getName());
    
    @Override
    public void configure(Properties properties) {
        replaceBuiltInNames = Boolean.parseBoolean(properties.getProperty("replaceBuiltInNames",Boolean.toString(replaceBuiltInNames)));
        checkDuplicate = Boolean.parseBoolean(properties.getProperty("checkDuplicate",Boolean.toString(checkDuplicate)));
        
        logger.log(Level.INFO,"replaceBuiltInNames: " + replaceBuiltInNames);
        logger.log(Level.INFO,"checkDuplicate: " + checkDuplicate);
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

    
    public static int iid_namingtag = 0;
    @Override
    public void onItemTemplatesCreated() {
       
        new NamingTagBehaviour();
        
        try {
            ItemTemplateBuilder builder = new ItemTemplateBuilder("jp.offspringnames.namingtag");

            builder.name("naming tag","naming tags","A small piece of wood used to carve a name.");
            builder.modelName("model.jpmod.offspringname.namingtag.");
            builder.descriptions("excellent", "good", "ok", "poor");
            builder.itemTypes(new short[]{
                ItemTypes.ITEM_TYPE_WOOD
           });

            builder.imageNumber((short)60);
            builder.behaviourType(NamingTagBehaviour.ID);
            builder.combatDamage(0);
            builder.decayTime(9072000L);
            builder.dimensions(10, 2, 2);
            builder.primarySkill((int)MiscConstants.NOID);
            builder.bodySpaces(MiscConstants.EMPTY_BYTE_PRIMITIVE_ARRAY);

            builder.difficulty(1.0f);
            builder.weightGrams(500);
            builder.material(ItemMaterials.MATERIAL_WOOD_PINE);
            builder.isTraded(true);
            builder.value(50);
        
            iid_namingtag = builder.build().getTemplateId();
        } catch(IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static boolean action(Action act, Creature performer, Item source, Creature target, short action, float counter) {
        
        try {
        if(source.getTemplateId() == iid_namingtag)
            if(((NamingTagBehaviour)source.getBehaviour()).action(act, performer, source, target, action, counter))
                return true;
        }
        catch(NoSuchBehaviourException ex)
        {
            return false;
        }
        
        
        return false;
    }
    
    public static ActionEntry actionCarveName,actionTag;
    public static Class classCarvingNameQuestion;
    
    @Override
    public void preInit() {
        
        
        ModActions.init();
        
        actionCarveName = ActionEntry.createEntry((short)ModActions.getNextActionId(), "Carve name", "carving name",new int[] {23});
        ModActions.registerAction(actionCarveName);     
        
        actionTag = ActionEntry.createEntry((short)ModActions.getNextActionId(), "Tag", "tagging",new int[] {23});
        ModActions.registerAction(actionTag);    
        
        
        try {
            
            ClassPool cpool = HookManager.getInstance().getClassPool();
            //  List getBehavioursFor(Creature performer, Item source, Item target) {

            String descriptor = Descriptor.ofMethod(cpool.get("java.util.List"), new CtClass[] {
                cpool.get("com.wurmonline.server.creatures.Creature"),
                cpool.get("com.wurmonline.server.items.Item"),
                cpool.get("com.wurmonline.server.creatures.Creature"),
            });
            
            
            CtClass cClass = cpool.get("com.wurmonline.server.behaviours.CreatureBehaviour"); 
            CtMethod method = cClass.getMethod("getBehavioursFor", descriptor);
            
            method.insertAfter("{" +
                    "if(subject.getTemplateId() == " + ModOffspringNames.class.getName() + ".iid_namingtag) {" +
                        "$_.add(" + ModOffspringNames.class.getName() + ".actionTag);" +
                    "}" +
            "}");
        } 
        catch(Exception ex)
        {
            logger.log(Level.SEVERE, "Compile code", ex);
        }
        
        try {
            
            ClassPool cpool = HookManager.getInstance().getClassPool();
            
            String descriptor = Descriptor.ofMethod(CtClass.booleanType, new CtClass[] {
                cpool.get("com.wurmonline.server.behaviours.Action"),
                cpool.get("com.wurmonline.server.creatures.Creature"),
                cpool.get("com.wurmonline.server.items.Item"),
                cpool.get("com.wurmonline.server.creatures.Creature"),
                CtClass.shortType,
                CtClass.floatType
            });
            
            
            CtClass cClass = cpool.get("com.wurmonline.server.behaviours.CreatureBehaviour"); 
            CtMethod method = cClass.getMethod("action", descriptor);
            
            method.insertAfter( "{" +
                                    "if(" + ModOffspringNames.class.getName() + ".action($$)) {" +
                                        "return true;" +
                                    "}" +
                                "}");
            
        } 
        catch(Exception ex)
        {
            logger.log(Level.SEVERE, "Compile code", ex);
        }
    }
    
    
    


    
    
}
