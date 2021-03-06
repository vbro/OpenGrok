/*
 * CDDL HEADER START
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * See LICENSE.txt included in this distribution for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file at LICENSE.txt.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 *
 * CDDL HEADER END
 */

 /*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
 */
package org.opensolaris.opengrok.authorization;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import org.opensolaris.opengrok.configuration.Nameable;

/**
 * This class covers authorization entities used in opengrok.
 *
 * Currently there are two:
 * <ul>
 * <li>stack of plugins</li>
 * <li>plugin</li>
 * </ul>
 *
 * The purpose is to extract common member variables and methods into an class,
 * namely:
 * <ul>
 * <li>name</li>
 * <li>role - sufficient/required/requisite</li>
 * <li>state - working/failed</li>
 * <li>setup - from configuration</li>
 * </ul>
 * and let the subclasses implement the important abstract methods.
 *
 * This class is intended to be read from a configuration.
 *
 * @author Krystof Tulinger
 */
public abstract class AuthorizationEntity implements Nameable, Serializable, Cloneable {

    private static final long serialVersionUID = 1L;
    /**
     * One of "required", "requisite", "sufficient".
     */
    protected AuthControlFlag flag;
    protected String name;
    protected Map<String, Object> setup = new TreeMap<>();

    protected transient boolean working = true;

    public AuthorizationEntity() {
    }

    /**
     * Copy constructor for the entity:
     * <ul>
     * <li>copy flag</li>
     * <li>copy name</li>
     * <li>deep copy of the setup</li>
     * <li>copy the working attribute</li>
     * </ul>
     *
     * @param x the entity to be copied
     */
    public AuthorizationEntity(AuthorizationEntity x) {
        flag = x.flag;
        name = x.name;
        setup = new TreeMap<>(x.setup);
        working = x.working;
    }

    public AuthorizationEntity(AuthControlFlag flag, String name) {
        this.flag = flag;
        this.name = name;
    }

    /**
     * Load this entity with given parameters.
     *
     * @param parameters given parameters passed to the plugin's load method
     *
     * @see IAuthorizationPlugin#load(java.util.Map)
     */
    abstract public void load(Map<String, Object> parameters);

    /**
     * Unload this entity.
     *
     * @see IAuthorizationPlugin#unload()
     */
    abstract public void unload();

    /**
     * Test the given entity if it should be allowed with this authorization
     * check.
     *
     * @param entity the given entity - this is either group or project and is
     * passed just for the logging purposes.
     * @param predicate predicate returning true or false for the given entity
     * which determines if the authorization for such entity is successful or
     * failed
     * @return true if successful; false otherwise
     */
    abstract public boolean isAllowed(Nameable entity, Predicate<IAuthorizationPlugin> predicate);

    /**
     * Set the plugin to all classes which requires this class in the
     * configuration. This creates a new instance of the plugin for each class
     * which needs it.
     *
     * @param plugin the new instance of a plugin
     * @return true if there is such case; false otherwise
     */
    abstract public boolean setPlugin(IAuthorizationPlugin plugin);

    /**
     * Perform a deep copy of the entity.
     *
     * @return the new instance of this entity
     */
    @Override
    abstract public AuthorizationEntity clone();

    /**
     * Get the value of flag
     *
     * @return the value of flag
     */
    public AuthControlFlag getFlag() {
        return flag;
    }

    /**
     * Set the value of flag
     *
     * @param flag new value of flag
     */
    public void setFlag(AuthControlFlag flag) {
        this.flag = flag;
    }

    /**
     * Set the value of flag
     *
     * @param flag new value of flag
     */
    public void setFlag(String flag) {
        this.flag = AuthControlFlag.get(flag);
    }

    /**
     * Get the value of name
     *
     * @return the value of name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Set the value of name
     *
     * @param name new value of name
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the value of setup
     *
     * @return the value of setup
     */
    public Map<String, Object> getSetup() {
        return setup;
    }

    /**
     * Set the value of setup
     *
     * @param setup new value of setup
     */
    public void setSetup(Map<String, Object> setup) {
        this.setup = setup;
    }

    /**
     * Check if the plugin exists and has not failed while loading.
     *
     * @return true if working, false otherwise
     */
    public boolean isWorking() {
        return working;
    }

    /**
     * Mark this entity as working.
     */
    public synchronized void setWorking() {
        working = true;
    }

    /**
     * Check if this plugin has failed during loading or is missing.
     *
     * This method has the same effect as !{@link isWorking()}.
     *
     * @return true if failed, true otherwise
     * @see #isWorking()
     */
    public boolean isFailed() {
        return !isWorking();
    }

    /**
     * Set this plugin as failed. This plugin will no more call the underlying
     * plugin isAllowed methods.
     *
     * @see IAuthorizationPlugin#isAllowed(HttpServletRequest, Group)
     * @see IAuthorizationPlugin#isAllowed(HttpServletRequest, Project)
     */
    public synchronized void setFailed() {
        working = false;
    }

    /**
     * Check if this plugin is marked as required.
     *
     * @return true if is required; false otherwise
     */
    public boolean isRequired() {
        return getFlag().isRequired();
    }

    /**
     * Check if this plugin is marked as sufficient.
     *
     * @return true if is sufficient; false otherwise
     */
    public boolean isSufficient() {
        return getFlag().isSufficient();
    }

    /**
     * Check if this plugin is marked as requisite.
     *
     * @return true if is requisite; false otherwise
     */
    public boolean isRequisite() {
        return getFlag().isRequisite();
    }
}
