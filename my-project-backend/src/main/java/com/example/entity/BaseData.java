package com.example.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.function.Consumer;

public interface BaseData {
    default <V> V asViewObject(Class<V> clazz, Consumer<V> consumer){  //0
        V v = this.asViewObject(clazz);
        consumer.accept(v);
        return v;
    }
    default <V> V asViewObject(Class<V> clazz){  //将当前对象转化为ViewObject,传入参数为一个类
        try{
            Field[] declaredFields = clazz.getDeclaredFields();  //获取要转换类的所有字段
            Constructor<V> constructor = clazz.getConstructor();  //获取构造器
            V v = constructor.newInstance();  //通过构造器把指定类型vo对象构造出来
            for (Field declaredField : declaredFields) convert(declaredField, v);
            return v;
        }catch (ReflectiveOperationException exception){
            throw new RuntimeException();
        }
    }
    private void convert(Field field, Object vo){  //拷贝方法
        try {
            Field source = this.getClass().getDeclaredField(field.getName());  //获取当前类型字段
            field.setAccessible(true);  //让字段允许访问
            source.setAccessible(true);
            field.set(vo, source.get(this));
        } catch (IllegalAccessException | NoSuchFieldException ignored) {

        }
    }
}
