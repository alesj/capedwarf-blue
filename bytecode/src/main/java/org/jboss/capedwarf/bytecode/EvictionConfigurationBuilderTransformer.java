/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.capedwarf.bytecode;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class EvictionConfigurationBuilderTransformer extends RewriteTransformer {
    protected void transformInternal(CtClass clazz) throws Exception {
        final ClassPool pool = clazz.getClassPool();
        CtClass intClass = pool.get(int.class.getName());

        CtMethod method = new CtMethod(clazz, "maxEntries", new CtClass[]{intClass}, clazz);
        method.setBody("return maxEntries((new Integer($1).longValue()));");
        clazz.addMethod(method);
    }

    protected boolean doCheck(CtClass clazz) throws NotFoundException {
        int count = 0;
        for (CtMethod m : clazz.getDeclaredMethods()) {
            if ("maxEntries".equals(m.getName())) {
                count++;
            }
        }
        return (count == 2);
    }
}
