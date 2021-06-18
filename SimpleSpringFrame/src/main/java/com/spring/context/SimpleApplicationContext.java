package com.spring.context;

import com.spring.annontation.Autowired;
import com.spring.annontation.Component;
import com.spring.annontation.ComponentScan;
import com.spring.annontation.Scope;
import com.spring.bean.InitalizingBean;
import com.spring.eneity.BeanDefinition;
import com.spring.processor.BeanPostProcessor;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实现Spring中ApplicationContext
 * 解析XML/注解 所配置/注解的类/方法
 * @Author: tangwq
 */
public class SimpleApplicationContext {

    private Class configClass;

    //单例池
    private Map<String,Object> singletonObjects = new ConcurrentHashMap<>();

    //BeanbeanDefinition的集合
    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    //前置处理器集合
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    /**
     * 构造函数
     * 实现了解析ComponentScan
     * 扫描路径下所有被Component注解的类 （以后可以拓展@Service，@bean等等）
     * @param configClass
     */
    public SimpleApplicationContext(Class configClass){
        this.configClass = configClass;

        //解析配置类
        //解析ComponentSacn注解 ---> 获取扫描路径----> 扫描--->Beandefiantion-->BeanDefinationMap
        Register(configClass);
        for(Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()){
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if(beanDefinition.getScope().equals("singleton")){
                //创建Bean对象（实例化，初始化Bean）
                Object bean = createBean(beanDefinition, beanName);
                //存入单例池
                singletonObjects.put(beanName,bean);
            }
        }



    }

    /**
     * 根据beanDefinition创建bean对象
     * @param beanDefinition
     * @return
     */
    private Object createBean(BeanDefinition beanDefinition, String beanName){
        //获取class对象
        Class clazz = beanDefinition.getClazz();
        try {
            //反射创建 bean实例对象  也就是Bean的实例化
            Object instance = clazz.getDeclaredConstructor().newInstance();

            //依赖注入（属性赋值）
            for (Field declaredField : clazz.getDeclaredFields()) {//获取实例对象中的属性值
                if(declaredField.isAnnotationPresent(Autowired.class)){ //判断该属性是否是被Autowired注解
                    //bean注入（赋值）
                    Object bean = getBean(declaredField.getName());
                    declaredField.setAccessible(true);
                    declaredField.set(instance,bean);
                }
            }

            //Bean 初始化操作（其实就是调用前置处理器，后置处理器，初始化器等等）
            //Aware回调 （暂时不写）

            // BeanPostProcessor 前置处理器---初始化后调用的方法(前置处理器都在前置处理器集合中，他在Bean初始化之前就放进去了)
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance,beanName);
            }

            //初始器
            if(instance instanceof InitalizingBean){ //如果bean实例它实现了InitalizingBean接口，则调用实现的afterPropertiesSet方法
                ((InitalizingBean)instance).afterPropertiesSet(); //调用该方法进行一些操作
            }
            // BeanPostProcessor 前置处理器---初始化后调用的方法
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(instance,beanName);
            }

            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void Register(Class configClass) {
        // 获取configClass上的ComponentScan注解
        ComponentScan componentScanAnn = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
        // 获取注解的value，也就是扫描路径path
        String path = componentScanAnn.value();
        //扫描 （根据路径-包名 获取所有类） 使用类加载器获取
        // 获取类加载器（这里获得的是AppClassLoader 应用程序加载器） PS：了解三种加载器分别加载哪些路径的类（jre/lib，jre/ext/lib，classpath）
        ClassLoader classLoader = SimpleApplicationContext.class.getClassLoader();
        // 将com.xx.xx转换位com/xx/xx的形式
        path = path.replaceAll("\\.","/");
        System.out.println(path);
        // 通过路径获取资源
        URL recouce = classLoader.getResource(path+"/");
        // 获取资源下的class文件
        File file = new File(recouce.getFile());
        if(file.isDirectory()){

            File []files = file.listFiles();
            for (File f : files) {
                //获取该文件的路径
                String filePath = f.getAbsolutePath();
                System.out.println(filePath);
                // 获取类名  类绝对路径是这样的G:\JAVA\IdeaProjects\HandwritingFrame\SimpleSpringFrame\target\classes\com\runtest\service\UserService.class
                // 实际上我们可以直接从com开始截取， 基本都是包的构造都是com\x\x\x
                String className = filePath.substring(filePath.lastIndexOf("classes")+"classes".length()+1,filePath.lastIndexOf(".class"));
                // 组合成 反射可以加载的类的形式 xx.xx.className
                className = className.replaceAll("\\\\",".");
                System.out.println(className);

                try {
                    // 通过类加载器加载class
                    Class<?> aclass = classLoader.loadClass(className);
                    //只处理被Component注解的类
                    if(aclass.isAnnotationPresent(Component.class)){ //判断该类是否是被Component注解的

                        //优先实例化前置处理器bean（因为在单例Bean（普通Bean）初始化时要调用的）
                        if (BeanPostProcessor.class.isAssignableFrom(aclass)) {
                            //实例化前置处理器 （Spring源码是通过getBean方式在创建的，我们这里直接创建）
                            BeanPostProcessor instance = (BeanPostProcessor) aclass.getDeclaredConstructor().newInstance();
                            //加入前置处理器集合中
                            beanPostProcessorList.add(instance);
                        }

                        //被Component注解的，那么他就是一个Bean
                        // 获取类上的Component注解
                        Component componentAnn = aclass.getDeclaredAnnotation(Component.class);
                        // 获取Bean的名字（注解的参数）
                        String beanName = componentAnn.value();

                        //初始化一个 BeanDefinition对象
                        BeanDefinition beanDefinition = new BeanDefinition();

                        if(aclass.isAnnotationPresent(Scope.class)){//判断类是否有作用域注解
                            Scope scopeAnn = aclass.getDeclaredAnnotation(Scope.class);
                            beanDefinition.setScope(scopeAnn.value());

                        }else{ // 没有Scope默认为单例
                            beanDefinition.setScope("singleton");
                        }
                        beanDefinition.setClazz(aclass);
                        // 将BeanDefination存入Map中
                        beanDefinitionMap.put(beanName,beanDefinition);


                    }
                } catch (ClassNotFoundException | NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    /**
     * 获取Bean实例
     * @param beanName
     * @return
     */
    public Object getBean(String beanName){
        if(beanDefinitionMap.containsKey(beanName)){
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName); // 拿到对应的BeanDefination对象
            if(beanDefinition.getScope().equals("singleton")){ //如果该bean是单例Bean
                return singletonObjects.get(beanName); //则直接从单例池中拿出bean对象
            }else{ //不是单例Bean则直接创建一个Bean对象
                // 创建bean对象
                return createBean(beanDefinition,beanName);
            }
        }else{
            // beanName不存在
            throw new RuntimeException();
        }
    }

}
