
package clishell.reflection;

import clishell.util.PropertyReferenceResolver;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class BeanGetterTest {

    public class Bean {
        private final String mBeanName;
        private Bean mNestedBean;
        private Bean mNestedBean2;
        public Bean(String name) {
            mBeanName = name;
        }
        public Bean(Bean nestedBean) {
            this("nested");
            mNestedBean = nestedBean;
        }
        public Bean(Bean nestedBean, Bean nestedBean2) {
            this(nestedBean);
            mNestedBean2 = nestedBean2;
        }
        public String getValue() {
            return mBeanName + " value";
        }
        public Bean getNestedBean() {
            return mNestedBean;
        }
        public Bean getNestedBean2() {
            return mNestedBean2;
        }
    }

    private final BeanGetter mBeanGetter = new BeanGetter();

    @Test
    public void test_getBeanProperty() {
        Bean bean1 = new Bean("my");
        Assert.assertEquals("my value", mBeanGetter.getBeanPropertyPath(bean1, "value"));
    }

    @Test
    public void test_getBeanPropertyPath1() {
        Bean bean2 = new Bean("your");
        Assert.assertEquals("your value", mBeanGetter.getBeanPropertyPath(bean2, "value"));
    }

    @Test
    public void test_getBeanPropertyPath2() {
        Bean bean3 = new Bean(new Bean("his"));
        Assert.assertEquals("his value", mBeanGetter.getBeanPropertyPath(bean3, "nestedBean.value"));

    }

    @Test
    public void test_getBeanPropertyPath3() {
        Bean bean4 = new Bean(new Bean(new Bean("their")));
        Assert.assertEquals("their value", mBeanGetter.getBeanPropertyPath(bean4, "nestedBean.nestedBean.value"));
    }

    @Test
    public void test_getBeanPropertyPath4() {
        Bean bean4 = new Bean(new Bean(new Bean("our"), new Bean("her")));
        Assert.assertEquals("our value", mBeanGetter.getBeanPropertyPath(bean4, "nestedBean.nestedBean.value"));
        Assert.assertEquals("her value", mBeanGetter.getBeanPropertyPath(bean4, "nestedBean.nestedBean2.value"));
    }

    @Test
    public void test_beanPropertyResolver1() {
        Bean bean5 = new Bean(new Bean(new Bean(new Bean("one")), new Bean("two")));
        PropertyReferenceResolver propertyReferenceResolver = new PropertyReferenceResolver("%{", "}");
        Assert.assertEquals(
            "hi there 'one value', 'two value'!",
            propertyReferenceResolver.resolvePropertyReferences(
                "hi there '%{nestedBean.nestedBean.nestedBean.value}', '%{nestedBean.nestedBean2.value}'!",
                mBeanGetter.setBean(bean5)
            )
        );
    }

}
