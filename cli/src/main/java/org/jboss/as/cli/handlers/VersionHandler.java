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
package org.jboss.as.cli.handlers;


import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.jboss.as.cli.CommandArgument;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandFormatException;
import org.jboss.as.cli.CommandHandler;
import org.jboss.as.cli.Util;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.dmr.ModelNode;
import org.wildfly.security.manager.WildFlySecurityManager;

/**
 *
 * @author Alexey Loubyansky
 */
public class VersionHandler implements CommandHandler {

    public static final VersionHandler INSTANCE = new VersionHandler();

    /* (non-Javadoc)
     * @see org.jboss.as.cli.CommandHandler#isAvailable(org.jboss.as.cli.CommandContext)
     */
    @Override
    public boolean isAvailable(CommandContext ctx) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.jboss.as.cli.CommandHandler#isBatchMode()
     */
    @Override
    public boolean isBatchMode(CommandContext ctx) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.jboss.as.cli.CommandHandler#handle(org.jboss.as.cli.CommandContext)
     */
    @Override
    public void handle(CommandContext ctx) throws CommandFormatException {
        final StringBuilder buf = new StringBuilder();
        buf.append("JBoss Admin Command-line Interface\n");
        buf.append("JBOSS_HOME: ").append(WildFlySecurityManager.getEnvPropertyPrivileged("JBOSS_HOME", null)).append('\n');
        buf.append("JBoss AS release: ");
        final ModelControllerClient client = ctx.getModelControllerClient();
        if(client == null) {
            buf.append("<connect to the controller and re-run the version command to see the release info>\n");
        } else {
            final ModelNode req = new ModelNode();
            req.get(Util.OPERATION).set(Util.READ_RESOURCE);
            req.get(Util.ADDRESS).setEmptyList();
            try {
                final ModelNode response = client.execute(req);
                if(Util.isSuccess(response)) {
                    if(response.hasDefined(Util.RESULT)) {
                        final ModelNode result = response.get(Util.RESULT);
                        byte flag = 0;
                        if(result.hasDefined(Util.RELEASE_VERSION)) {
                            buf.append(result.get(Util.RELEASE_VERSION).asString());
                            ++flag;
                        }
                        if(result.hasDefined(Util.RELEASE_CODENAME)) {
                            String codename = result.get(Util.RELEASE_CODENAME).asString();
                            if (codename.length() > 0) {
                                buf.append(" \"").append(codename).append('\"');
                                ++flag;
                            }
                        }
                        if(flag == 0) {
                            buf.append("release info was not provided by the controller");
                        }

                        if(result.hasDefined(Util.PRODUCT_NAME)) {
                            buf.append("\nJBoss AS product: ").append(result.get(Util.PRODUCT_NAME).asString());
                            if(result.hasDefined(Util.PRODUCT_VERSION)) {
                                buf.append(' ').append(result.get(Util.PRODUCT_VERSION).asString());
                            }
                        }
                    } else {
                        buf.append("result was not available.");
                    }
                } else {
                    buf.append(Util.getFailureDescription(response));
                }
                buf.append('\n');
            } catch (IOException e) {
                throw new CommandFormatException("Failed to get the AS release info: " + e.getLocalizedMessage());
            }
        }
        buf.append("JAVA_HOME: ").append(WildFlySecurityManager.getEnvPropertyPrivileged("JAVA_HOME", null)).append('\n');
        buf.append("java.version: ").append(WildFlySecurityManager.getPropertyPrivileged("java.version", null)).append('\n');
        buf.append("java.vm.vendor: ").append(WildFlySecurityManager.getPropertyPrivileged("java.vm.vendor", null)).append('\n');
        buf.append("java.vm.version: ").append(WildFlySecurityManager.getPropertyPrivileged("java.vm.version", null)).append('\n');
        buf.append("os.name: ").append(WildFlySecurityManager.getPropertyPrivileged("os.name", null)).append('\n');
        buf.append("os.version: ").append(WildFlySecurityManager.getPropertyPrivileged("os.version", null));
        ctx.printLine(buf.toString());
    }

    @Override
    public CommandArgument getArgument(CommandContext ctx, String name) {
        return null;
    }

    @Override
    public boolean hasArgument(CommandContext ctx, String name) {
        return false;
    }

    @Override
    public boolean hasArgument(CommandContext ctx, int index) {
        return false;
    }

    @Override
    public List<CommandArgument> getArguments(CommandContext ctx) {
        return Collections.emptyList();
    }
}
