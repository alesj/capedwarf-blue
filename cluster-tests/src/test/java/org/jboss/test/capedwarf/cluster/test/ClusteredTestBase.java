package org.jboss.test.capedwarf.cluster.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.test.TestBase;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public abstract class ClusteredTestBase extends TestBase {

    @Deployment (name = "dep1") @TargetsContainer("container-1")
    public static WebArchive getDeploymentA() {
        return getDeployment();
    }

    @Deployment(name = "dep2") @TargetsContainer("container-2")
    public static WebArchive getDeploymentB() {
        return getDeployment();
    }

    public static WebArchive getDeployment() {
        return getCapedwarfDeployment().addClass(ClusteredTestBase.class);
    }
}
