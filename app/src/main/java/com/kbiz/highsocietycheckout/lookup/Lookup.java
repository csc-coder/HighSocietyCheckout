/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kbiz.highsocietycheckout.lookup;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author kdot
 */
public class Lookup {
    private static HashMap<Class<?>, ArrayList<?>> contents;

    static{
        contents = new HashMap<>();
    }

    public static void add(Object toAdd){
        Class<?> clazz = toAdd.getClass();
        //add to classes list for direct class
        ArrayList clazzContainer = ensureEntryContainer(clazz);
        clazzContainer.add(toAdd);
        //add to superclass container
        Class<?>[] ifces = toAdd.getClass().getInterfaces();
        for(Class<?> ifce:ifces){
            clazzContainer = ensureEntryContainer(ifce);
            clazzContainer.add(toAdd);
        }
        Class<?> superClass = toAdd.getClass().getSuperclass();
        while(superClass != Object.class){
            clazzContainer = ensureEntryContainer(superClass);
            clazzContainer.add(toAdd);
            superClass = superClass.getSuperclass();
        }
    }

    public static <T> boolean contains(Class<T> clazz) {
        return contents.containsKey(clazz);
    }

    public static <T> T get(Class<T> clazz)  {
        if( ! contents.containsKey(clazz)){
            //try to instantiate default constructor
            Constructor<?> constructor;
            if((constructor = hasNoArgsPublicConstructor(clazz) )!= null){
                try {
                    T instance = (T) constructor.newInstance();
                    add(instance);
                    return instance;
                } catch (InstantiationException | IllegalAccessException| InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
        ensureEntryContainer(clazz);
        ArrayList<?> objects = contents.get(clazz);
        if(objects.isEmpty()){
            return null;
        }
        return (T) objects.get(0);
    }

    public static <T> List<T> getAll(Class<T> clazz)  {
        ensureEntryContainer(clazz);
        ArrayList<?> objects = contents.get(clazz);
        if(objects.isEmpty()){
            return null;
        }
        return (List<T>) objects;
    }

    private static <T> ArrayList<?> ensureEntryContainer(Class<T> clazz) {
        if(contents.containsKey(clazz)){
            return contents.get(clazz);
        }
        ArrayList<Object> container = new ArrayList<>();
        contents.put(clazz, container);


        ArrayList<Object> ifceContainer;
        //add interface containers
        for(Class<?> ifce:clazz.getClass().getInterfaces()){
            if(contents.containsKey(ifce)){
                continue;
            }
            ifceContainer = new ArrayList<>();
            contents.put(ifce, ifceContainer);
        }
        //add superclass container
        Class<?> superClass = clazz.getSuperclass();
        do{
            if(superClass == null){
                break;
            }
            if(contents.containsKey(superClass)){
                superClass = superClass.getSuperclass();
                continue;
            }
            ArrayList<Object> superClassContainer = new ArrayList<>();
            contents.put(superClass, superClassContainer);
            superClass = superClass.getSuperclass();
        } while (superClass != Object.class);
        return container;
    }

    private static Constructor<?> hasNoArgsPublicConstructor(Class<?> clazz) {
        for (Constructor<?> constructor : clazz.getConstructors()) {
            // In Java 7-, use getParameterTypes and check the length of the array returned
            if (constructor.getParameterCount() == 0) {
                return constructor;
            }
        }
        return null;
    }

}
