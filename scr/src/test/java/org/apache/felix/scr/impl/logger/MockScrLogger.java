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
package org.apache.felix.scr.impl.logger;


import org.apache.felix.scr.impl.MockBundle;
import org.apache.felix.scr.impl.MockBundleContext;
import org.apache.felix.scr.impl.manager.ScrConfiguration;
import org.osgi.service.log.LogService;


public class MockScrLogger extends ScrLogger
{
    public MockScrLogger()
    {
        super(new ScrConfiguration()
        {

            @Override
            public long stopTimeout()
            {
                return 0;
            }

            @Override
            public long lockTimeout()
            {
                return 0;
            }

            @Override
            public boolean keepInstances()
            {
                return false;
            }

            @Override
            public boolean isFactoryEnabled()
            {
                return false;
            }

            @Override
            public boolean infoAsService()
            {
                return false;
            }

            @Override
            public long serviceChangecountTimeout()
            {
                return 0;
            }

            @Override
            public int getLogLevel()
            {
                return LogService.LOG_ERROR;
            }

            @Override
            public boolean globalExtender() {
                return false;
            }

            @Override
            public boolean cacheMetadata()
            {
                return false;
            }
        }, new MockBundleContext(new MockBundle()));
    }
}