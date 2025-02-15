/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.felix.http.base.internal.whiteboard.tracker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.http.base.internal.jakartawrappers.ServletContextHelperWrapper;
import org.apache.felix.http.base.internal.runtime.ServletContextHelperInfo;
import org.apache.felix.http.base.internal.util.ServiceUtils;
import org.apache.felix.http.base.internal.whiteboard.WhiteboardManager;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.servlet.context.ServletContextHelper;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Tracks all {@link org.osgi.service.http.context.ServletContextHelper} services for
 * the http whiteboard
 */
public final class JavaxServletContextHelperTracker extends ServiceTracker<ServletContextHelper, ServiceReference<ServletContextHelper>>
{
    private final WhiteboardManager contextManager;

    private final long selfBundleId;

    /** Map containing all info objects reported from the trackers. */
    private final Map<Long, ServletContextHelperInfo> allInfos = new ConcurrentHashMap<Long, ServletContextHelperInfo>();

    private static org.osgi.framework.Filter createFilter(final BundleContext btx)
    {
        try
        {
            return btx.createFilter(String.format("(objectClass=%s)",
                    org.osgi.service.http.context.ServletContextHelper.class.getName()));
        }
        catch ( final InvalidSyntaxException ise)
        {
            // we can safely ignore it as the above filter is a constant
        }
        return null; // we never get here - and if we get an NPE which is fine
    }

    /**
     * Create a new tracker
     * @param context bundle context
     * @param manager whiteboard manager
     */
    public JavaxServletContextHelperTracker(@NotNull final BundleContext context, @NotNull final WhiteboardManager manager)
    {
        super(context, createFilter(context), null);
        this.contextManager = manager;
        this.selfBundleId = context.getBundle().getBundleId();
    }

    @Override
    public void close() {
        super.close();
        this.allInfos.clear();
    }

    @Override
    public final ServiceReference<ServletContextHelper> addingService(@NotNull final ServiceReference<ServletContextHelper> ref)
    {
        this.added(ref);
        return ref;
    }

    @Override
    public final void modifiedService(@NotNull final ServiceReference<ServletContextHelper> ref, @NotNull final ServiceReference<ServletContextHelper> service)
    {
        final ServletContextHelperInfo newInfo = new ServletContextHelperInfo(ref);
        final ServletContextHelperInfo oldInfo = this.allInfos.get(ref.getProperty(Constants.SERVICE_ID));
        if (oldInfo == null || !newInfo.isSame(oldInfo)) {
            this.removed(ref);
            this.added(ref);
        }
    }

    @Override
    public final void removedService(@NotNull final ServiceReference<ServletContextHelper> ref, @NotNull final ServiceReference<ServletContextHelper> service)
    {
        this.removed(ref);
    }

    private void added(@NotNull final ServiceReference<ServletContextHelper> ref)
    {
        // ignore all contexts registered by this bundle
        if ( ref.getBundle().getBundleId() != this.selfBundleId ) {
            final ServletContextHelperInfo info = new JavaxServletContextHelperInfo(ref);
            if ( this.contextManager.addContextHelper(info) )
            {
                this.allInfos.put((Long)ref.getProperty(Constants.SERVICE_ID), info);
            }
        }
    }

    private void removed(@NotNull final ServiceReference<ServletContextHelper> ref)
    {
        final ServletContextHelperInfo info = this.allInfos.get(ref.getProperty(Constants.SERVICE_ID));
        if ( info != null )
        {
            this.contextManager.removeContextHelper(info);
        }
    }

    /**
     * Filter info for javax filters
     */
    private static final class JavaxServletContextHelperInfo extends ServletContextHelperInfo {

        private final ServiceReference<org.osgi.service.http.context.ServletContextHelper> reference;

        /**
         * Create new info
         * @param ref Reference to the service
         */
        @SuppressWarnings({ "rawtypes", "unchecked" })
        public JavaxServletContextHelperInfo(final ServiceReference<ServletContextHelper> ref) {
            super(ref);
            this.reference = (ServiceReference)ref;
        }

        @Override
        public ServletContextHelper getService(final BundleContext bundleContext) {
            final org.osgi.service.http.context.ServletContextHelper helper = ServiceUtils.safeGetServiceObjects(bundleContext, this.reference);
            if ( helper == null ) {
                return null;
            }
            return new ServletContextHelperWrapper(helper);
        }

        @Override
        public void ungetService(final BundleContext bundleContext, final ServletContextHelper service) {
            if ( service instanceof ServletContextHelperWrapper ) {
                final org.osgi.service.http.context.ServletContextHelper helper = ((ServletContextHelperWrapper)service).getHelper();
                ServiceUtils.safeUngetServiceObjects(bundleContext, this.reference, helper);
            }
        }

        @Override
        public @NotNull String getServiceType() {
            return org.osgi.service.http.context.ServletContextHelper.class.getName();
        }
    }

}
