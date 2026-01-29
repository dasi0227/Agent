package com.dasi.domain.ai.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import com.dasi.domain.ai.model.entity.ArmoryRequestEntity;
import com.dasi.domain.ai.service.armory.ArmoryContext;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;

@Slf4j
public abstract class AbstractArmoryNode extends AbstractMultiThreadStrategyRouter<ArmoryRequestEntity, ArmoryContext, String> {

    @Resource
    protected ApplicationContext applicationContext;

    @Override
    protected void multiThread(ArmoryRequestEntity armoryRequestEntity, ArmoryContext armoryContext) {
        // 缺省的，可以让继承类不一定非要实现该方法
    }

    protected synchronized <T> void registerBean(String beanName, Class<T> beanClass, T beanInstance) {
        // 1) 拿到 Bean 工厂
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();

        // 2) 销毁之前有的 Singleton 和 BeanDefinition
        if (beanFactory.containsSingleton(beanName)) {
            beanFactory.destroySingleton(beanName);
        }
        if (beanFactory.containsBeanDefinition(beanName)) {
            beanFactory.removeBeanDefinition(beanName);
        }

        // 3) 注册 BeanDefinition
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(beanClass, () -> beanInstance);
        BeanDefinition definition = builder.getBeanDefinition();
        definition.setScope(BeanDefinition.SCOPE_SINGLETON);
        beanFactory.registerBeanDefinition(beanName, definition);
    }

    protected <T> T getBean(String beanName) {
        return (T) applicationContext.getBean(beanName);
    }

}
