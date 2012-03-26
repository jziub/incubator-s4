/*
 * Copyright (c) 2011 Yahoo! Inc. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *          http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License. See accompanying LICENSE file. 
 */
package org.apache.s4.core;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.s4.base.Event;
import org.apache.s4.base.KeyFinder;

/*
 * The Key class is used to get the value of the key on a specific type of
 * event. This is done to abstract all the complexity required to get the 
 * value. The method for getting the value is implemented in a method of 
 * an object of type KeyFinder<T>.
 * 
 * The application programmer provides the events and the corresponding 
 * finders. The framework will use it to key on events.
 */
public class Key<T extends Event> {

    final private KeyFinder<T> finder;
    final private String separator;

    public Key(KeyFinder<T> finder, String separator) {
        this.finder = finder;
        this.separator = separator;
    }

    public List<String> getList(T event) {
        return finder.get(event);
    }

    public String get(T event) {
        List<String> keys = getList(event);

        return StringUtils.join(keys, separator);
    }
}
