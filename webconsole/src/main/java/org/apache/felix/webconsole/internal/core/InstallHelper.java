/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.webconsole.internal.core;


import java.io.File;
import java.io.InputStream;

import org.apache.felix.webconsole.SimpleWebConsolePlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.startlevel.BundleStartLevel;


class InstallHelper extends BaseUpdateInstallHelper {
    private final BundleContext bundleContext;
    private final String location;
    private final int startlevel;
    private final boolean doStart;


    InstallHelper( final SimpleWebConsolePlugin plugin, final BundleContext bundleContext, final File bundleFile,
        final String location, final int startlevel, final boolean doStart, final boolean refreshPackages )
    {
        super( plugin, "Background Install " + bundleFile, bundleFile, refreshPackages );

        this.bundleContext = bundleContext;
        this.location = location;
        this.startlevel = startlevel;
        this.doStart = doStart;
    }


    @Override
    protected Bundle doRun( final InputStream bundleStream ) throws BundleException {
        Bundle bundle = bundleContext.installBundle( location, bundleStream );

        if ( startlevel > 0 ) {
            final BundleStartLevel bsl = bundle.adapt(BundleStartLevel.class);
            if (bsl != null) {
                bsl.setStartLevel(startlevel);
            }
        }

        // don't start fragments
        if ( doStart && bundle.getHeaders().get( Constants.FRAGMENT_HOST ) == null ) {
            bundle.start();
        }

        return bundle;
    }
}